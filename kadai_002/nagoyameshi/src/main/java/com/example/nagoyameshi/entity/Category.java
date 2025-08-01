package com.example.nagoyameshi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import lombok.Data;

@Entity
@Table(name = "categories")
@Data
public class Category {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "id")
   private Integer id;

   @Column(name = "name")
   private String name;
   
   @Transient
   public String getImageFileName() {
       switch (this.name) {
           case "焼き肉":
               return "yakiniku.jpg";
           case "テイクアウト":
               return "takeout.jpg";
           case "居酒屋":
               return "izakaya.jpg";
           case "海鮮料理":
               return "kaisen.jpg";
           case "ラーメン":
               return "ramen.jpg";
           case "和食":
               return "washoku.jpg";
           default:
               return null;
       }
   }
}