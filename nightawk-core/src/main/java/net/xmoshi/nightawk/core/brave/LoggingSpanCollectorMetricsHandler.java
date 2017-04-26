package net.xmoshi.nightawk.core.brave;

import com.github.kristofa.brave.SpanCollectorMetricsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Xs
 */
public class LoggingSpanCollectorMetricsHandler implements SpanCollectorMetricsHandler {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Override
    public void incrementAcceptedSpans(int quantity) {
        // NOP
    }

    @Override
    public void incrementDroppedSpans(int quantity) {
        LOGGER.warn(quantity + " spans were dropped !");
    }
}
