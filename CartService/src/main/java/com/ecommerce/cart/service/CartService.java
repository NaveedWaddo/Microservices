package com.ecommerce.cart.service;

import com.ecommerce.cart.dto.ProductDTO;
import com.ecommerce.cart.dto.UserDTO;
import com.ecommerce.cart.entity.Cart;
import com.ecommerce.cart.entity.CartItem;
import com.ecommerce.cart.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    private static final String PRODUCT_SERVICE_URL = "http://localhost:8081/api/products";
    private static final String USER_SERVICE_URL = "http://localhost:8080/api/users";
    
    public Cart getCartByUserId(Long userId) {
        // Validate user exists
        UserDTO user = restTemplate.getForObject(
            USER_SERVICE_URL + "/{id}",
            UserDTO.class,
            userId
        );
        
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        
        return cartRepository.findByUserId(userId)
            .orElseGet(() -> {
                Cart newCart = new Cart();
                newCart.setUserId(userId);
                return cartRepository.save(newCart);
            });
    }
    
    public Cart addToCart(Long userId, Long productId, Integer quantity) {
        // Validate product exists and has sufficient stock
        ProductDTO product = restTemplate.getForObject(
            PRODUCT_SERVICE_URL + "/{id}",
            ProductDTO.class,
            productId
        );
        
        if (product == null) {
            throw new RuntimeException("Product not found");
        }
        
        if (product.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock");
        }
        
        Cart cart = getCartByUserId(userId);
        
        // Check if product already exists in cart
        CartItem existingItem = cart.getItems().stream()
            .filter(item -> item.getProductId().equals(productId))
            .findFirst()
            .orElse(null);
            
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setProductId(productId);
            newItem.setQuantity(quantity);
            cart.getItems().add(newItem);
        }
        
        return cartRepository.save(cart);
    }
}