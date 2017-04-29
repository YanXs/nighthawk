package com.github.nightawk.dubbo.http;

import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.tracker.RpcAttachment;
import com.alibaba.dubbo.tracker.RpcTracker;
import com.alibaba.dubbo.tracker.RpcTrackerEngine;
import com.github.kristofa.brave.ClientRequestInterceptor;
import com.github.kristofa.brave.ClientResponseInterceptor;
import com.github.kristofa.brave.http.HttpClientRequestAdapter;
import com.github.kristofa.brave.http.HttpClientResponseAdapter;
import com.github.kristofa.brave.http.SpanNameProvider;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * @author Xs.
 */
public class BraveOkHttpRequestResponseInterceptor implements Interceptor {

    private final ClientRequestInterceptor clientRequestInterceptor;
    private final ClientResponseInterceptor clientResponseInterceptor;
    private final SpanNameProvider spanNameProvider;

    public BraveOkHttpRequestResponseInterceptor(RpcTracker rpcTracker, SpanNameProvider spanNameProvider) {
        RpcTrackerEngine trackerEngine = rpcTracker.trackerEngine();
        this.clientRequestInterceptor = (ClientRequestInterceptor) trackerEngine.clientRequestInterceptor();
        this.clientResponseInterceptor = (ClientResponseInterceptor) trackerEngine.clientResponseInterceptor();
        this.spanNameProvider = spanNameProvider;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request.Builder builder = request.newBuilder();
        OkHttpRequest okHttpRequest = new OkHttpRequest(builder, request);
        // add remote method name
        addMethodName(okHttpRequest);
        clientRequestInterceptor.handle(new HttpClientRequestAdapter(okHttpRequest, spanNameProvider));
        Response response = chain.proceed(builder.build());
        clientResponseInterceptor.handle(new HttpClientResponseAdapter(new OkHttpResponse(response)));
        return response;
    }

    /**
     * @param okHttpRequest
     */
    private void addMethodName(OkHttpRequest okHttpRequest) {
        okHttpRequest.addHeader(RpcAttachment.SpanName.getName(), RpcContext.getContext().getMethodName());
    }
}
