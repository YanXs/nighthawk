package com.nightawk.dubbo.protocol.resteasy;

import okhttp3.OkHttpClient;
import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.internal.ClientInvocation;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

public class OkHttpClientEngine implements ClientHttpEngine {

    private OkHttpClient okHttpClient;
    private boolean createdHttpClient;
    protected HostnameVerifier hostnameVerifier;
    protected int responseBufferSize = 8192;

    public OkHttpClientEngine() {
        this.okHttpClient = new OkHttpClient();
        this.createdHttpClient = true;
    }

    public OkHttpClientEngine(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    @Override
    public SSLContext getSslContext() {
        return null;
    }

    @Override
    public HostnameVerifier getHostnameVerifier() {
        return null;
    }

    @Override
    public ClientResponse invoke(ClientInvocation request) {
        return null;
    }

    @Override
    public void close() {

    }
}
