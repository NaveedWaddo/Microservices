package com.ecommerce.cart.controller;

import com.ecommerce.cart.entity.Cart;
import com.ecommerce.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
public class CartController {
    @Autowired
    private CartService cartService;
    
    @GetMapping("/user/{userId}")
    public Cart getCart(@PathVariable Long userId) {
        return cartService.getCartByUserId(userId);
    }
    
    @PostMapping("/user/{userId}/products/{productId}")
    public Cart addToCart(
        @PathVariable Long userId,
        @PathVariable Long productId,
        @RequestParam Integer quantity
    ) {
        return cartService.addToCart(userId, productId, quantity);
    }
}