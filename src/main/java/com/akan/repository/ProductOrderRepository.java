package com.akan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.akan.model.ProductOrder;

public interface ProductOrderRepository extends JpaRepository<ProductOrder, Integer> {

	public List<ProductOrder>  findByUserId(Integer userId);
	
	ProductOrder findByOrderId(String orderId);
	

}
