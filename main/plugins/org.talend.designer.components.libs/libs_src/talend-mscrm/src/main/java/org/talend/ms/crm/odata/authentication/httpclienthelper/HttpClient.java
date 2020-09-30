package org.talend.ms.crm.odata.authentication.httpclienthelper;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class HttpClient {

    public final static int TEMPORARY_REDIRECT = 307;
    public final static int PERMANENT_REDIRECT = 308;

    private RequestHttpContext requestHttpContext;

    public HttpClient(RequestHttpContext requestHttpContext) {
        this.requestHttpContext = requestHttpContext;
    }

    public static String encodeValue(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }

    public HttpResponse call(AtomicInteger atomicInteger, Function<HttpResponse, Boolean> acceptRedirect) throws IOException, InterruptedException {
        final HttpURLConnection conn = buildUrl(requestHttpContext);

        if ("POST".equals(conn.getRequestMethod())) {
            conn.setDoOutput(true);
            try (DataOutputStream dos = new DataOutputStream(conn.getOutputStream())) {
                final byte[] form = requestHttpContext.getBodyContent().getBytes(StandardCharsets.UTF_8);
                dos.write(form);
                dos.flush();
            }
        } else {
            conn.connect();
        }

        final HttpResponse respContext = HttpResponse.fromHttpUrlConnection(conn);
        return followRedirect(requestHttpContext, respContext, atomicInteger, acceptRedirect);
    }

    private HttpResponse followRedirect(RequestHttpContext queryContext, HttpResponse response, AtomicInteger nbRedirect, Function<HttpResponse, Boolean> acceptRedirect) throws IOException, InterruptedException {
        final int nbR = nbRedirect.decrementAndGet();
        final int status = response.getStatus();

        boolean redirect = false;
        if (status != HttpURLConnection.HTTP_OK && ((status == HttpURLConnection.HTTP_MOVED_TEMP
                || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER
                || status == TEMPORARY_REDIRECT || status == PERMANENT_REDIRECT))) {
            redirect = true;
        }


        //final HttpHeaders headers = resp.headers();
        final Optional<String> location = response.getFirstValueHeader("location");


        if (!redirect || !location.isPresent() || nbR <= 0) {
            return response;
        }

        queryContext.setBase(location.get());
        queryContext.setParams(Collections.emptyMap());

        final List<String> cookie = response.getAllValuesHeader("Set-Cookie").orElse(Collections.emptyList());
        if (cookie.size() > 0) {
            final String collect = cookie.stream().collect(Collectors.joining("; "));
            queryContext.getHeaders().put("Cookie", collect);
        }

        Boolean continueRedirect = acceptRedirect.apply(response);
        if (!continueRedirect) {
            return response;
        }

        HttpClient manager = new HttpClient(queryContext);
        final HttpResponse redirectResponseContext = manager.call(nbRedirect, acceptRedirect);


        return redirectResponseContext;
    }

    private HttpURLConnection buildUrl(RequestHttpContext context) throws IOException {
        StringBuilder sb = new StringBuilder(context.getBase());

        if (context.getParams().size() > 0) {
            sb.append("?");

            boolean first = true;
            for (Map.Entry<String, String> e : context.getParams().entrySet()) {
                if (!first) {
                    sb.append("&");
                }
                first = false;
                sb.append(e.getKey());
                sb.append("=");
                sb.append(encodeValue(e.getValue()));
            }
        }

        URL url = new URL(sb.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        if (context.getHeaders().size() > 0) {
            for (Map.Entry<String, String> e : context.getHeaders().entrySet()) {
                conn.setRequestProperty(e.getKey(), e.getValue());
            }
        }

        conn.setRequestMethod(context.getMethod());
        conn.setInstanceFollowRedirects(context.isFollowRedirects());
        conn.setConnectTimeout(context.getConnectionTimeout());
        conn.setReadTimeout(context.getReadTimeout());

        return conn;
    }

    public RequestHttpContext getRequestHttpContext() {
        return requestHttpContext;
    }
}