package com.github.nightawk.core.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author xs.
 */
public class ExceptionTracer {

    public static String trace(Throwable throwable) {
        StringWriter stringWriter = new StringWriter(8096);
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
