package com.akan.service.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.akan.model.Product;
import com.akan.repository.ProductRepository;
import com.akan.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService{

	@Autowired
	ProductRepository productRepo;
	
	
	@Override
	public Product saveProduct(Product product) {
		return productRepo.save(product);
	}

	@Override
	public List<Product> getAllProduct() {
		return productRepo.findAll();
	}

	@Override
	public Boolean deleteProduct(int id) {
		Product product = productRepo.findById(id).orElse(null);
		if(!ObjectUtils.isEmpty(product)){
			productRepo.delete(product);
			return true;
		}
	    return false;
	  }

	@Override
	public Product getProductById(int id) {
		Product product = productRepo.findById(id).orElse(null);
		 return product;
	}

	
	@Override
	public Product updateProduct(Product product, MultipartFile image) {
		Product dbproduct = getProductById(product.getId());
		String imagename = image.isEmpty() ? product.getImage(): image.getOriginalFilename();
		

		System.out.println("titlep "+product.getTitle());
			dbproduct.setTitle(product.getTitle());
			System.out.println("title "+dbproduct.getTitle());
			dbproduct.setDescription(product.getDescription());
			dbproduct.setCategory(product.getCategory());
			dbproduct.setPrice(product.getPrice());
			dbproduct.setStock(product.getStock());
			dbproduct.setImage(imagename);
			dbproduct.setIsActive(product.getIsActive());
			dbproduct.setDiscount(product.getDiscount());
			
			Double discount =  product.getPrice()*(product.getDiscount()/100.0);
			Double discountPrice = product.getPrice() -discount;
			dbproduct.setDiscountPrice(discountPrice);
			
		    Product updateProduct = productRepo.save(dbproduct);
		
			 if(!ObjectUtils.isEmpty(updateProduct)) {
				 if(!image.isEmpty()) {
				 try {
					File saveFile = new ClassPathResource("static/img").getFile();
					Path path = Paths.get(saveFile.getAbsolutePath() + File.separator+ "product_img" + File.separator + image.getOriginalFilename());
					Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				   }catch(Exception e) {
					e.printStackTrace(); }
			   } 
		       return product;
			 }
		return null;
	}

	@Override
	public List<Product> getAllActiveProduct(String category) {
		List<Product> products = null;
		if(ObjectUtils.isEmpty(category)) {
			products = productRepo.findByIsActiveTrue();
		  }else {
			  products = productRepo.findByCategory(category);
		  }
		return products;
	}

	@Override
	public List<Product> searchProduct(String ch) {
	return productRepo.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch);
	
	}

	@Override
	public Page<Product> getAllActiveProductPagination(Integer pageNo, Integer pageSize, String category) {

		Pageable pageable =   PageRequest.of(pageNo, pageSize);
		Page<Product> pageProduct = null;
		
		if(ObjectUtils.isEmpty(category)) {
			pageProduct = productRepo.findByIsActiveTrue(pageable);
		  }else {
			  pageProduct = productRepo.findByCategory(pageable, category);
		  }
		return pageProduct;
	}

	@Override
	public Page<Product> searchProductPagination(Integer pageNo, Integer pageSize, String ch) {
		
		Pageable pageable =   PageRequest.of(pageNo, pageSize);
		return productRepo.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch, pageable);
		
	}

	@Override
	public Page<Product> getAllProductPagination(Integer pageNo, Integer pageSize) {
		
		Pageable pageable =   PageRequest.of(pageNo, pageSize);
		return productRepo.findAll(pageable);
	}


	

}
