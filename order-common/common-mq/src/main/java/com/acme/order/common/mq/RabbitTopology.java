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
            .deadLetterRoutingKey("dead.payment").build();
        Queue confirm = QueueBuilder.durable(CONFIRM_QUEUE).deadLetterExchange(EXCHANGE)
            .deadLetterRoutingKey("dead.inventory").build();
        Queue release = QueueBuilder.durable(RELEASE_QUEUE).deadLetterExchange(EXCHANGE)
            .deadLetterRoutingKey("dead.inventory").build();
        Queue query = QueueBuilder.durable(QUERY_QUEUE).deadLetterExchange(EXCHANGE).deadLetterRoutingKey("dead.query")
            .build();
        return new Declarables(payment, confirm, release, query,
            BindingBuilder.bind(payment).to(exchange).with("payment.succeeded"),
            BindingBuilder.bind(confirm).to(exchange).with("order.paid"),
            BindingBuilder.bind(release).to(exchange).with("order.canceled"),
            BindingBuilder.bind(query).to(exchange).with("order.#"));
    }
}
