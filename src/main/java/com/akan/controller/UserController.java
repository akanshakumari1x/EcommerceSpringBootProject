package com.akan.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.akan.model.Cart;
import com.akan.model.Category;
import com.akan.model.OrderRequest;
import com.akan.model.ProductOrder;
import com.akan.model.UserDtls;
import com.akan.service.CartService;
import com.akan.service.CategoryService;
import com.akan.service.OrderService;
import com.akan.service.UserService;
import com.akan.util.CommonUtil;
import com.akan.util.OrderStatus;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserService userService;	
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private CartService cartService;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private CommonUtil commonUtil;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	
	@GetMapping("/")
	public String index(){
		return "user/home";
	}
	
	@ModelAttribute
	public void getUserDetails(Principal p, Model m) {
		if(p!=null) {
			String email = p.getName();
			UserDtls userDtls = userService.getUserByEmail(email);
			m.addAttribute("user",userDtls);
			
			Integer countCart = cartService.getCountCart(userDtls.getId());
			System.out.println(" model "+ countCart);
			m.addAttribute("countCart",countCart);
		}
		List<Category> allActiveCategory =	categoryService.getAllActiveCategory();
	    m.addAttribute("category", allActiveCategory);
	}
	
	@GetMapping("/addCart")
	public String addToCart(@RequestParam Integer pid, @RequestParam Integer uid, HttpSession session ) {
		Cart saveCart = cartService.saveCart(pid, uid);
		System.out.println(" pid "+ pid + " uid" + uid);
		if(ObjectUtils.isEmpty(saveCart)) {
			session.setAttribute("errorMsg", "Product Add to Cart failed. !!");
		}else {
			session.setAttribute("successMsg", "Product Added to Cart. !!");
		}
		return "redirect:/product/" + pid;
		
	}
	
	@GetMapping("/cart")
	public String loadCartPage(Principal p, Model m){
		
		UserDtls user = getLoggedInUserDetails(p);
		List<Cart>carts = cartService.getCartsByUser(user.getId());
		m.addAttribute("carts", carts);
		
		if(carts.size() > 0) {
			Double totalOrderPrice = carts.get(carts.size()-1).getTotalOrderPrice();
			m.addAttribute("totalOrderPrice", totalOrderPrice);
		}
		return "/user/cart";
	}
	
	@GetMapping("/cartQuantityUpdate")
	public String updateCartQuantity(@RequestParam String sy, @RequestParam Integer cid) {	
		cartService.updateQuantity(sy, cid);
		return "redirect:/user/cart";
	}
	
	
	public UserDtls getLoggedInUserDetails(Principal p) {
		String email = p.getName();
		UserDtls userDtls = userService.getUserByEmail(email);
		return userDtls;
	}
	
	
	@GetMapping("/orders")
	public String orderPage(Principal p, Model m) {
		UserDtls user = getLoggedInUserDetails(p);
		List<Cart>carts = cartService.getCartsByUser(user.getId());
		m.addAttribute("carts", carts);
		
		if(carts.size() > 0) {
			Double orderPrice = carts.get(carts.size()-1).getTotalOrderPrice() +250+100 ;
			Double totalOrderPrice = carts.get(carts.size()-1).getTotalOrderPrice();
			m.addAttribute("orderPrice",orderPrice);
			m.addAttribute("totalOrderPrice", totalOrderPrice);
		}
		return"/user/order";
	}
	
	@PostMapping("/save-order")
	public String saveOrder(@ModelAttribute OrderRequest request, Principal p ) throws Exception {
		UserDtls user = getLoggedInUserDetails(p);
		orderService.saveOrder(user.getId(), request);
		return "/user/success";
	}
	
	@GetMapping("/success")
	public String loadSuccess() {
		return "/user/success";
	}
	
	@GetMapping("/user-orders")
	public String myOrder(Model m, Principal p) {
		UserDtls loginUser = getLoggedInUserDetails(p);
		List<ProductOrder> orders = orderService.getOrderByUser(loginUser.getId());
		m.addAttribute("orders", orders);
		return "/user/my_orders";
	}
	
	
	@GetMapping("/update-status")
	public String updateOrderStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession session) {
		OrderStatus [] values = OrderStatus.values();
		String status = null;
		for(OrderStatus orderSt:values) {
			if(orderSt.getId().equals(st)) {
				status = orderSt.getName();
			}
		}
		ProductOrder updateOrder = orderService.updateOrderStatus(id, status);
		try {
			commonUtil.sendMailForProductOrder(updateOrder, status);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(!ObjectUtils.isEmpty(updateOrder)) {
			session.setAttribute("successMsg", "Status Updated !!");
		}else {
			session.setAttribute("errorMsg", "Status not Updated  !!");
		}
		return "redirect:/user/user-orders";
	}
	
	@GetMapping("/profile")
	public String profile()
	{
		return"/user/profile";
	}
	
	@PostMapping("/update-profile")
	public String updateProfile(@ModelAttribute UserDtls user, @RequestParam MultipartFile img, HttpSession session)
	{
		UserDtls updaateUserProfile = userService.updateUserProfile(user, img);
		
		if(ObjectUtils.isEmpty(updaateUserProfile)) {
			session.setAttribute("errorMsg", "Profile not Updated  !!");
		}else {
			session.setAttribute("successMsg", "Profile Updated  !!");
		}
		return"redirect:/user/profile";
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam String newPassword, @RequestParam String currentPassword, Principal p, HttpSession session) {
		
		UserDtls loggedInUserDetails = getLoggedInUserDetails(p);
		
		boolean matches = passwordEncoder.matches(currentPassword, loggedInUserDetails.getPassword());
		
		if(matches) {
			String encodePassword = passwordEncoder.encode(newPassword);
			loggedInUserDetails.setPassword(encodePassword);
			UserDtls updateUser = userService.updateUser(loggedInUserDetails);
			
			if(ObjectUtils.isEmpty(updateUser)) {
				session.setAttribute("errorMsg", "Password not Updated !! Error in server.");
			}else {
				session.setAttribute("successMsg", "Password Updated  !!");
			 }	
			}else {
				session.setAttribute("errorMsg", "Current Password incorrect !!");
			}
		
		return"redirect:/user/profile";
		
	}
	
	
}
