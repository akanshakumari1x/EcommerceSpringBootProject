package com.akan.controller;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.akan.model.Category;
import com.akan.model.Product;
import com.akan.model.ProductOrder;
import com.akan.model.UserDtls;
import com.akan.service.CartService;
import com.akan.service.CategoryService;
import com.akan.service.OrderService;
import com.akan.service.ProductService;
import com.akan.service.UserService;
import com.akan.util.CommonUtil;
import com.akan.util.OrderStatus;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private CartService cartService;
	
	@Autowired
	private OrderService orderService;
	
	
	@Autowired
	private CommonUtil commonUtil;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	
	@GetMapping("/")
	public String index() {
		return "admin/index";
	}
	
	@GetMapping("/loadAddProduct")
	public String loadAddProduct(Model model) {
		List<Category>categories =  categoryService.getAllCategory();
		model.addAttribute("categories",categories);
		return "admin/add_product";
	}
	
	@GetMapping("/category")
	public String category(Model m, @RequestParam(name="pageNo", defaultValue ="0") Integer pageNo,
    		@RequestParam (name ="pageSize", defaultValue = "10") Integer pageSize) {
		//m.addAttribute("categorys",categoryService.getAllActiveCategory());
		Page<Category> page = categoryService.getAllCategoryPagination(pageNo, pageSize);
		
		List<Category> categorys = page.getContent();
    	m.addAttribute("categorys", categorys);
    	
    	m.addAttribute("pageNo", page.getNumber());
    	m.addAttribute("pageSize", pageSize);
    	m.addAttribute("totalElements", page.getTotalElements());
    	m.addAttribute("totalPages", page.getTotalPages());
    	m.addAttribute("isFirst",page.isFirst());
    	m.addAttribute("isLast",page.isLast());
       
		return "admin/category";
	}
	
	
	@PostMapping("/saveCategory")
	public String saveCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file, HttpSession session) throws IOException {
		
		 if (category == null || category.getName() == null || category.getName().isEmpty()) {
		        session.setAttribute("errorMsg", "Category name is required");
		        System.out.println("cat" + category);
		        return "redirect:/admin/orders";
		    }
		
		String imageName = file != null ? file.getOriginalFilename() : "default.jpg";
		category.setImageName(imageName);
		
		Boolean existCategory = categoryService.existCategory(category.getName());
		if(existCategory) {
			session.setAttribute("errorMsg", "Category Name already exists");
		}else {
			Category saveCategory = categoryService.saveCategory(category);
			System.out.println(" akansha parent else");
			
			if(ObjectUtils.isEmpty(saveCategory)) {
				session.setAttribute("errorMsg", "fatalerror");
				System.out.println(" akansha if ");
			}else{
				System.out.println(" akansha else ");
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator+ "category_img" + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				session.setAttribute("successMsg", "saved successfully");
			}
		}
		return "redirect:/admin/category";
	}
	
	@GetMapping("/deleteCategory/{id}")
	public String deleteCategory(@PathVariable int id, HttpSession session) {
		Boolean deleteCategory =  categoryService.deleteCategory(id);
		if(deleteCategory) {
			session.setAttribute("successMsg", "category delete successfully");
		}else {
			session.setAttribute("errorMsg", "something went wrong");
			}
		return "redirect:/admin/category";
	}
	
	@GetMapping("/loadEditCategory/{id}")
	public String loadEditCategory(@PathVariable int id, Model model) {
		model.addAttribute("category", categoryService.getCategoryById(id));
		return "admin/edit_category";
	}
	
	@PostMapping("/updateCategory")
	public String updateCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file, HttpSession session) throws IOException {
		
		Category oldCategory = categoryService.getCategoryById(category.getId());
		String imagename = file.isEmpty() ? oldCategory.getImageName() :  file.getOriginalFilename();
		
		if(!ObjectUtils.isEmpty(category)) {
		 oldCategory.setName(category.getName());
		 oldCategory.setIsActive(category.getIsActive());
		 oldCategory.setImageName(imagename);	
		}
		
		Category updateCategory = categoryService.saveCategory(oldCategory);
		if(!ObjectUtils.isEmpty(updateCategory)) {
			if(!file.isEmpty()) {
			File saveFile = new ClassPathResource("static/img").getFile();
			Path path = Paths.get(saveFile.getAbsolutePath() + File.separator+ "category_img" + File.separator + file.getOriginalFilename());
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			session.setAttribute("successMsg", "category Updated successfully");
			}
		}else {
			session.setAttribute("errorMsg", "something went wrong");
		}
		return "redirect:/admin/loadEditCategory/" + category.getId();
	}
	
	@PostMapping("/saveProduct")
	public String saveProduct(@ModelAttribute Product product,  @RequestParam("file") MultipartFile file, HttpSession session) throws IOException {
		
		 String imagename = file.isEmpty() ? "default.jpg" :  file.getOriginalFilename();
		 product.setImage(imagename);
		 product.setDiscount(0);
		 product.setDiscountPrice(product.getPrice());
		
		 Product saveProduct = productService.saveProduct(product);
		 if(!ObjectUtils.isEmpty(saveProduct)) {
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator+ "product_img" + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				session.setAttribute("successMsg", "product added successfully");
			}else {
				session.setAttribute("errorMsg", "something went wrong"); 
				}
		 
		return "redirect:/admin/loadAddProduct";
	}
	
	@GetMapping("/products")
	public String loadViewProduct(Model m, @RequestParam(defaultValue ="") String ch,  @RequestParam(name="pageNo", defaultValue ="0") Integer pageNo,
    		@RequestParam (name ="pageSize", defaultValue = "10") Integer pageSize) {
		
//	List<Product> products = null;
//     if(ch !=null && ch.length() >0) {
//    	 products = productService.searchProduct(ch);
//     }else {
//    	 products = productService.getAllProduct();
//     }   
//       m.addAttribute("products", products);
       
       
       Page<Product> page = null;
       if(ch !=null && ch.length() >0) {
    	   page = productService.searchProductPagination(pageNo, pageSize, ch);
       }else {
    	   page = productService.getAllProductPagination(pageNo, pageSize);
       }   
	    m.addAttribute("products", page.getContent());
		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst",page.isFirst());
		m.addAttribute("isLast",page.isLast());
		
	   return "admin/products";
	}
	
	@GetMapping("/deleteProduct/{id}")
	public String deleteProduct(@PathVariable int  id, HttpSession session) {
		Boolean deleteProduct =  productService.deleteProduct(id);
		if(deleteProduct) {
			session.setAttribute("successMsg", "Product deleted successfully");
		}else {
			session.setAttribute("errorMsg", "something went wrong"); 
			}
	   return "redirect:/admin/products";
	}
	
	@GetMapping("/editProduct/{id}")
	public String editProduct(@PathVariable int id, Model model) {
		model.addAttribute("product", productService.getProductById(id));
		model.addAttribute("categories",categoryService.getAllCategory());
		return "admin/edit_product";
	}
	
	@PostMapping("/updateProduct")
	public String updateProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile image, HttpSession session) {
		System.out.println(" title"+ product.getTitle());
		if(product.getDiscount()<0 || product.getDiscount() >100) {
			session.setAttribute("errorMsg", "InvalidDiscount");
		}else {
	     Product updateProduct  = productService.updateProduct(product, image);
	     if(!ObjectUtils.isEmpty(updateProduct)) {
	    	 session.setAttribute("successMsg", "Product updated successfully");
	     }else {
	    	 session.setAttribute("errorMsg", "something went wrong");
	         }
		}
		return "redirect:/admin/editProduct/" +product.getId();
	}
	

	@GetMapping("/users")
	public String getAllUsers(Model m, @RequestParam Integer type) {
		List<UserDtls>users = null;
		
		if(type ==1) {
			users = userService.getUsers("ROLE_USER");
		}else {
			users = userService.getUsers("ROLE_USER");
		}
		m.addAttribute("userType", type);
		m.addAttribute("users",users);
		return "/admin/users";
	}
	
	@GetMapping("/updateSts")
	public String  updateUserAccountStatus(@RequestParam Boolean status, @RequestParam Integer id, @RequestParam Integer type, HttpSession session) {
		Boolean f = userService.updateAccountStatus(id, status);
		if(f) {
			session.setAttribute("successMsg", "Account status successfully");
		}else {
		session.setAttribute("errorMsg", "something went wrong");
		}
		return "redirect:/admin/users?type="+type;
	}
	
	@GetMapping("/orders")
	public String getAllOrders(Model m,  @RequestParam(name="pageNo", defaultValue ="0") Integer pageNo,
    		@RequestParam (name ="pageSize", defaultValue = "10") Integer pageSize) {
		
    		Page<ProductOrder>page = orderService.getAllOrdersPagiantion(pageNo, pageSize);
	    	m.addAttribute("orders", page.getContent());
	    	m.addAttribute("srch",false);
		
		    m.addAttribute("products", page.getContent());
			m.addAttribute("pageNo", page.getNumber());
			m.addAttribute("pageSize", pageSize);
			m.addAttribute("totalElements", page.getTotalElements());
			m.addAttribute("totalPages", page.getTotalPages());
			m.addAttribute("isFirst",page.isFirst());
			m.addAttribute("isLast",page.isLast());
		
		return "/admin/orders";
	}
	
	@PostMapping("/update-order-status")
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
		return "redirect:/admin/orders";
	}
	
	   @GetMapping("/search-order")
	   public String searchProduct(@RequestParam String orderId, Model m, 
			   HttpSession session,  @RequestParam(name="pageNo", defaultValue ="0") Integer pageNo,
	    		@RequestParam (name ="pageSize", defaultValue = "10") Integer pageSize) {
		   
		   if(orderId != null && orderId.length() >0 ) {
				  ProductOrder order = orderService.getOrdersByOrderId(orderId.trim());
				  if(ObjectUtils.isEmpty(order)) {
					  session.setAttribute("errorMsg", "Incorrect Order ID !!");
					  m.addAttribute("orderDtls",null);
				  }else {
					  m.addAttribute("orderDtls",order);
				  }
				  m.addAttribute("srch",true);
		   }else {
			   
//			List<ProductOrder>allOrders = orderService.getAllOrders();
//			m.addAttribute("orders", allOrders);
//			m.addAttribute("srch",false);
			
			Page<ProductOrder>page = orderService.getAllOrdersPagiantion(pageNo, pageSize);
			m.addAttribute("orders", page);
			m.addAttribute("srch",false);
			
			m.addAttribute("pageNo", page.getNumber());
			m.addAttribute("pageSize", pageSize);
			m.addAttribute("totalElements", page.getTotalElements());
			m.addAttribute("totalPages", page.getTotalPages());
			m.addAttribute("isFirst",page.isFirst());
			m.addAttribute("isLast",page.isLast());
			
			}
			  return "redirect:/admin/orders";
		   }
	   
	   
	   @GetMapping("/search")
	   public String searchProduct(@RequestParam String ch, Model m) {
		   List<Product>searchProduct = productService.searchProduct(ch);
		   m.addAttribute("products", searchProduct);
		   return "products";
	   }
	   
	   
	   @GetMapping("/add-admin")
	   public String loadAdminAdd() {
		   return "/admin/add_admin";
	   }
	   
	   @PostMapping("/save-admin")
	    public String saveAdmin(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile file, HttpSession session) throws IOException {
	    	
	    	String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
	    	user.setProfileImage(imageName);
	    	UserDtls saveUser = userService.saveAdmin(user);
	    	if(!ObjectUtils.isEmpty(saveUser)) 
	    	{
	    		if(!file.isEmpty()) {
	    			File saveFile = new ClassPathResource("static/img").getFile();
					Path path = Paths.get(saveFile.getAbsolutePath() + File.separator+ "profile_img" +
	    			File.separator + file.getOriginalFilename());
					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);	
	    		}
	    		session.setAttribute("successMsg", " Saved successfully");
	    	}else {
	    		session.setAttribute("errorMsg", "something went wrong");
	    	}
		 return "redirect:/admin/add-admin";
	    }
	   
	   @GetMapping("/profile")
	   public String profile() {
		   return "/admin/profile";
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
			return"redirect:/admin/profile";
		}
		
		@PostMapping("/change-password")
		public String changePassword(@RequestParam String newPassword, @RequestParam String currentPassword, Principal p, HttpSession session) {
			
			UserDtls loggedInUserDetails = commonUtil.getLoggedInUserDetails(p);
			
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
			
			return"redirect:/admin/profile";
			
		}
		
		   
	
}
