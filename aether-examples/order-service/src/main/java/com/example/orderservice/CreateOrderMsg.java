package com.example.orderservice;

import com.aether.message.Message;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderMsg(UUID customerId, BigDecimal totalAmount) implements Message {
}
