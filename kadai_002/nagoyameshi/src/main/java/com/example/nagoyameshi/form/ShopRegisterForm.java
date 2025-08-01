package com.example.nagoyameshi.form;

import java.time.LocalTime;
import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class ShopRegisterForm {
   @NotBlank(message = "店舗名を入力してください。")
   private String name;

   private MultipartFile imageFile;
   
   @NotNull(message = "カテゴリを選択してください。")
   private Integer categoryId;

   @NotBlank(message = "説明を入力してください。")
   private String description;
   
   @NotNull(message = "定員を入力してください。")
   @Min(value = 1, message = "定員は1人以上に設定してください。")
   private Integer capacity;

   @NotBlank(message = "郵便番号を入力してください。")
   @Pattern(regexp = "\\d{3}-\\d{4}", message = "郵便番号はXXX-XXXXの形式で入力してください。")
   private String postalCode;

   @NotBlank(message = "住所を入力してください。")
   private String address;

   @NotNull(message = "開店時間を選択してください。")
   private LocalTime openingTime;

   @NotNull(message = "閉店時間を選択してください。")
   private LocalTime closingTime;

   @NotNull(message = "定休日を選択してください。")
   private List<String> regularHoliday;
   
   @NotBlank(message = "電話番号を入力してください。")
   @Pattern(regexp = "0\\d{1,3}-\\d{1,4}-\\d{4}", message = "電話番号は10桁または11桁の半角数字をハイフン付きで入力してください。")
   private String phoneNumber;
}