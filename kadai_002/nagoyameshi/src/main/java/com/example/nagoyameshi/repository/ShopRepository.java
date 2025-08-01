package com.example.nagoyameshi.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.nagoyameshi.entity.Shop;

public interface ShopRepository extends JpaRepository<Shop, Integer> {
   public Page<Shop> findByNameLike(String keyword, Pageable pageable);
   public Shop findFirstByOrderByIdDesc();
   public List<Shop> findByCategoryId(Integer categoryId);
   public List<Shop> findByCategoryName(String name);
   public Page<Shop> findByCategory(String category, Pageable pageable);
   public Page<Shop> findByCategory_Name(String name, Pageable pageable);
   public Page<Shop> findByNameLikeOrAddressLike(String nameKeyword, String addressKeyword, Pageable pageable);
   public Page<Shop> findByAddressLike(String area, Pageable pageable); 
   public Page<Shop> findByNameLikeOrAddressLikeOrderByCreatedAtDesc(String nameKeyword, String addressKeyword, Pageable pageable);
   public Page<Shop> findByAddressLikeOrderByCreatedAtDesc(String area, Pageable pageable);
   public Page<Shop> findAllByOrderByCreatedAtDesc(Pageable pageable);
   public Page<Shop> findByCategory_NameOrderByCreatedAtDesc(String categoryName, Pageable pageable);
   public List<Shop> findTop10ByOrderByCreatedAtDesc();
   @Query("""
		   SELECT s FROM Shop s
		   LEFT JOIN s.reviews r
		   GROUP BY s.id
		   ORDER BY AVG(r.rating) DESC
		 """)
   public Page<Shop> findAllByOrderByAverageRatingDesc(Pageable pageable);

   @Query("""
		   SELECT s FROM Shop s
		   LEFT JOIN s.reviews r
		   WHERE s.name LIKE %:name%
		      OR s.address LIKE %:address%
		      OR s.category.name LIKE %:categoryName%
		   GROUP BY s.id
		   ORDER BY AVG(r.rating) DESC
		 """)
  public Page<Shop> findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByAverageRatingDesc(
		     @Param("name") String nameKeyword,
		     @Param("address") String addressKeyword,
		     @Param("categoryName") String categoryNameKeyword,
		     Pageable pageable);

   @Query("""
		   SELECT s FROM Shop s
		   LEFT JOIN s.reviews r
		   WHERE s.category.id = :categoryId
		   GROUP BY s.id
		   ORDER BY AVG(r.rating) DESC
		 """)
  public Page<Shop> findByCategoryIdOrderByAverageRatingDesc(@Param("categoryId") Integer categoryId, Pageable pageable);

  

}