package com.acme.order.payment;

import com.acme.order.api.payment.PaymentDtos.CreateRequest;
import com.acme.order.api.payment.PaymentDtos.PayView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 编排支付单创建、支付成功和支付查询业务。
 *
 * @author heyu
 * @since 2026-08-20
 */
@Service
public class PaymentService {

    private final PaymentRepository repo;

    public PaymentService(PaymentRepository r) {
        repo = r;
    }

    @Transactional
    public PayView create(CreateRequest r) {
        return repo.create(r.orderNo(), r.userId(), r.amountCent());
    }

    @Transactional
    public boolean mockSuccess(String no, String notify) {
        return repo.success(no, notify);
    }

    public PayView get(String no) {
        return repo.get(no);
    }
}
