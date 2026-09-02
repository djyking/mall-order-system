# 压测说明

运行 `k6 run scripts/k6-order.js`，可通过 `RATE`、`DURATION`、`BASE_URL` 调节。记录 QPS、平均延迟、P95、P99、错误率，同时观察 CPU、JVM、Hikari 连接池和 RabbitMQ backlog。

仓库不包含虚构吞吐数据。只有在固定硬件、数据量、中间件部署与脚本参数下实际执行后，才把结果追加到本文件。
