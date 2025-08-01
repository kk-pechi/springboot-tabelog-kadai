package com.example.nagoyameshi.entity;

import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "shops")
@Data
@ToString(exclude = {"reservations", "reviews"})
public class Shop {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "id")
   private Integer id;

   @Column(name = "name")
   private String name;

   @Column(name = "image_name")
   private String imageName;
   
   @ManyToOne
   @JoinColumn(name = "category_id")
   private Category category;

   @Column(name = "description")
   private String description;

   @Column(name = "postal_code")
   private String postalCode;

   @Column(name = "address")
   private String address;

   @Column(name = "opening_time")
   private LocalTime openingTime;

   @Column(name = "closing_time")
   private LocalTime closingTime;

   @Column(name = "regular_holiday")
   private String regularHoliday;
   
   @Column(name = "phone_number")
   private String phoneNumber;
   
   @Column(name = "capacity")
   private Integer capacity;

   @Column(name = "created_at", insertable = false, updatable = false)
   private Timestamp createdAt;

   @Column(name = "updated_at", insertable = false, updatable = false)
   private Timestamp updatedAt;
   
   @OneToMany(mappedBy = "shop", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
   private List<Reservation> reservations;
   
   @OneToMany(mappedBy = "shop", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
   private List<Review> reviews;
   
   @Column(name = "average_rating")
   private Double averageRating;
   
   @Transient
   public int getRoundedAverageRating() {
       if (getAverageRating() == null) return 0;
       return (int) Math.round(getAverageRating());
   }
   @Transient
   public Double getAverageRating() {
       if (reviews == null || reviews.isEmpty()) {
           return 0.0;
       }
       return reviews.stream()
                     .mapToInt(Review::getRating)
                     .average()
                     .orElse(0.0);
   }
}