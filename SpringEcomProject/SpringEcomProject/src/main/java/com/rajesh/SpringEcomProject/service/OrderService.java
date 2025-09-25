package com.rajesh.SpringEcomProject.service;

import com.rajesh.SpringEcomProject.model.Order;
import com.rajesh.SpringEcomProject.model.OrderItem;
import com.rajesh.SpringEcomProject.model.Product;
import com.rajesh.SpringEcomProject.model.dto.OrderItemRequest;
import com.rajesh.SpringEcomProject.model.dto.OrderItemResponse;
import com.rajesh.SpringEcomProject.model.dto.OrderRequest;
import com.rajesh.SpringEcomProject.model.dto.OrderResponse;
import com.rajesh.SpringEcomProject.repo.OrderRepo;
import com.rajesh.SpringEcomProject.repo.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private OrderRepo orderRepo;


    public OrderResponse placeOrder(OrderRequest orderRequest) {

        Order order = new Order();
        String orderId ="ORD" + UUID.randomUUID().toString().substring(0,8).toLowerCase();
        order.setOrderId(orderId);
        order.setCustomerName(orderRequest.customerName());
        order.setEmail(orderRequest.email());
        order.setStatus("placed");
        order.setOrderDate(LocalDate.now());

        List<OrderItem> orderItems = new ArrayList<>();
        for(OrderItemRequest itemRequest : orderRequest.items()){

            Product product = productRepo.findById(itemRequest.productId())
                    .orElseThrow(() -> new RuntimeException("product not found"));

            product.setStockQuantity(product.getStockQuantity() - itemRequest.quantity());
            productRepo.save(product);

            OrderItem orderItem = new OrderItem(
                    product,
                    itemRequest.quantity(),
                    product.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity())),
                    order
            );
            orderItems.add(orderItem);
        }
        order.setOrderItems(orderItems);
        Order savedOrder = orderRepo.save(order);

        List<OrderItemResponse> itemResponses= new ArrayList<>();
        for(OrderItem item:order.getOrderItems()){
            OrderItemResponse orderItemResponse = new OrderItemResponse(
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getTotalPrice()
            );
            itemResponses.add(orderItemResponse);
        }

        OrderResponse orderResponse = new OrderResponse(
                savedOrder.getOrderId(),
                savedOrder.getCustomerName(),
                savedOrder.getEmail(),
                savedOrder.getStatus(),
                savedOrder.getOrderDate(),
                itemResponses
        );

        return orderResponse;
    }

    public List<OrderResponse> getAllOrdersResponses() {
        List<Order> orders = orderRepo.findAll();
        List<OrderResponse> orderResponses = new ArrayList<>();

        for(Order order : orders){

            List<OrderItemResponse> itemResponses = new ArrayList<>();

            for(OrderItem item: order.getOrderItems()){
                OrderItemResponse orderItemResponse = new OrderItemResponse(
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getTotalPrice()
                );
                itemResponses.add(orderItemResponse);
            }

            OrderResponse orderResponse = new OrderResponse(
                    order.getOrderId(),
                    order.getCustomerName(),
                    order.getEmail(),
                    order.getStatus(),
                    order.getOrderDate(),
                    itemResponses
            );
            orderResponses.add(orderResponse);
        }


        return orderResponses;
    }
}
