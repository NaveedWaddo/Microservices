package com.ecommerce.cart.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    
    @OneToMany(cascade = CascadeType.ALL)
    private List<CartItem> items;
}