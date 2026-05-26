package com.akan.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.akan.model.Cart;
import com.akan.model.Product;
import com.akan.model.UserDtls;
import com.akan.repository.CartRepository;
import com.akan.repository.ProductRepository;
import com.akan.repository.UserRepository;
import com.akan.service.CartService;


@Service
public class CartServiceImpl implements CartService {
	
	@Autowired
	private CartRepository cartRepo;
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private ProductRepository prodRepo;
	
	@Override
	public Cart saveCart(Integer productId, Integer userId) {
		
		UserDtls userDtls = userRepo.findById(userId).get();
		Product product = prodRepo.findById(productId).get();
		
		Cart cartStatus  = cartRepo.findByProductIdAndUserId(productId, userId);
	    Cart cart = null;
	    
		if(ObjectUtils.isEmpty(cartStatus)) {
	    	cart = new Cart();
	    	cart.setProduct(product);
	    	cart.setUser(userDtls);
	    	cart.setQuantity(1);
	    	cart.setTotalPrice(1 * product.getDiscountPrice());
	    	
	    }else {
	    	cart = cartStatus;
	    	cart.setQuantity(cart.getQuantity() + 1);
	    	cart.setTotalPrice(cart.getQuantity() * cart.getProduct().getDiscountPrice());
	    }
		Cart saveCart = cartRepo.save(cart);
		return saveCart;
	}

	@Override
	public List<Cart> getCartsByUser(Integer userId) {
		List<Cart>carts = cartRepo.findByUserId(userId);

		Double totalOrderPrice = 0.0;
		List<Cart>updateCarts = new ArrayList<>();
		
		for(Cart c:carts) {
			
		 Double totalPrice = (c.getProduct().getDiscountPrice() * c.getQuantity());
		 c.setTotalPrice(totalPrice);
		 totalOrderPrice =  totalOrderPrice + totalPrice;
		 c.setTotalOrderPrice(totalOrderPrice);
		 updateCarts.add(c);
		 
		}
		
		return updateCarts;
	}

	@Override
	public Integer getCountCart(Integer userId) {
		Integer countByUserId = cartRepo.countByUserId(userId);
		System.out.println(" countByUserId "+ countByUserId);
		return countByUserId;
	}

	@Override
	public void updateQuantity(String sy, Integer cid) {
		
		Cart cart = cartRepo.findById(cid).get();
		int updateQuantity;
		
		if(sy.equalsIgnoreCase("de")) {
			updateQuantity =  cart.getQuantity() - 1;
			
			if(updateQuantity <= 0) {
				cartRepo.delete(cart);
			}else {
				cart.setQuantity(updateQuantity);
				cartRepo.save(cart);
			}
		}else {
				updateQuantity = cart.getQuantity() + 1;
				cart.setQuantity(updateQuantity);
				cartRepo.save(cart);
			}
	}

}
