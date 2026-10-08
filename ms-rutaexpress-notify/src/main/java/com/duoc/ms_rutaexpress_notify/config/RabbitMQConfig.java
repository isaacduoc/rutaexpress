package com.duoc.ms_rutaexpress_notify.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchanges
    public static final String EXCHANGE_DIRECT = "cmd.direct";
    public static final String EXCHANGE_TOPIC = "cmd.topic";
    public static final String EXCHANGE_DLX = "cmd.dead.dlx";

    // Colas principales
    public static final String QUEUE_EMAIL = "q.cmd.email";
    public static final String QUEUE_WAREHOUSE = "q.cmd.warehouse";
    public static final String QUEUE_LABEL = "q.cmd.label";

    // Dead Letter Queues
    public static final String QUEUE_EMAIL_DLQ = "q.cmd.email.dlq";
    public static final String QUEUE_WAREHOUSE_DLQ = "q.cmd.warehouse.dlq";
    public static final String QUEUE_LABEL_DLQ = "q.cmd.label.dlq";

    // Routing keys principales
    public static final String ROUTING_KEY_EMAIL = "email.send";
    public static final String ROUTING_KEY_WAREHOUSE = "warehouse.ticket";
    public static final String ROUTING_KEY_LABEL = "label.gen";

    // Routing keys DLQ
    public static final String ROUTING_KEY_EMAIL_DLQ = "email.dlq";
    public static final String ROUTING_KEY_WAREHOUSE_DLQ = "warehouse.dlq";
    public static final String ROUTING_KEY_LABEL_DLQ = "label.dlq";

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(EXCHANGE_DIRECT, true, false);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE_TOPIC, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(EXCHANGE_DLX, true, false);
    }

    // =========================
    // COLAS PRINCIPALES
    // =========================

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(QUEUE_EMAIL)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_EMAIL_DLQ)
                .build();
    }

    @Bean
    public Queue warehouseQueue() {
        return QueueBuilder.durable(QUEUE_WAREHOUSE)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_WAREHOUSE_DLQ)
                .build();
    }

    @Bean
    public Queue labelQueue() {
        return QueueBuilder.durable(QUEUE_LABEL)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_LABEL_DLQ)
                .build();
    }

    // =========================
    // DEAD LETTER QUEUES
    // =========================

    @Bean
    public Queue emailDlq() {
        return QueueBuilder.durable(QUEUE_EMAIL_DLQ).build();
    }

    @Bean
    public Queue warehouseDlq() {
        return QueueBuilder.durable(QUEUE_WAREHOUSE_DLQ).build();
    }

    @Bean
    public Queue labelDlq() {
        return QueueBuilder.durable(QUEUE_LABEL_DLQ).build();
    }

    // =========================
    // BINDINGS DIRECT
    // =========================

    @Bean
    public Binding emailDirectBinding(
            Queue emailQueue,
            DirectExchange directExchange) {

        return BindingBuilder.bind(emailQueue)
                .to(directExchange)
                .with(ROUTING_KEY_EMAIL);
    }

    @Bean
    public Binding warehouseDirectBinding(
            Queue warehouseQueue,
            DirectExchange directExchange) {

        return BindingBuilder.bind(warehouseQueue)
                .to(directExchange)
                .with(ROUTING_KEY_WAREHOUSE);
    }

    @Bean
    public Binding labelDirectBinding(
            Queue labelQueue,
            DirectExchange directExchange) {

        return BindingBuilder.bind(labelQueue)
                .to(directExchange)
                .with(ROUTING_KEY_LABEL);
    }

    // =========================
    // BINDINGS TOPIC
    // =========================

    @Bean
    public Binding emailTopicBinding(
            Queue emailQueue,
            TopicExchange topicExchange) {

        return BindingBuilder.bind(emailQueue)
                .to(topicExchange)
                .with("email.*");
    }

    @Bean
    public Binding warehouseTopicBinding(
            Queue warehouseQueue,
            TopicExchange topicExchange) {

        return BindingBuilder.bind(warehouseQueue)
                .to(topicExchange)
                .with("warehouse.#");
    }

    @Bean
    public Binding labelTopicBinding(
            Queue labelQueue,
            TopicExchange topicExchange) {

        return BindingBuilder.bind(labelQueue)
                .to(topicExchange)
                .with("label.*");
    }

    // =========================
    // BINDINGS DLQ
    // =========================

    @Bean
    public Binding emailDlqBinding(
            Queue emailDlq,
            DirectExchange deadLetterExchange) {

        return BindingBuilder.bind(emailDlq)
                .to(deadLetterExchange)
                .with(ROUTING_KEY_EMAIL_DLQ);
    }

    @Bean
    public Binding warehouseDlqBinding(
            Queue warehouseDlq,
            DirectExchange deadLetterExchange) {

        return BindingBuilder.bind(warehouseDlq)
                .to(deadLetterExchange)
                .with(ROUTING_KEY_WAREHOUSE_DLQ);
    }

    @Bean
    public Binding labelDlqBinding(
            Queue labelDlq,
            DirectExchange deadLetterExchange) {

        return BindingBuilder.bind(labelDlq)
                .to(deadLetterExchange)
                .with(ROUTING_KEY_LABEL_DLQ);
    }
}