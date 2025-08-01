package com.example.nagoyameshi.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.nagoyameshi.entity.Favorite;

public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {
	//お気に入り登録済みか確認
	//UserのIDとShopのIDが一致するレコードが存在するか
	boolean existsByUserIdAndShopId(Integer userId, Integer shopId);
	
	//指定したユーザと民宿の組み合わせに一致するお気に入り情報を取得
	//お気に入り解除につかう
	List<Favorite> findByUserIdAndShopId(Integer userId, Integer shopId);
	
	//お気に入りのリストを取得する
	//ページネーションも使う
	Page<Favorite> findByUserId(Integer userId, Pageable pageable);
	
	@Query(value = "SELECT f FROM Favorite f JOIN FETCH f.shop WHERE f.user.id = :userId",
		       countQuery = "SELECT COUNT(f) FROM Favorite f WHERE f.user.id = :userId")
		Page<Favorite> findByUserIdWithShop(@Param("userId") Integer userId, Pageable pageable);
}