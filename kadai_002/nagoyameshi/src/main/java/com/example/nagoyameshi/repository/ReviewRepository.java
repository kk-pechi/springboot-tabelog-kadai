package com.example.nagoyameshi.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.entity.User;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
	   public List<Review> findTop6ByShopOrderByCreatedAtDesc(Shop shop);
	   public List<Review> findTop6ByShopIdOrderByCreatedAtDesc(Integer shopId); 
	   public List<Review> findByShopIdOrderByCreatedAtDesc(Integer shopId);
	   public List<Review> findByShop(Shop shop);
	   public Review findByShopAndUser(Shop shop, User user);
	   public long countByShop(Shop shop);
	   public long countByShopId(Integer shopId);
	   public Page<Review> findByShopOrderByCreatedAtDesc(Shop shop, Pageable pageable);
	   public Review findFirstByOrderByIdDesc();
	}