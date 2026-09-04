package com.acme.order.common.mq;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 订单领域事件的 RabbitMQ 拓扑配置。
 *
 * @author heyu
 * @since 2026-07-15
 */
@Configuration
public class RabbitTopology {

    /**
     * 订单领域事件交换机。
     */
    public static final String EXCHANGE = "order.domain.exchange";

    /**
     * 支付成功事件队列。
     */
    public static final String PAYMENT_QUEUE = "order.payment-succeeded.queue";

    /**
     * 库存确认事件队列。
     */
    public static final String CONFIRM_QUEUE = "order.inventory-confirm.queue";

    /**
     * 库存释放事件队列。
     */
    public static final String RELEASE_QUEUE = "order.inventory-release.queue";

    /**
     * 订单查询索引事件队列。
     */
    public static final String QUERY_QUEUE = "order.query-index.queue";

    public static final String CLOSE_DELAY_QUEUE = "order.close.delay.queue";

    public static final String CLOSE_QUEUE = "order.close.queue";

    public static final String PAYMENT_DLQ = "payment.succeeded.dlq";

    public static final String CONFIRM_DLQ = "inventory.confirm.dlq";

    public static final String RELEASE_DLQ = "inventory.release.dlq";

    public static final String CLOSE_DLQ = "order.close.dlq";

    public static final String QUERY_DLQ = "query.projection.dlq";

    @Bean
    TopicExchange domainExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    Jackson2JsonMessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    Declarables orderBindings(TopicExchange exchange) {
        Queue payment = QueueBuilder.durable(PAYMENT_QUEUE).deadLetterExchange(EXCHANGE)
            .deadLetterRoutingKey("dead.payment.succeeded").build();
        Queue confirm = QueueBuilder.durable(CONFIRM_QUEUE).deadLetterExchange(EXCHANGE)
            .deadLetterRoutingKey("dead.inventory.confirm").build();
        Queue release = QueueBuilder.durable(RELEASE_QUEUE).deadLetterExchange(EXCHANGE)
            .deadLetterRoutingKey("dead.inventory.release").build();
        Queue query = QueueBuilder.durable(QUERY_QUEUE).deadLetterExchange(EXCHANGE)
            .deadLetterRoutingKey("dead.query").build();
        Queue closeDelay = QueueBuilder.durable(CLOSE_DELAY_QUEUE).ttl(30 * 60 * 1000)
            .deadLetterExchange(EXCHANGE).deadLetterRoutingKey("order.close").build();
        Queue close = QueueBuilder.durable(CLOSE_QUEUE).deadLetterExchange(EXCHANGE)
            .deadLetterRoutingKey("dead.order.close").build();
        Queue paymentDlq = QueueBuilder.durable(PAYMENT_DLQ).build();
        Queue confirmDlq = QueueBuilder.durable(CONFIRM_DLQ).build();
        Queue releaseDlq = QueueBuilder.durable(RELEASE_DLQ).build();
        Queue closeDlq = QueueBuilder.durable(CLOSE_DLQ).build();
        Queue queryDlq = QueueBuilder.durable(QUERY_DLQ).build();
        return new Declarables(payment, confirm, release, query, closeDelay, close, paymentDlq, confirmDlq,
            releaseDlq, closeDlq, queryDlq,
            BindingBuilder.bind(payment).to(exchange).with("payment.succeeded"),
            BindingBuilder.bind(confirm).to(exchange).with("order.paid"),
            BindingBuilder.bind(release).to(exchange).with("order.canceled"),
            BindingBuilder.bind(query).to(exchange).with("order.#"),
            BindingBuilder.bind(closeDelay).to(exchange).with("order.created"),
            BindingBuilder.bind(close).to(exchange).with("order.close"),
            BindingBuilder.bind(paymentDlq).to(exchange).with("dead.payment.succeeded"),
            BindingBuilder.bind(confirmDlq).to(exchange).with("dead.inventory.confirm"),
            BindingBuilder.bind(releaseDlq).to(exchange).with("dead.inventory.release"),
            BindingBuilder.bind(closeDlq).to(exchange).with("dead.order.close"),
            BindingBuilder.bind(queryDlq).to(exchange).with("dead.query"));
    }
}
