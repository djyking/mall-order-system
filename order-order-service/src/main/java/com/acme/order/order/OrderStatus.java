package com.acme.order.order;
public enum OrderStatus {WAIT_PAY(10),WAIT_DELIVERY(20),WAIT_RECEIVE(30),COMPLETED(40),CANCELED(90);public final int code;OrderStatus(int c){code=c;}public static OrderStatus of(int c){for(var s:values())if(s.code==c)return s;throw new IllegalArgumentException("unknown status "+c);}}
