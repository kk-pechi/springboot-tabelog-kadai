package com.example.nagoyameshi.form;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Range;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class ReservationInputForm {

    private Integer shopId;

    @NotNull(message = "予約日を選択してください。")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate reservationDate;

    @NotNull(message = "予約時間を選択してください。")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime reservationTime;

    @NotNull(message = "予約人数を入力してください。")
    @Range(min = 1, max = 20)
    @Min(value = 1, message = "予約人数は1人以上に設定してください。")
    private Integer numberOfPeople;
}