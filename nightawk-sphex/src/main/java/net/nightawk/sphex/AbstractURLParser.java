package net.nightawk.sphex;

import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractURLParser implements URLParser {

    protected static final ConcurrentHashMap<String, URLCapsule> URL_CAPSULES = new ConcurrentHashMap<>();

}
