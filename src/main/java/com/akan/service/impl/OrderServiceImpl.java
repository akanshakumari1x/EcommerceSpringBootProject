package com.akan.service.impl;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.akan.model.Cart;
import com.akan.model.OrderAddress;
import com.akan.model.OrderRequest;
import com.akan.model.ProductOrder;
import com.akan.repository.CartRepository;
import com.akan.repository.ProductOrderRepository;
import com.akan.service.OrderService;
import com.akan.util.CommonUtil;
import com.akan.util.OrderStatus;

import jakarta.mail.MessagingException;

@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private ProductOrderRepository orderRepo;
	
	@Autowired
	private CartRepository cartRepo;
	
	@Autowired
	private CommonUtil commonUtil;
	
	@Override
	public void saveOrder(Integer userid, OrderRequest orderRequest) throws Exception {
		
		List<Cart> carts = cartRepo.findByUserId(userid);
		for(Cart cart: carts) {
			ProductOrder order = new ProductOrder();
			order.setOrderId(UUID.randomUUID().toString());
			
			order.setOrderDate(LocalDate.now());
			order.setProduct(cart.getProduct());
			order.setPrice(cart.getProduct().getDiscountPrice());
			order.setQuantity(cart.getQuantity());
			order.setUser(cart.getUser());
			
			order.setStatus(OrderStatus.IN_PROGRESS.getName());
			order.setPaymentType(orderRequest.getPaymentType());
			
			OrderAddress address = new OrderAddress();
			address.setFirstName(orderRequest.getFirstName());
			address.setLastName(orderRequest.getLastName());
			address.setEmail(orderRequest.getEmail());
			address.setMobileNo(orderRequest.getMobileNo());
			address.setAddress(orderRequest.getAddress());
			address.setCity(orderRequest.getCity());
			address.setState(orderRequest.getState());
			address.setPincode(orderRequest.getPincode());
			order.setOrderAddress(address);
			
			ProductOrder saveOrder = orderRepo.save(order);
			commonUtil.sendMailForProductOrder(saveOrder, "success");
		}
	}

	@Override
	public List<ProductOrder> getOrderByUser(Integer userId) {
		List<ProductOrder> orders = orderRepo.findByUserId(userId);
		return orders;
	}

	@Override
	public ProductOrder updateOrderStatus(Integer id, String status) {
	    Optional<ProductOrder> findById =  orderRepo.findById(id);
	    if(findById.isPresent()) {
	    	ProductOrder productOrder = findById.get();
	    	productOrder.setStatus(status);
	    	ProductOrder updateOrder = orderRepo.save(productOrder);
	    	return updateOrder;
	    }
		return null;
	}


	@Override
	public ProductOrder getOrdersByOrderId(String orderId) {
		return  orderRepo.findByOrderId(orderId);
	}

	@Override
	public Page<ProductOrder> getAllOrdersPagiantion(Integer pageNo, Integer pageSize) {
		Pageable pageable =   PageRequest.of(pageNo, pageSize);
		return orderRepo.findAll(pageable);
	}

	@Override
	public List<ProductOrder> getAllOrders() {
		orderRepo.findAll();
		return null;
	}

}
