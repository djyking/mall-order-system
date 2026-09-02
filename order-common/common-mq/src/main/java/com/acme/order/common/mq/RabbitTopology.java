package com.acme.order.common.mq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.*;

/** 订单领域事件的 RabbitMQ 拓扑配置。 */
@Configuration
public class RabbitTopology {
  public static final String EXCHANGE = "order.domain.exchange",
      PAYMENT_QUEUE = "order.payment-succeeded.queue",
      CONFIRM_QUEUE = "order.inventory-confirm.queue",
      RELEASE_QUEUE = "order.inventory-release.queue",
      QUERY_QUEUE = "order.query-index.queue";

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
    Queue payment =
        QueueBuilder.durable(PAYMENT_QUEUE)
            .deadLetterExchange(EXCHANGE)
            .deadLetterRoutingKey("dead.payment")
            .build();
    Queue confirm =
        QueueBuilder.durable(CONFIRM_QUEUE)
            .deadLetterExchange(EXCHANGE)
            .deadLetterRoutingKey("dead.inventory")
            .build();
    Queue release =
        QueueBuilder.durable(RELEASE_QUEUE)
            .deadLetterExchange(EXCHANGE)
            .deadLetterRoutingKey("dead.inventory")
            .build();
    Queue query =
        QueueBuilder.durable(QUERY_QUEUE)
            .deadLetterExchange(EXCHANGE)
            .deadLetterRoutingKey("dead.query")
            .build();
    return new Declarables(
        payment,
        confirm,
        release,
        query,
        BindingBuilder.bind(payment).to(exchange).with("payment.succeeded"),
        BindingBuilder.bind(confirm).to(exchange).with("order.paid"),
        BindingBuilder.bind(release).to(exchange).with("order.canceled"),
        BindingBuilder.bind(query).to(exchange).with("order.#"));
  }
}
