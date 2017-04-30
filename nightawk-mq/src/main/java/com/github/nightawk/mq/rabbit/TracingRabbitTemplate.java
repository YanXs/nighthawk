package com.github.nightawk.mq.rabbit;

import com.github.kristofa.brave.*;
import com.github.nightawk.core.util.Headers;
import com.twitter.zipkin.gen.Endpoint;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;

import java.util.Collection;
import java.util.Collections;

/**
 * @author xs.
 */
public class TracingRabbitTemplate extends RabbitTemplate {

    private final ClientRequestInterceptor clientRequestInterceptor;

    private final ClientResponseInterceptor clientResponseInterceptor;

    public TracingRabbitTemplate(ClientRequestInterceptor clientRequestInterceptor,
                                 ClientResponseInterceptor clientResponseInterceptor) {
        this.clientRequestInterceptor = clientRequestInterceptor;
        this.clientResponseInterceptor = clientResponseInterceptor;
    }


    public void send(final String exchange, final String routingKey,
                     final Message message, final CorrelationData correlationData)
            throws AmqpException {
        clientRequestInterceptor.handle(new RabbitClientRequestAdapter(message));
        super.send(exchange, routingKey, message, correlationData);
        clientResponseInterceptor.handle(new RabbitClientResponseAdapter(message));
    }

    protected Message doSendAndReceive(final String exchange, final String routingKey, final Message message,
                                       CorrelationData correlationData) {
        clientRequestInterceptor.handle(new RabbitClientRequestAdapter(message));
        Message returnMessage = super.doSendAndReceive(exchange, routingKey, message, correlationData);
        clientResponseInterceptor.handle(new RabbitClientResponseAdapter(message));
        return returnMessage;
    }

    private class RabbitClientRequestAdapter implements ClientRequestAdapter {

        private final Message message;

        private RabbitClientRequestAdapter(Message message) {
            this.message = message;
        }

        @Override
        public String getSpanName() {
            return "rabbit-mq";
        }

        @Override
        public void addSpanIdToRequest(SpanId spanId) {
            MessageProperties messageProperties = message.getMessageProperties();
            if (spanId == null) {
                messageProperties.setHeader(Headers.Sampled.getName(), "0");
            } else {
                messageProperties.setHeader(Headers.Sampled.getName(), "1");
                messageProperties.setHeader(Headers.TraceId.getName(), IdConversion.convertToString(spanId.traceId));
                messageProperties.setHeader(Headers.SpanId.getName(), IdConversion.convertToString(spanId.spanId));
                if (spanId.nullableParentId() != null) {
                    messageProperties.setHeader(Headers.ParentSpanId.getName(), IdConversion.convertToString(spanId.parentId));
                }
            }
        }

        @Override
        public Collection<KeyValueAnnotation> requestAnnotations() {
            return Collections.emptyList();
        }

        @Override
        public Endpoint serverAddress() {
            return null;
        }
    }

    private class RabbitClientResponseAdapter implements ClientResponseAdapter {
        private final Message message;

        public RabbitClientResponseAdapter(Message message) {
            this.message = message;
        }

        @Override
        public Collection<KeyValueAnnotation> responseAnnotations() {
            if (message == null){
                return Collections.emptyList();
            }
            KeyValueAnnotation statusAnnotation = KeyValueAnnotation.create("return.status", "OK");
            return Collections.singletonList(statusAnnotation);
        }
    }
}
