# 分库分表

配置样例位于 `config/sharding/application-sharding.yml`：按 `user_id % 2` 分库，按 `order_id % 8` 分表。优点是“我的订单”固定在单库，写入分散；代价是用户列表仍需单库多表归并。订单号查询先访问 Redis 路由缓存，未命中查询 `order_route`；运营查询走 CQRS 投影而不扫描全部分片。

默认开发配置使用单库以便直接运行；启用 `sharding` profile 前需要加入 ShardingSphere-JDBC 运行依赖并建立 16 组物理表。该配置属于预留接入，不应被描述为默认已启用。
