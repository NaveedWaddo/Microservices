package com.ecommerce.order.service;

import com.ecommerce.order.dto.ProductDTO;
import com.ecommerce.order.dto.UserDTO;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    private static final String PRODUCT_SERVICE_URL = "http://localhost:8081/api/products";
    private static final String USER_SERVICE_URL = "http://localhost:8080/api/users";
    
    public Order createOrder(Order order) {
        // Validate user exists
        UserDTO user = restTemplate.getForObject(
            USER_SERVICE_URL + "/{id}", 
            UserDTO.class, 
            order.getUserId()
        );
        
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        
        // Validate products and calculate total
        double totalAmount = 0.0;
        for (OrderItem item : order.getOrderItems()) {
            ProductDTO product = restTemplate.getForObject(
                PRODUCT_SERVICE_URL + "/{id}",
                ProductDTO.class,
                item.getProductId()
            );
            
            if (product == null) {
                throw new RuntimeException("Product not found: " + item.getProductId());
            }
            
            if (product.getQuantity() < item.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
            
            // Update product quantity
            product.setQuantity(product.getQuantity() - item.getQuantity());
            restTemplate.put(PRODUCT_SERVICE_URL + "/{id}", product, product.getId());
            
            // Calculate item total
            item.setPrice(product.getPrice());
            totalAmount += item.getPrice() * item.getQuantity();
        }
        
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setTotalAmount(totalAmount);
        
        return orderRepository.save(order);
    }

    public List<Order> getOrdersByUserId(Long userId) {
        // Validate user exists
        UserDTO user = restTemplate.getForObject(
            USER_SERVICE_URL + "/{id}",
            UserDTO.class,
            userId
        );
        
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        
        return orderRepository.findByUserId(userId);
    }
}