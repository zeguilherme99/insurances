package com.zagdev.insurances.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public TopicExchange policyExchange() {
        return new TopicExchange("policy-exchange");
    }

    @Bean
    public Queue statusQueue() {
        return new Queue("policy-status-queue", false);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);

        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                .maxAttempts(5)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build());

        return factory;
    }

    @Bean
    public Queue paymentResultQueue() {
        return QueueBuilder.durable("payment-result-queue")
                .deadLetterExchange("")
                .deadLetterRoutingKey("payment-result-queue.dlq")
                .build();
    }

    @Bean
    public Queue paymentResultDlq() {
        return QueueBuilder.durable("payment-result-queue.dlq").build();
    }

    @Bean
    public Binding binding(Queue statusQueue, TopicExchange policyExchange) {
        return BindingBuilder.bind(statusQueue).to(policyExchange).with("policy.status.changed");
    }
}
