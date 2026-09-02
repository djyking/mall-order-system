# RabbitMQ 拓扑

Topic Exchange：`order.domain.exchange`。核心路由键为 `payment.succeeded`、`order.created`、`order.paid`、`order.canceled`。消费者队列分别推进订单状态、确认库存、释放库存和更新查询投影。队列配置死信路由，业务异常回滚数据库事务并拒绝 ACK；生产端由 Outbox 重试，不把 Publisher Confirm 误认为数据库与 MQ 原子事务。
