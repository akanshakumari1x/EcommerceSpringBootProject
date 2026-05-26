package com.akan.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.akan.model.OrderRequest;
import com.akan.model.ProductOrder;


public interface OrderService {

	public void saveOrder(Integer userid, OrderRequest orderRequest) throws Exception;
	
	public List<ProductOrder> getOrderByUser(Integer userId);
	
	public ProductOrder updateOrderStatus(Integer id, String status);
	
	public List<ProductOrder> getAllOrders();
	
	public ProductOrder getOrdersByOrderId(String orderId);
	
	public Page<ProductOrder> getAllOrdersPagiantion(Integer pageNo, Integer pageSize);
	
	
	
}
