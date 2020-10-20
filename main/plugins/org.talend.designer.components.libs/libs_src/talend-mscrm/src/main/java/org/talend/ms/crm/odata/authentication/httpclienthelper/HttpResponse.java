package org.talend.ms.crm.odata.authentication.httpclienthelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class HttpResponse {
    private int status;
    private Map<String, List<String>> headers;
    private String body;

    private Optional<String> code = null;//Optional.empty();

    public static HttpResponse fromHttpUrlConnection(HttpURLConnection conn) throws IOException {
        final int status = conn.getResponseCode();
        final Map<String, List<String>> respHeaders = conn.getHeaderFields();

        HttpResponse context = new HttpResponse(status);
        context.setHeaders(respHeaders);

        try(BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))){
            final String body = in.lines().collect(Collectors.joining("\n"));
            context.setBody(body);
        }

        return context;
    }

    private HttpResponse(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    private Optional<List<String>> _getAllValuesHeader(String key) {
        if (this.getHeaders() == null || this.getHeaders().size() <= 0) {
            return Optional.empty();
        }

        if (!this.getHeaders().containsKey(key)) {
            return Optional.empty();
        }

        return Optional.ofNullable(this.getHeaders().get(key));
    }

    public Optional<List<String>> getAllValuesHeader(String key) {
        String keyUpper = ("" + key.charAt(0)).toUpperCase() + key.substring(1);
        String keyLower = ("" + key.charAt(0)).toLowerCase() + key.substring(1);

        Optional<List<String>> values = _getAllValuesHeader(keyUpper);
        if(!values.isPresent()){
            values = _getAllValuesHeader(keyLower);
        }

        return values;
    }

    public Optional<String> getFirstValueHeader(String key) {
        final Optional<List<String>> values = getAllValuesHeader(key);
        if(!values.isPresent()){
            return Optional.empty();
        }

        final List<String> ss = values.get();
        if(ss.size() <= 0){
            return Optional.empty();
        }

        return Optional.ofNullable(ss.get(0));
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Extract 'Code' parameter from the redirect url if exist.
     * @return An Optional with the code if any.
     */
    private Optional<String> _extractCode(){
        final Optional<String> optLocation = this.getFirstValueHeader("Location");
        if (!optLocation.isPresent()) {
            return Optional.empty();
        }
        final String[] split = optLocation.get().split("&|\\?");
        final Optional<String> optCode = Arrays.stream(split).filter(e -> e.startsWith("code=")).findFirst();

        if (optCode.isPresent()) {
            String code = optCode.get().substring(5);
            return Optional.ofNullable(code);
        }

        return Optional.empty();
    }

    public Optional<String> extractCode(){
        if(code != null){
            return code;
        }

        code = _extractCode();
        return code;
    }
}
