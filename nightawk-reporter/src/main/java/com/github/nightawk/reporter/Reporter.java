package com.github.nightawk.reporter;

/**
 * @author Xs.
 */
public interface Reporter {

    void report(Metrics metrics);

    Reporter NEVER_REPORT = new Reporter() {
        @Override
        public void report(Metrics metrics) {
            // NOP
        }
    };
}
