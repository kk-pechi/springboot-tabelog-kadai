package com.example.nagoyameshi.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Range;

import lombok.Data;

@Data
public class ReviewRegisterForm {
   @NotNull(message = "評価を選択してください。")
   @Range(min = 1, max = 5)
   private Integer rating;

   @NotBlank(message = "コメントを入力してください。")
   private String comment;
}