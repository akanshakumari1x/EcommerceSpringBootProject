package com.akan.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.akan.model.Category;
import com.akan.model.Product;
import com.akan.repository.CategoryRepository;
import com.akan.service.CategoryService;

@Service
public class CategoryServiceImpl implements CategoryService{

	@Autowired
	private CategoryRepository catrepo;

	@Override
	public Category saveCategory(Category category) {
		return catrepo.save(category);
	}
	
	@Override
	public List<Category>getAllCategory() {
		return catrepo.findAll();
	}

	@Override
	public Boolean existCategory(String catName) {
		return catrepo.existsByName(catName);
	}

	@Override
	public Boolean deleteCategory(int id) {
		Category category = catrepo.findById(id).orElse(null);
		if(!ObjectUtils.isEmpty(category)){
			catrepo.delete(category);
			return true;
		}
		return false;
	}

	@Override
	public Category getCategoryById(int id) {
		 Category category = catrepo.findById(id).orElse(null);
		 return category;
	}

	@Override
	public List<Category> getAllActiveCategory() {
		List<Category> categories = catrepo.findByIsActiveTrue();
		return categories;
	}

	@Override
	public Page<Category> getAllCategoryPagination(Integer pageNo, Integer pageSize) {
		
		Pageable pageable =   PageRequest.of(pageNo, pageSize);
		return catrepo.findAll(pageable);
	}



}
