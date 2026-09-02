package com.acme.order.common.observability;

import io.micrometer.core.instrument.*;

public final class OrderMetrics {
    private final MeterRegistry registry;
    public OrderMetrics(MeterRegistry registry){this.registry=registry;}
    public void success(String operation){registry.counter(operation+"_success_total").increment();}
    public void failure(String operation){registry.counter(operation+"_fail_total").increment();}
}
