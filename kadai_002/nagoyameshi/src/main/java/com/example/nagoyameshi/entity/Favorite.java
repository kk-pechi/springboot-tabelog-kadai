package com.example.nagoyameshi.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "favorite")
@Data
public class Favorite {

		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY) 
		private Integer id;
		
		@ManyToOne
		@JoinColumn(name = "user_id", nullable = false)
		private User user;
		
		@ManyToOne
		@JoinColumn(name = "shop_id", nullable = false)
		private Shop shop;
		
		@Column(name = "created_at", insertable = false, updatable = false)
		private Timestamp createdAt;
}
