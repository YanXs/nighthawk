package com.github.nightawk.mq.rabbit;

import com.github.kristofa.brave.*;
import com.github.nightawk.core.util.Headers;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

import java.util.Collection;
import java.util.Collections;

import static com.github.kristofa.brave.IdConversion.convertToLong;

/**
 * @author xs.
 */
public class TracingMessageListenerContainer extends SimpleMessageListenerContainer {

    private final ServerRequestInterceptor serverRequestInterceptor;
    private final ServerResponseInterceptor serverResponseInterceptor;

    public TracingMessageListenerContainer(Brave brave) {
        this.serverRequestInterceptor = brave.serverRequestInterceptor();
        this.serverResponseInterceptor = brave.serverResponseInterceptor();
    }

    protected void doInvokeListener(MessageListener listener, Message message) throws Exception {
        try {
            serverRequestInterceptor.handle(new RabbitServerRequestAdapter(message));
            listener.onMessage(message);
            serverResponseInterceptor.handle(new RabbitServerResponseAdapter());
        } catch (Exception e) {
            throw wrapToListenerExecutionFailedExceptionIfNeeded(e, message);
        }
    }

    private class RabbitServerRequestAdapter implements ServerRequestAdapter {

        private final Message message;

        private RabbitServerRequestAdapter(Message message) {
            this.message = message;
        }

        @Override
        public TraceData getTraceData() {
            MessageProperties messageProperties = message.getMessageProperties();
            final String sampled = (String) messageProperties.getHeaders().get(Headers.Sampled.getName());
            if (sampled != null) {
                if (sampled.equals("0") || sampled.toLowerCase().equals("false")) {
                    return TraceData.builder().sample(false).build();
                } else {
                    final String parentSpanId = (String) messageProperties.getHeaders().get(Headers.ParentSpanId.getName());
                    final String traceId = (String) messageProperties.getHeaders().get(Headers.TraceId.getName());
                    final String spanId = (String) messageProperties.getHeaders().get(Headers.SpanId.getName());

                    if (traceId != null && spanId != null) {
                        SpanId span = getSpanId(traceId, spanId, parentSpanId);
                        return TraceData.builder().sample(true).spanId(span).build();
                    }
                }
            }
            return TraceData.builder().build();
        }

        @Override
        public String getSpanName() {
            return "rabbit-mq";
        }

        @Override
        public Collection<KeyValueAnnotation> requestAnnotations() {
            return Collections.emptyList();
        }

        private SpanId getSpanId(String traceId, String spanId, String parentSpanId) {
            return SpanId.builder()
                    .traceId(convertToLong(traceId))
                    .spanId(convertToLong(spanId))
                    .parentId(parentSpanId == null ? null : convertToLong(parentSpanId)).build();
        }
    }

    private class RabbitServerResponseAdapter implements ServerResponseAdapter {

        @Override
        public Collection<KeyValueAnnotation> responseAnnotations() {
            return Collections.emptyList();
        }
    }
}
