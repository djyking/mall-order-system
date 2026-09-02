package com.acme.order.payment;
import com.acme.order.api.payment.PaymentDtos.*;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;
@Service public class PaymentService {private final PaymentRepository repo;public PaymentService(PaymentRepository r){repo=r;}@Transactional public PayView create(CreateRequest r){return repo.create(r.orderNo(),r.userId(),r.amountCent());}@Transactional public boolean mockSuccess(String no,String notify){return repo.success(no,notify);}public PayView get(String no){return repo.get(no);}}
