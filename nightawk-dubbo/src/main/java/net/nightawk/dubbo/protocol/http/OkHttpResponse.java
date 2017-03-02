package net.nightawk.dubbo.protocol.http;

import com.github.kristofa.brave.http.HttpResponse;
import okhttp3.Response;

/**
 * copy from brave-http
 */
class OkHttpResponse implements HttpResponse {

    private final Response response;

    OkHttpResponse(Response response) {
        this.response = response;
    }

    @Override
    public int getHttpStatusCode() {
        return response.code();
    }
}
