package com.akan.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import com.akan.model.Category;
import com.akan.model.Product;
import com.akan.model.UserDtls;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.akan.service.CartService;
import com.akan.service.CategoryService;
import com.akan.service.ProductService;
import com.akan.service.UserService;
import com.akan.util.CommonUtil;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private CommonUtil commonUtil;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private CartService cartService;
	
	
	@ModelAttribute
	public void getUserDetails(Principal p, Model m) {
		if(p!=null) {
			String email = p.getName();
			UserDtls userDtls = userService.getUserByEmail(email);
			m.addAttribute("user",userDtls);
			Integer countCart = cartService.getCountCart(userDtls.getId());
			m.addAttribute("countCart",countCart);
		}
		
		List<Category> allActiveCategory =	categoryService.getAllActiveCategory();
	    m.addAttribute("category", allActiveCategory);
	}
	    
	   @GetMapping("/")
	   public String index(Model m) {
		   List<Category> allActiveCategory =	categoryService.getAllActiveCategory().stream()
				   .sorted((c1,c2)->c2.getId().compareTo(c1.getId()))
				   .limit(6).toList();
		   List<Product> allActiveProduct  = productService.getAllActiveProduct("").stream()
				   .sorted((p1,p2)->p2.getId().compareTo(p1.getId())).
				   limit(8).toList();
		   m.addAttribute("category", allActiveCategory);
		   m.addAttribute("products",allActiveProduct);
		   return "index";
	   }
	    @GetMapping("/signin")
	    public String login() {
	        return "login"; // renders templates/index.html
	    }
	    
	    @GetMapping("/register")
	    public String register() {
	        return "register"; // renders templates/index.html
	    }
	    
	    @GetMapping("/products")
	    public String products(Model m, @RequestParam(value ="category", defaultValue ="") String category,
	    		@RequestParam(name="pageNo", defaultValue ="0") Integer pageNo,
	    		@RequestParam (name ="pageSize", defaultValue = "2") Integer pageSize)  {
	    	
	    	List<Category> categories = categoryService.getAllActiveCategory();
	    	m.addAttribute("paramValue", category);
	    	m.addAttribute("categories", categories);
	    	
//	    	List<Product> products = productService.getAllActiveProduct(category);
//	    	m.addAttribute("products", products);
	    	
	    	Page<Product> page = productService.getAllActiveProductPagination(pageNo, pageSize, category);
	    	List<Product> products = page.getContent();
	    	m.addAttribute("products", products);
	    	m.addAttribute("productsSize", products.size());
	    	m.addAttribute("pageNo", page.getNumber());
	    	m.addAttribute("pageSize", pageSize);
	    	m.addAttribute("totalElements", page.getTotalElements());
	    	m.addAttribute("totalPages", page.getTotalPages());
	    	m.addAttribute("isFirst",page.isFirst());
	    	m.addAttribute("isLast",page.isLast());
	        return "product"; // renders templates/index.html
	    }
	    
	    @GetMapping("/product/{id}")
	    public String product(@PathVariable int id, Model m) {
	    	Product productById = productService.getProductById(id);
	    	m.addAttribute("product", productById);
	        return "view_product.html"; // renders templates/index.html
	    }
	    
	    @PostMapping("/saveUser")
	    public String saveUser(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile file, HttpSession session) throws IOException {
	    	
	    	Boolean existsEmail = userService.existEmail(user.getEmail());
	    	
	    	if(existsEmail)
	    	{
	    		System.out.println(" email "+ existsEmail);
	    	   session.setAttribute("errorMsg", "Email already exists");
	    	}else { 		
	    		String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
		    	user.setProfileImage(imageName);
		    	UserDtls saveUser = userService.saveUser(user);
		    	
		    	if(!ObjectUtils.isEmpty(saveUser)) 
		    	{
		    		if(!file.isEmpty()) {
		    			File saveFile = new ClassPathResource("static/img").getFile();
						Path path = Paths.get(saveFile.getAbsolutePath() + File.separator+ "profile_img" +
		    			File.separator + file.getOriginalFilename());
						Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);	
		    		}
		    		session.setAttribute("successMsg", "user saved successfully");
		    	}else {
		    		session.setAttribute("errorMsg", "something went wrong");
		    	}
	    	}
		 return "redirect:/register";
	    }
	    
	    
	    //forget password
	    
	    @GetMapping("/forgot-password")
	    public String showForgotPassword() {
			return "forgot_password.html";
	    }
	    
	    
	    @PostMapping("/forgot-password")
	    public String processForgotPassword(@RequestParam String email, HttpSession session, HttpServletRequest request) throws UnsupportedEncodingException, MessagingException {
	    	
	    	UserDtls userByEmail = userService.getUserByEmail(email);
	    	
	    	if(ObjectUtils.isEmpty(userByEmail)) {
	    		session.setAttribute("errorMsg", "invalidEmail");
	    	}else {
	    	 String resetToken = UUID.randomUUID().toString();
	    	 userService.updateUserResetToken(email, resetToken);
	    	 
	    	 //Generate Url  :http://localhost:8080/reset-password?tokens=njjij
	    	 
	    	    String url = CommonUtil.generateUrl(request) + "/reset-password?token="+resetToken ;
	    	 
	    		Boolean sendMail = commonUtil.sendMail(url,email);
	    		
	    		if(sendMail) {
	    			session.setAttribute("successMsg", "please check your mail. Password Reset Link sent");
	    		}else {
	    			session.setAttribute("errorMsg", "Something went wrong!"); }
	           	}
			return "redirect:/forgot-password";
	    }
	    
	    @GetMapping("/reset-password")
	    public String showResetPassword(@RequestParam String token, HttpSession session, Model m) {
	    
	     UserDtls userByToken = userService.getUserBytoken(token);
	     
	      if(ObjectUtils.isEmpty(userByToken)) {
	    	  m.addAttribute("msg","Your link is invalid or expired!!");
	    	  return "message"; 
	      }
	       m.addAttribute("token", token);
	       return "reset_password";
	    }
	    
	   @PostMapping("/reset-password")
	   public String ResetPassword(@RequestParam String token, @RequestParam String password, HttpSession session, Model m) {
	   
	    UserDtls userByToken = userService.getUserBytoken(token);
	    
	      if(userByToken == null) {
	    	  m.addAttribute("errorMsg", "your link is invalid or expired !!");
	    	  return "message";
	      }else {
	    	  userByToken.setPassword(passwordEncoder.encode(password));
	    	  userByToken.setResetToken(null);
	    	  userService.updateUser(userByToken);
	    	  session.setAttribute("successMsg", "Password change Successfully");
	    	  m.addAttribute("msg", "Password change Successfully");
	    	  return "message";
	      }
	    	
	    }  
	   
	   @GetMapping("/search")
	   public String searchProduct(@RequestParam String ch, Model m) {
		   
		   List<Product>searchProduct = productService.searchProduct(ch);
		   m.addAttribute("products", searchProduct);
		   
		   List<Category> categories = categoryService.getAllActiveCategory();
	       m.addAttribute("categories", categories);
		   return "product";
	   }
	   
	   
	   
	   
	}



