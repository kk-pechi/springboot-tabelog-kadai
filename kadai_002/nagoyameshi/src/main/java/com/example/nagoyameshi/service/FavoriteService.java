package com.example.nagoyameshi.service;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.nagoyameshi.entity.Favorite;
import com.example.nagoyameshi.repository.FavoriteRepository;
import com.example.nagoyameshi.repository.ShopRepository;
import com.example.nagoyameshi.repository.UserRepository;

@Service
public class FavoriteService {
	private final FavoriteRepository favoriteRepository;
	private final ShopRepository shopRepository;
	private final UserRepository userRepository;
	
	//各リポジトリをSpringから注入するためのコンストラクタ（DI）
	public FavoriteService(FavoriteRepository favoriteRepository, ShopRepository shopRepository, UserRepository userRepository) {
		this.favoriteRepository = favoriteRepository; 
		this.shopRepository = shopRepository; 
		this.userRepository = userRepository; 
	}
	
	@Transactional//お気に入りを追加する
	public void addFavorite(Integer userId, Integer shopId){
		if (!favoriteRepository.existsByUserIdAndShopId(userId, shopId)) {
			Favorite favorite = new Favorite();
			favorite.setUser(userRepository.getReferenceById(userId));
			favorite.setShop(shopRepository.getReferenceById(shopId));
			//Favoriteオブジェクトを作成して、ユーザーと店舗情報をセットしDBに登録
			favoriteRepository.save(favorite);
		}
	}
		
	@Transactional
	public void removeFavorite(Integer userId, Integer shopId) {
	    List<Favorite> favorites = favoriteRepository.findByUserIdAndShopId(userId, shopId);
	    for (Favorite favorite : favorites) {
	        favoriteRepository.delete(favorite);
	    }
	}
	
	//お気に入りに登録しているかチェック
	public boolean isFavorite(Integer userId, Integer shopId) {
		//この組み合わせのデータが存在するかどうかを返す
		return favoriteRepository.existsByUserIdAndShopId(userId, shopId);
	}
	
	//ページネーションありのリスト取得用
	public Page<Favorite> getFavoritesByUser(Integer userId, Pageable pageable) {
		return favoriteRepository.findByUserId(userId, pageable);
	}
	
	public Page<Favorite> getFavoritesByUserWithShop(Integer userId, Pageable pageable) {
	    return favoriteRepository.findByUserIdWithShop(userId, pageable);
	}
	
}
