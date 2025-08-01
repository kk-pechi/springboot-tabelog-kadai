package com.example.nagoyameshi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.form.CategoryEditForm;
import com.example.nagoyameshi.form.CategoryRegisterForm;
import com.example.nagoyameshi.repository.CategoryRepository;



@Service
public class CategoryService {
   private final CategoryRepository categoryRepository;

   public CategoryService(CategoryRepository categoryRepository) {
       this.categoryRepository = categoryRepository;
   }

   // すべてのカテゴリをページングされた状態で取得する
   public Page<Category> findAllCategorys(Pageable pageable) {
       return categoryRepository.findAll(pageable);
   }

   // 指定されたキーワードをカテゴリ名に含むカテゴリを、ページングされた状態で取得する
   public Page<Category> findCategoryByNameLike(String keyword, Pageable pageable) {
       return categoryRepository.findByNameLike("%" + keyword + "%", pageable);
   }

   // 指定したidを持つカテゴリを取得する
   public Optional<Category> findCategoryById(Integer id) {
       return categoryRepository.findById(id);
   }

   // カテゴリのレコード数を取得する
   public long countCategorys() {
       return categoryRepository.count();
   }

   // idが最も大きいカテゴリを取得する
   public Category findFirstCategoryByOrderByIdDesc() {
       return categoryRepository.findFirstByOrderByIdDesc();
   }
   
   // カテゴリ名一覧を取得する
   public List<String> getAllCategoryNames() {
	    return categoryRepository.findAll()
	            .stream()
	            .map(Category::getName)
	            .toList();
	}
   // カテゴリ一覧を取得   
   public List<Category> getAllCategories() {
	    return categoryRepository.findAll();
	}
   

   @Transactional
   public void createCategory(CategoryRegisterForm categoryRegisterForm) {
       Category category = new Category();
       category.setName(categoryRegisterForm.getName());
 
       categoryRepository.save(category);
   }

   @Transactional
   public void updateCategory(CategoryEditForm categoryEditForm) {
	   Category category = categoryRepository.getReferenceById(categoryEditForm.getId());
       category.setName(categoryEditForm.getName());


       categoryRepository.save(category);
   }

   @Transactional
   public void deleteCategory(Category category) {
       categoryRepository.delete(category);
   }

}
