package com.github.nightawk.core.util;

import java.lang.reflect.Type;
import java.util.List;

public abstract class AbstractObjectCodec<T> {

    private final Codec codec;

    public AbstractObjectCodec(Codec codec) {
        this.codec = codec;
    }

    public byte[] write(T value) {
        return codec.write(value);
    }

    public byte[] write(List<T> values) {
        return codec.write(values);
    }

    public T read(byte[] bytes, Type type) {
        return codec.read(bytes, type);
    }

    public List<T> readList(byte[] bytes, Type type) {
        return codec.read(bytes, type);
    }
}
