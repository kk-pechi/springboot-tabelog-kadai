package com.example.nagoyameshi.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.form.ShopEditForm;
import com.example.nagoyameshi.form.ShopRegisterForm;
import com.example.nagoyameshi.repository.CategoryRepository;
import com.example.nagoyameshi.repository.ShopRepository;



@Service
public class ShopService {
   private final ShopRepository shopRepository;
   private final CategoryRepository categoryRepository;

   public ShopService(ShopRepository shopRepository, CategoryRepository categoryRepository) {
       this.shopRepository = shopRepository;
       this.categoryRepository = categoryRepository;
   }

   // すべての店舗をページングされた状態で取得する
   public Page<Shop> findAllShops(Pageable pageable) {
       return shopRepository.findAll(pageable);
   }
   
   // すべての店舗を平均評価が高い順に並べ替え、ページングされた状態で取得する
   public Page<Shop> findAllShopsByOrderByAverageRatingDesc(Pageable pageable) {
       return shopRepository.findAllByOrderByAverageRatingDesc(pageable);
   } 

   // 指定されたキーワードを店舗名に含む店舗を、ページングされた状態で取得する
   public Page<Shop> findShopByNameLike(String keyword, Pageable pageable) {
       return shopRepository.findByNameLike("%" + keyword + "%", pageable);
   }

   // 指定したidを持つ店舗を取得する
   public Optional<Shop> findShopById(Integer id) {
       return shopRepository.findById(id);
   }

   // 店舗のレコード数を取得する
   public long countShops() {
       return shopRepository.count();
   }

   // idが最も大きい店舗を取得する
   public Shop findFirstShopByOrderByIdDesc() {
       return shopRepository.findFirstByOrderByIdDesc();
   }
   
   // 指定されたカテゴリの店舗を、ページングされた状態で取得する
   public Page<Shop> findShopsByCategory(String categoryName, Pageable pageable) {
	    return shopRepository.findByCategory_Name(categoryName, pageable);
	}

 //定休日保存用にカンマ区切りにする
   public String convertRegularHolidayListToString(List<String> holidays) {
       if (holidays.contains("定休日なし")) {
           return "定休日なし";
       }
       return String.join(",", holidays);
   }
   
// 指定されたキーワードを店舗名または住所に含む店舗を作成日時が新しい順に並べ替え、ページングされた状態で取得する
   public Page<Shop> findShopsByNameLikeOrAddressLikeOrderByCreatedAtDesc(String nameKeyword, String addressKeyword, Pageable pageable) {
       return shopRepository.findByNameLikeOrAddressLikeOrderByCreatedAtDesc("%" + nameKeyword + "%", "%" + addressKeyword + "%", pageable);
   }

   // 指定されたキーワードを住所に含む店舗を作成日時が新しい順に並べ替え、ページングされた状態で取得する
   public Page<Shop> findShopsByAddressLikeOrderByCreatedAtDesc(String area, Pageable pageable) {
       return shopRepository.findByAddressLikeOrderByCreatedAtDesc("%" + area + "%", pageable);
   }

   // すべての店舗を作成日時が新しい順に並べ替え、ページングされた状態で取得する
   public Page<Shop> findAllShopsByOrderByCreatedAtDesc(Pageable pageable) {
       return shopRepository.findAllByOrderByCreatedAtDesc(pageable);
   }
   
   // 指定されたカテゴリ名に該当する店舗を作成日時が新しい順に並べ替え、ページングされた状態で取得する
   public Page<Shop> findByCategoryNameOrderByCreatedAtDesc(String categoryName, Pageable pageable) {
	    return shopRepository.findByCategory_NameOrderByCreatedAtDesc(categoryName, pageable);
	}
   
   // 指定されたidのカテゴリが設定された店舗を平均評価が高い順に並べ替え、ページングされた状態で取得する
   public Page<Shop> findShopsByCategoryIdOrderByAverageRatingDesc(Integer categoryId, Pageable pageable) {
       return shopRepository.findByCategoryIdOrderByAverageRatingDesc(categoryId, pageable);
   } 

   // 作成日時が新しい順に10件の店舗を取得する
   	public List<Shop> findTop10ShopsByOrderByCreatedAtDesc() {
       return shopRepository.findTop10ByOrderByCreatedAtDesc();
   }  
   	
   	public Page<Shop> findShopsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByAverageRatingDesc(
   	        String nameKeyword, String addressKeyword, String categoryNameKeyword, Pageable pageable) {

   	    return shopRepository.findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByAverageRatingDesc(
   	            "%" + nameKeyword + "%", "%" + addressKeyword + "%", "%" + categoryNameKeyword + "%", pageable);
   	}
   	
   	public List<Shop> findTop10ShopsByOrderByAverageRatingDesc() {
   	    List<Shop> allShops = shopRepository.findAll();
   	    return allShops.stream()
   	            .sorted((s1, s2) -> Double.compare(s2.getAverageRating(), s1.getAverageRating()))
   	            .limit(10)
   	            .toList();
   	}
   	public List<Shop> findTop10ShopsByAverageRating() {
   	    Pageable pageable = PageRequest.of(0, 10);
   	    return shopRepository.findAllByOrderByAverageRatingDesc(pageable).getContent();
   	}


   @Transactional
   public void create(ShopRegisterForm shopRegisterForm) {
       Shop shop = new Shop();
       MultipartFile imageFile = shopRegisterForm.getImageFile();

       if (!imageFile.isEmpty()) {
           String imageName = imageFile.getOriginalFilename(); 
           String hashedImageName = generateNewFileName(imageName);
           Path filePath = Paths.get("src/main/resources/static/storage/" + hashedImageName);
           copyImageFile(imageFile, filePath);
           shop.setImageName(hashedImageName);
       }
       
       Category category = categoryRepository.getReferenceById(shopRegisterForm.getCategoryId());
       
       shop.setName(shopRegisterForm.getName());
       shop.setCategory(category);
       shop.setDescription(shopRegisterForm.getDescription());
       shop.setCapacity(shopRegisterForm.getCapacity());
       shop.setPostalCode(shopRegisterForm.getPostalCode());
       shop.setAddress(shopRegisterForm.getAddress());
       shop.setOpeningTime(shopRegisterForm.getOpeningTime());
       shop.setClosingTime(shopRegisterForm.getClosingTime());
       shop.setRegularHoliday(convertRegularHolidayListToString(shopRegisterForm.getRegularHoliday()));
       shop.setPhoneNumber(shopRegisterForm.getPhoneNumber());

       shopRepository.save(shop);
   }

   @Transactional
   public void update(ShopEditForm shopEditForm) {
	   Shop shop = shopRepository.getReferenceById(shopEditForm.getId());
       MultipartFile imageFile = shopEditForm.getImageFile();

       if (!imageFile.isEmpty()) {
           String imageName = imageFile.getOriginalFilename(); 
           String hashedImageName = generateNewFileName(imageName);
           Path filePath = Paths.get("src/main/resources/static/storage/" + hashedImageName);
           copyImageFile(imageFile, filePath);
           shop.setImageName(hashedImageName);
       }
       
       Category category = categoryRepository.getReferenceById(shopEditForm.getCategoryId());
       shop.setName(shopEditForm.getName());
       shop.setCategory(category);
       shop.setDescription(shopEditForm.getDescription());
       shop.setCapacity(shopEditForm.getCapacity());
       shop.setPostalCode(shopEditForm.getPostalCode());
       shop.setAddress(shopEditForm.getAddress());
       shop.setOpeningTime(shopEditForm.getOpeningTime());
       shop.setClosingTime(shopEditForm.getClosingTime());
       shop.setRegularHoliday(convertRegularHolidayListToString(shopEditForm.getRegularHoliday()));
       shop.setPhoneNumber(shopEditForm.getPhoneNumber());

       shopRepository.save(shop);
   }

   @Transactional
   public void delete(Shop shop) {
       shopRepository.delete(shop);
   }
   
   // 営業時間のチェック
   public boolean isValidBusinessHours(LocalTime openingTime, LocalTime closingTime) {
       return closingTime.isAfter(openingTime);
   }

   // UUIDを使って生成したファイル名を返す
   public String generateNewFileName(String fileName) {
       String[] fileNames = fileName.split("\\.");                
       for (int i = 0; i < fileNames.length - 1; i++) {
           fileNames[i] = UUID.randomUUID().toString();            
       }
       String hashedFileName = String.join(".", fileNames);
       return hashedFileName;
   }     
   
   // 画像ファイルを指定したファイルにコピーする
   public void copyImageFile(MultipartFile imageFile, Path filePath) {           
       try {
           Files.copy(imageFile.getInputStream(), filePath);
       } catch (IOException e) {
           e.printStackTrace();
       }          
   }
}

