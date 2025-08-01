package com.example.nagoyameshi.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.ReservationInputForm;
import com.example.nagoyameshi.repository.ReservationRepository;

@Service
public class ReservationService {
	private final ReservationRepository reservationRepository;


    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;

    }

    // 指定されたユーザーに紐づく予約を作成日時が新しい順に並べ替え、ページングされた状態で取得する
    public Page<Reservation> findReservationsByUserOrderByCreatedAtDesc(User user, Pageable pageable) {
        return reservationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }
       
    // 予約人数が定員以下かどうかをチェックする
    public boolean isWithinCapacity(Integer numberOfPeople, Integer capacity) {
        return numberOfPeople <= capacity;
    }
    
 // reservationDate + reservationTime を flatpickr 用の文字列に整形
    public String getPreviousDateTime(LocalDate reservationDate, LocalTime reservationTime, BindingResult bindingResult) {
        if (reservationDate != null && reservationTime != null
                && !bindingResult.hasFieldErrors("reservationDate")
                && !bindingResult.hasFieldErrors("reservationTime")) {

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            String formattedDate = reservationDate.format(dateFormatter);
            String formattedTime = reservationTime.format(timeFormatter);

            return formattedDate + " " + formattedTime;
        }

        return "";
    }
    
    @Transactional
    public void createReservation(ReservationInputForm reservationInputForm, Shop shop, User user) {
    	Reservation reservation = new Reservation();
        reservation.setShop(shop);
        reservation.setUser(user);
        reservation.setReservationDate(reservationInputForm.getReservationDate());
        reservation.setReservationTime(reservationInputForm.getReservationTime());
        reservation.setNumberOfPeople(reservationInputForm.getNumberOfPeople());

        reservationRepository.save(reservation);
    } 
    @Transactional
    public void deleteReservation(Reservation reservation) {
        reservationRepository.delete(reservation);
    }
    public Optional<Reservation> findReservationById(Integer id) {
        return reservationRepository.findById(id);
    }
    
}
