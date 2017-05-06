package com.github.nightawk.core.util;

import java.util.concurrent.TimeUnit;

public interface Sleeper {

    Sleeper JUST_SLEEP = new Sleeper() {
        @Override
        public void sleepFor(long time, TimeUnit unit) throws InterruptedException {
            unit.sleep(time);
        }
    };

    void sleepFor(long time, TimeUnit unit) throws InterruptedException;

}
