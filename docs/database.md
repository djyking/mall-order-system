# 数据库设计

金额全部使用 `BIGINT` 分；时间使用 `DATETIME(3)`；聚合根带 `version`。库存不变量为 `total_stock = available_stock + reserved_stock + sold_stock`。关键唯一键包括订单号、业务订单对应支付单、订单与 SKU 的预占记录、消费组与事件编号。

执行顺序：`00_create_database.sql` 至 `07_query.sql`，最后执行 `init_data.sql`。Docker Compose 会按文件名自动执行。
