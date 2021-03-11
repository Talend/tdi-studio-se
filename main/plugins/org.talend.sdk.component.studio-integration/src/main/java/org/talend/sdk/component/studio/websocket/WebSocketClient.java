/**
 * Copyright (C) 2006-2021 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.talend.sdk.component.studio.websocket;

import static java.util.Collections.emptyList;
import static java.util.Locale.ENGLISH;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.json.bind.Jsonb;
import javax.json.bind.spi.JsonbProvider;
import javax.json.stream.JsonParsingException;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.apache.tomcat.websocket.Constants;
import org.talend.sdk.component.server.front.model.ComponentDetail;
import org.talend.sdk.component.server.front.model.ComponentDetailList;
import org.talend.sdk.component.server.front.model.ComponentIndex;
import org.talend.sdk.component.server.front.model.ComponentIndices;
import org.talend.sdk.component.server.front.model.ConfigTypeNodes;
import org.talend.sdk.component.server.front.model.DocumentationContent;
import org.talend.sdk.component.server.front.model.Environment;
import org.talend.sdk.component.server.front.model.error.ErrorPayload;
import org.talend.sdk.component.studio.lang.Pair;
import org.talend.sdk.component.studio.util.TaCoKitConst;
// we shouldn't need the execution runtime so don't even include it here
//
// technical note: this client includes the transport (websocket) but also the protocol/payload formatting/parsing
// todo: better error handling, can need some server bridge love too to support ERROR responses
public class WebSocketClient implements AutoCloseable {
    private static final byte[] EOM = "^@".getBytes(StandardCharsets.UTF_8);
    private final Queue<Session> sessions = new ConcurrentLinkedQueue<>();
    private final WebSocketContainer container;

    private final String protocol;

    private final String port;

    private final String basePath;

    private final long timeout;
    private final Jsonb jsonb;

    private String serverHost;

    private Runnable synch;

    public WebSocketClient(final String protocol, final String port, final String basePath, final long timeout) {
        this.protocol = protocol;
        this.serverHost = TaCoKitConst.DEFAULT_LOCALHOST;
        this.port = port;
        this.basePath = basePath;
        this.timeout = timeout;
        this.container = ContainerProvider.getWebSocketContainer();
        this.jsonb = JsonbProvider.provider("org.apache.johnzon.jsonb.JohnzonProvider").create().build();
    }

    public void setServerHost(String host) {
        this.serverHost = host;
    }

    public void setSynch(final Runnable synch) {
        this.synch = synch;
    }

    private String buildRequest(final String id, final String uri, final Object payload) {
        final String method = id.substring("/v1/".length(), id.indexOf('/', "/v1/".length() + 1));
        return "SEND\r\ndestination:" + uri + "\r\ndestinationMethod:" + method.toUpperCase(ENGLISH) + "\r\nAccept: application/json\r\nContent-Type: application/json\r\n\r\n" + (payload == null ? "" : jsonb.toJson(payload)) + "^@";
    }

    private <T> T parseResponse(final byte[] payload, final Class<T> expectedResponse) {
        if (expectedResponse.isInstance(payload)) {
            return expectedResponse.cast(payload);
        }
        if (String.class == expectedResponse) {
            return expectedResponse.cast(new String(payload, StandardCharsets.UTF_8));
        }
        try (InputStream stream = new ByteArrayInputStream(payload)) {
            return jsonb.fromJson(stream, expectedResponse);
        } catch (final JsonParsingException jpe) {
            throw new IllegalArgumentException("Can\'t parse JSON: \'" + new String(payload, StandardCharsets.UTF_8) + "\'");
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private <T> T sendAndWait(final String id, final String uri, final Object payload, final Class<T> expectedResponse, final boolean doCheck) {
        final Session session = getOrCreateSession(id, doCheck);
        final PayloadHandler handler = new PayloadHandler(this);
        session.getUserProperties().put("handler", handler);
        final String buildRequest = buildRequest(id, uri, payload);
        try {
            try {
                session.getBasicRemote().sendBinary(ByteBuffer.wrap(buildRequest.getBytes(StandardCharsets.UTF_8)));
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
            try {
                handler.latch.await(1, MINUTES); // todo: make it configurable? 1mn is already a lot
            } catch (final InterruptedException e) {
                Thread.interrupted();
                throw new IllegalStateException(e);
            }
        } finally {
            doRelease(session);
        }
        return parseResponse(handler.payload(), expectedResponse);
    }

    private void doRelease(final Session session) {
        sessions.add(session);
    }

    private Session getOrCreateSession(final String id, final boolean doCheck) {
        if (doCheck && synch != null) {
            synchronized (this) {
                if (synch != null) {
                    synch.run();
                    synch = null;
                }
            }
        }
        Session poll = sessions.poll();
        if (poll != null && !poll.isOpen()) {
            try {
                poll.close(new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "Session is no more opened"));
            } catch (final Exception e) {
            }
            // just to go through close cycle but should fail since it is not opened, we
            // just ignore any error
            poll = null;
        }
        if (poll == null) {
            poll = doConnect();
        }
        return poll;
    }

    private String getBase() {
        return protocol + serverHost + ":" + port + basePath;
    }

    private Session doConnect() {
        final URI connectUri = URI.create(getBase() + "/bus");
        final ClientEndpointConfig endpointConfig = ClientEndpointConfig.Builder.create().build();
        endpointConfig.getUserProperties().put(Constants.IO_TIMEOUT_MS_PROPERTY, Long.toString(timeout));
        try {
            return container.connectToServer(new Endpoint() {
                @Override
                public void onOpen(final Session session, final EndpointConfig endpointConfig) {
                    session.addMessageHandler(ByteBuffer.class, new MessageHandler.Partial<ByteBuffer>() {
                        @Override
                        public synchronized void onMessage(final ByteBuffer part, final boolean last) {
                            final Consumer<ByteBuffer> callback = Consumer.class.cast(session.getUserProperties().get("handler"));
                            callback.accept(part);
                        }
                    });
                }
                @Override
                public void onError(final Session session, final Throwable throwable) {
                    final PayloadHandler handler = PayloadHandler.class.cast(session.getUserProperties().get("handler"));
                    if (handler != null) {
                        handler.latch.countDown();
                    }
                    throw new IllegalStateException(throwable);
                }
            }, endpointConfig, connectUri);
        } catch (final DeploymentException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public synchronized void close() {
        sessions.forEach(s -> {
            try {
                s.close(new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "Shutting down the studio"));
            } catch (final IOException e) {
            }
        });
        // no-op: todo: define if we want to log it, we will not do anything anyway at
        // that time
        sessions.clear();
    }

    public synchronized V1 v1() {
        return new V1(this);
    }


    public static class V1 {
        private final WebSocketClient root;

        private V1(final WebSocketClient root) {
            this.root = root;
        }

        public V1Action action() {
            return new V1Action(root);
        }

        public V1ConfigurationType configurationType() {
            return new V1ConfigurationType(root);
        }

        public V1Component component() {
            return new V1Component(root);
        }

        public V1Documentation documentation() {
            return new V1Documentation(root);
        }

        public boolean healthCheck() {
            root.sendAndWait("/v1/get/environment", "/environment", null, Environment.class, false);
            return true;
        }
    }


    public static class V1ConfigurationType {
        private final WebSocketClient root;

        private V1ConfigurationType(final WebSocketClient root) {
            this.root = root;
        }

        public ConfigTypeNodes getRepositoryModel() {
            return root.sendAndWait("/v1/get/configurationtype/index", "/configurationtype/index?language=" + Locale.getDefault().getLanguage() + "&lightPayload=false", null, ConfigTypeNodes.class, true);
        }

        public ConfigTypeNodes getRepositoryModel(final boolean lightPayload) {
            return root.sendAndWait("/v1/get/configurationtype/index", "/configurationtype/index?language=" + Locale.getDefault().getLanguage() + "&lightPayload="+lightPayload, null, ConfigTypeNodes.class, true);
        }

        public Map<String, String> migrate(final String id, final int configurationVersion, final Map<String, String> payload) {
            return root.sendAndWait("/v1/post/configurationtype/migrate/{id}/{configurationVersion}",
                    "/configurationtype/migrate/" + id + "/" + configurationVersion, payload, Map.class, true);
        }
    }


    public static class V1Action {
        private final WebSocketClient root;

        private V1Action(final WebSocketClient root) {
            this.root = root;
        }

        public <T> T execute(final Class<T> expectedResponse, final String family, final String type, final String action, final Map<String, String> payload) {
            return root.sendAndWait("/v1/post/action/execute", "/action/execute?family=" + family + "&type=" + type + "&action=" + action, payload, expectedResponse, true);
        }
    }

    public static class V1Documentation {

        private final WebSocketClient root;

        private V1Documentation(final WebSocketClient root) {
            this.root = root;
        }

        public DocumentationContent getDocumentation(final String language, final String componentId, final String format) {
            return root.sendAndWait("/v1/get/documentation/component",
                    "/documentation/component/" + componentId + "?language=" + language + "&format=" + format, null,
                    DocumentationContent.class, true);
        }

    }


    public static class V1Component {
        private static final int BUNDLE_SIZE = 25;
        private final WebSocketClient root;

        private V1Component(final WebSocketClient root) {
            this.root = root;
        }

        public ComponentIndices getIndex(final String language) {
            return root.sendAndWait("/v1/get/component/index", "/component/index?language=" + language + "&includeIconContent=true", null, ComponentIndices.class, true);
        }

        public Map<String, ?> dependencies(final String id) {
            return root.sendAndWait("/v1/get/component/dependencies", "/component/dependencies?identifier=" + id, null, Map.class, true);
        }

        public byte[] icon(final String id) {
            return root.sendAndWait("/v1/get/component/icon/" + id, "/component/icon/" + id, null, byte[].class, true);
        }

        public byte[] familyIcon(final String id) {
            return root.sendAndWait("/v1/get/component/icon/family/" + id, "/component/icon/family/" + id, null, byte[].class, true);
        }

        public ComponentDetailList getDetail(final String language, final String[] identifiers) {
            if (identifiers == null || identifiers.length == 0) {
                return new ComponentDetailList(emptyList());
            }
            return root.sendAndWait("/v1/get/component/details", "/component/details?language=" + language + Stream.of(identifiers).map(i -> "identifiers=" + i).collect(Collectors.joining("&", "&", "")), null, ComponentDetailList.class, true);
        }

        public Stream<Pair<ComponentIndex, ComponentDetail>> details(final String language) {
            final List<ComponentIndex> components = getIndex(language).getComponents();
            // create bundles
            int bundleCount = components.size() / BUNDLE_SIZE;
            bundleCount = bundleCount * BUNDLE_SIZE >= components.size() ? bundleCount : (bundleCount + 1);
            return IntStream.range(0, bundleCount).mapToObj(i -> {
                final int from = BUNDLE_SIZE * i;
                final int to = from + BUNDLE_SIZE;
                return components.subList(from, Math.min(to, components.size()));
            }).flatMap(bundle -> {
                final Map<String, ComponentIndex> byId = bundle.stream().collect(toMap(c -> c.getId().getId(), identity()));
                return getDetail(language, bundle.stream().map(i -> i.getId().getId()).toArray(String[]::new)).getDetails().stream().map(d -> new Pair<>(byId.get(d.getId().getId()), d));
            });
        }

        public Map<String, String> migrate(final String id, final int configurationVersion, final Map<String, String> payload) {
            return root.sendAndWait("/v1/post/component/migrate/{id}/{configurationVersion}", "/component/migrate/" + id + "/" + configurationVersion, payload, Map.class, true);
        }
    }


    private static class PayloadHandler implements Consumer<ByteBuffer> {
        private final CountDownLatch latch = new CountDownLatch(1);
        private final ByteArrayOutputStream out = new ByteArrayOutputStream();
        private final byte[] lastBytes = new byte[2];
        private final WebSocketClient root;

        public PayloadHandler(final WebSocketClient webSocketClient) {
            root = webSocketClient;
        }

        @Override
        public void accept(final ByteBuffer byteBuffer) {
            try {
                final byte[] array = byteBuffer.array();
                if (array.length >= 2) {
                    System.arraycopy(array, array.length - 2, lastBytes, 0, 2);
                } else if (array.length > 0) {
                    lastBytes[0] = lastBytes[1];
                    lastBytes[1] = array[0];
                }
                out.write(array);
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            } finally {
                if (Arrays.equals(lastBytes, EOM)) {
                    latch.countDown();
                }
            }
        }

        private byte[] payload() {
            return payload(true);
        }

        private byte[] payload(final boolean failOnBadStatus) {
            final byte[] value = out.toByteArray();
            // todo: check status header and fail if > 399 with the error message in the
            // payload
            int start = 0;
            {
                // find the first empty line which means the payload starts
                boolean visitedEol = false;
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                for (int idx = 0; idx < value.length - 1; idx++) {
                    if (value[idx] == '\r' && value[idx + 1] == '\n') {
                        if (failOnBadStatus) {
                            final String header = new String(baos.toByteArray(), StandardCharsets.UTF_8);
                            if (header.startsWith("status:")) {
                                try {
                                    if (Integer.parseInt(header.substring("status:".length()).trim()) > 399) {
                                        final String response = new String(value);
                                        ErrorPayload errorPayload;
                                        try {
                                            errorPayload = root.parseResponse(payload(false), ErrorPayload.class);
                                        } catch (final IllegalArgumentException iae) {
                                            errorPayload = null;
                                        }
                                        throw new ClientException(response, errorPayload);
                                    }
                                } catch (final NumberFormatException nfe) {
                                }
                            }
                        }
                        // no-op: ignore this validation then
                        idx++;
                        if (visitedEol) {
                            start = idx + 1;
                            break;
                        }
                        visitedEol = true;
                        baos.reset();
                    } else {
                        baos.write(value[idx]);
                        visitedEol = false;
                    }
                }
            }
            final int len = value.length - EOM.length - start;
            if (len <= 0) {
                return new byte[0];
            }
            final byte[] payload = new byte[len];
            System.arraycopy(value, start, payload, 0, len);
            return payload;
        }
    }


    public static class ClientException extends RuntimeException {
        private ErrorPayload errorPayload;

        private ClientException(final String raw, final ErrorPayload errorPayload) {
            super(raw);
            this.errorPayload = errorPayload;
        }

        public ErrorPayload getErrorPayload() {
            return this.errorPayload;
        }
    }
}
