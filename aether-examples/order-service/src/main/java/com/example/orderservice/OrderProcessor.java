package com.example.orderservice;

import com.aether.spring.annotation.ActorBean;
import com.aether.spring.annotation.OnMessage;

@ActorBean(name = "order-processor")
public class OrderProcessor {

    @OnMessage
    public void onCreateOrder(CreateOrderMsg msg) {
        System.out.println("Processing order for customer: " + msg.customerId());
    }

}
