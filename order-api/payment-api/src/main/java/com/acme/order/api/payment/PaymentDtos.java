package com.acme.order.api.payment;

public final class PaymentDtos {
    private PaymentDtos(){}
    public record CreateRequest(String orderNo,long userId,long amountCent){}
    public record PayView(String payOrderNo,String orderNo,long amountCent,String status){}
}
