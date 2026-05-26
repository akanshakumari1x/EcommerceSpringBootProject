package com.akan.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.akan.model.Product;

public interface ProductService {

	public Product saveProduct(Product product);
	
	public List<Product> getAllProduct();
	
	public Boolean deleteProduct(int id);

	public Product getProductById(int id);
	
	public Product updateProduct(Product Product, MultipartFile image);
	
	public List<Product> getAllActiveProduct(String category);
	
	public List<Product>searchProduct(String ch);
	
	public Page<Product> getAllActiveProductPagination(Integer pageNo, Integer pageSize, String category);

	public Page<Product>searchProductPagination(Integer pageNo, Integer pageSize, String ch);
	
	public Page<Product> getAllProductPagination(Integer pageNo, Integer pageSize);
}
