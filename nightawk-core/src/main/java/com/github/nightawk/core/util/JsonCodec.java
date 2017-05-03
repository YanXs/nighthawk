package com.github.nightawk.core.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

public class JsonCodec implements Codec {

    private final Gson gson;

    public JsonCodec() {
        gson = new GsonBuilder().create();
    }

    @Override
    public byte[] write(Object value) {
        String json = gson.toJson(value);
        try {
            return json.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new CodecException(e);
        }
    }

    @Override
    public <T> T read(byte[] bytes, Type type) {
        return gson.fromJson(new String(bytes), type);
    }
}
