package com.example.nagoyameshi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.ReviewEditForm;
import com.example.nagoyameshi.form.ReviewRegisterForm;
import com.example.nagoyameshi.repository.ReviewRepository;
import com.example.nagoyameshi.repository.ShopRepository;

@Service
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final ShopRepository shopRepository;

	public ReviewService(ReviewRepository reviewRepository, ShopRepository shopRepository) {
	    this.reviewRepository = reviewRepository;
	    this.shopRepository = shopRepository;
	}

    // 指定されたIDのレビューを取得する（存在しない可能性があるのでOptional）
    public Optional<Review> findReviewById(Integer id) {
        return reviewRepository.findById(id);
    }

    // 指定された店舗の最新レビュー上位6件を取得（投稿日時の降順）
    public List<Review> findTop6ReviewsByShopOrderByCreatedAtDesc(Shop shop) {
        return reviewRepository.findTop6ByShopOrderByCreatedAtDesc(shop);
    }

    // 指定された店舗とユーザーに紐づくレビューを1件取得（1人1レビュー制）
    public Review findReviewByShopAndUser(Shop shop, User user) {
        return reviewRepository.findByShopAndUser(shop, user);
    }

    // 指定された店舗のレビュー件数を取得
    public long countReviewsByShop(Shop shop) {
        return reviewRepository.countByShop(shop);
    }

    // 指定された店舗のレビューをページング付きで取得（投稿日降順）
    public Page<Review> findReviewsByShopOrderByCreatedAtDesc(Shop shop, Pageable pageable) {
        return reviewRepository.findByShopOrderByCreatedAtDesc(shop, pageable);
    }

    // レビューを新規登録（レビュー登録フォームの内容をエンティティに詰めて保存）
    @Transactional
    public void createReview(ReviewRegisterForm reviewRegisterForm, Shop shop, User user) {
        Review review = new Review();
        review.setShop(shop);
        review.setUser(user);
        review.setRating(reviewRegisterForm.getRating());
        review.setComment(reviewRegisterForm.getComment());
        reviewRepository.save(review);
    }

    // 既存のレビューを編集（編集フォームの内容で上書き）
    @Transactional
    public void updateReview(ReviewEditForm reviewEditForm, Review review) {
        review.setRating(reviewEditForm.getRating());
        review.setComment(reviewEditForm.getComment());
        reviewRepository.save(review);
    }
    
    public void updateAverageRating(Shop shop) {
        double avg = reviewRepository.findByShop(shop).stream()
            .mapToInt(Review::getRating)
            .average()
            .orElse(0.0);
        shop.setAverageRating(avg);
        shopRepository.save(shop);
    }

    // 指定されたレビューを削除
    @Transactional
    public void deleteReview(Review review) {
        reviewRepository.delete(review);
    }

    // 指定されたユーザーがその店舗にレビューをすでに投稿しているかを確認（1人1レビュー制の制御に使用）
    public boolean hasUserAlreadyReviewed(Shop shop, User user) {
        return reviewRepository.findByShopAndUser(shop, user) != null;
    }

    // 店舗IDを指定して最新レビュー上位6件を取得（Shopオブジェクト不要なケース用）
    public List<Review> findTop6ReviewsByShopId(Integer shopId) {
        return reviewRepository.findTop6ByShopIdOrderByCreatedAtDesc(shopId);
    }

    // 店舗IDを指定してすべてのレビューを投稿日時の降順で取得（ページングなし）
    public List<Review> findAllReviewsByShopId(Integer shopId) {
        return reviewRepository.findByShopIdOrderByCreatedAtDesc(shopId);
    }
}
