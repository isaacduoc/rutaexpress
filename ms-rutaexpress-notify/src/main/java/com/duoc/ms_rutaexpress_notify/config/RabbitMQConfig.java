
package com.duoc.ms_rutaexpress_notify.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // =========================
    // EXCHANGES
    // =========================

    public static final String EXCHANGE_DIRECT = "cmd.direct";
    public static final String EXCHANGE_TOPIC = "cmd.topic";
    public static final String EXCHANGE_DLX = "cmd.dead.dlx";

    // =========================
    // POLITICAS DE RETENCION
    // =========================

    public static final int MESSAGE_TTL = 86400000;
    public static final int MAX_QUEUE_LENGTH = 1000;

    // =========================
    // COLAS PRINCIPALES
    // =========================

    public static final String QUEUE_EMAIL = "q.cmd.email";
    public static final String QUEUE_WAREHOUSE = "q.cmd.warehouse";
    public static final String QUEUE_LABEL = "q.cmd.label";

    // =========================
    // DEAD LETTER QUEUES
    // =========================

    public static final String QUEUE_EMAIL_DLQ = "q.cmd.email.dlq";
    public static final String QUEUE_WAREHOUSE_DLQ = "q.cmd.warehouse.dlq";
    public static final String QUEUE_LABEL_DLQ = "q.cmd.label.dlq";

    // =========================
    // ROUTING KEYS DIRECT
    // =========================

    public static final String ROUTING_KEY_EMAIL = "email.send";
    public static final String ROUTING_KEY_WAREHOUSE = "warehouse.ticket";
    public static final String ROUTING_KEY_LABEL = "label.gen";

    // =========================
    // ROUTING KEYS TOPIC
    // =========================

    public static final String ROUTING_KEY_EMAIL_TOPIC = "email.*";
    public static final String ROUTING_KEY_WAREHOUSE_TOPIC = "warehouse.#";
    public static final String ROUTING_KEY_LABEL_TOPIC = "label.*";

    // =========================
    // ROUTING KEYS DLQ
    // =========================

    public static final String ROUTING_KEY_EMAIL_DLQ = "email.dlq";
    public static final String ROUTING_KEY_WAREHOUSE_DLQ = "warehouse.dlq";
    public static final String ROUTING_KEY_LABEL_DLQ = "label.dlq";

    // =========================
    // CREACION DE EXCHANGES
    // =========================

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
                .withArgument("x-message-ttl", MESSAGE_TTL)
                .withArgument("x-max-length", MAX_QUEUE_LENGTH)
                .build();
    }

    @Bean
    public Queue warehouseQueue() {
        return QueueBuilder.durable(QUEUE_WAREHOUSE)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_WAREHOUSE_DLQ)
                .withArgument("x-message-ttl", MESSAGE_TTL)
                .withArgument("x-max-length", MAX_QUEUE_LENGTH)
                .build();
    }

    @Bean
    public Queue labelQueue() {
        return QueueBuilder.durable(QUEUE_LABEL)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_LABEL_DLQ)
                .withArgument("x-message-ttl", MESSAGE_TTL)
                .withArgument("x-max-length", MAX_QUEUE_LENGTH)
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
    public Binding emailDirectBinding() {
        return BindingBuilder.bind(emailQueue())
                .to(directExchange())
                .with(ROUTING_KEY_EMAIL);
    }

    @Bean
    public Binding warehouseDirectBinding() {
        return BindingBuilder.bind(warehouseQueue())
                .to(directExchange())
                .with(ROUTING_KEY_WAREHOUSE);
    }

    @Bean
    public Binding labelDirectBinding() {
        return BindingBuilder.bind(labelQueue())
                .to(directExchange())
                .with(ROUTING_KEY_LABEL);
    }

    // =========================
    // BINDINGS TOPIC
    // =========================

    @Bean
    public Binding emailTopicBinding() {
        return BindingBuilder.bind(emailQueue())
                .to(topicExchange())
                .with(ROUTING_KEY_EMAIL_TOPIC);
    }

    @Bean
    public Binding warehouseTopicBinding() {
        return BindingBuilder.bind(warehouseQueue())
                .to(topicExchange())
                .with(ROUTING_KEY_WAREHOUSE_TOPIC);
    }

    @Bean
    public Binding labelTopicBinding() {
        return BindingBuilder.bind(labelQueue())
                .to(topicExchange())
                .with(ROUTING_KEY_LABEL_TOPIC);
    }

    // =========================
    // BINDINGS DLQ
    // =========================

    @Bean
    public Binding emailDlqBinding() {
        return BindingBuilder.bind(emailDlq())
                .to(deadLetterExchange())
                .with(ROUTING_KEY_EMAIL_DLQ);
    }

    @Bean
    public Binding warehouseDlqBinding() {
        return BindingBuilder.bind(warehouseDlq())
                .to(deadLetterExchange())
                .with(ROUTING_KEY_WAREHOUSE_DLQ);
    }

    @Bean
    public Binding labelDlqBinding() {
        return BindingBuilder.bind(labelDlq())
                .to(deadLetterExchange())
                .with(ROUTING_KEY_LABEL_DLQ);
    }
}
