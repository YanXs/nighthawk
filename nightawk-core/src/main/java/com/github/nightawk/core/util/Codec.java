package com.github.nightawk.core.util;

import java.lang.reflect.Type;

public interface Codec {

    JsonCodec JSON = new JsonCodec();

    byte[] write(Object value);

    <T> T read(byte[] bytes, Type type);

}
