package com.akan.util;

import java.io.UnsupportedEncodingException;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.akan.model.ProductOrder;
import com.akan.model.UserDtls;
import com.akan.service.UserService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;


@Component
public class CommonUtil {

	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private UserService userService;
	
	
	public Boolean sendMail(String url, String reciepentEmail) throws UnsupportedEncodingException, MessagingException {
		
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		
		helper.setFrom("wishesakanshagupta918@gmail.com", "Shoping Cart");
		helper.setTo(reciepentEmail);
		
		String content = "<p>Hello,</p>" + "<p>You have requested to reset your password.</p>"
				+ "<p>Click the link below to change your password:</p>" + "<p><a href=\"" + url
				+ "\">Change my password</a></p>";
		
		helper.setSubject("Password Reset");
		helper.setText(content, true);
		mailSender.send(message);
		return true;
	}

	public static String generateUrl(HttpServletRequest request) {
		
		String siteUrl = request.getRequestURL().toString();
		
		return siteUrl.replace(request.getServletPath(),"");
	}
	
//	
	String msg = "<p>Dear Customer,</p>"              //  dummby msg for prank only  
			+ "<p>Thank you for shopping with us !!</p>" 
			+"<p>We are pleased to inform you that your order for The Smart LED TV has been successfully placed.</p>"
			+"<p><b>Order Details:</b></p>"
			+"<p>Product : Samsung 55-inch 4K Ultra HD Smart LED TV</p>"
			+"<p>Order Number : ORD789456</p>"
			+"<p>Order Date : 22 May 2026</p>"
			+"<p>Payment Method : Credit/Debit Card</p>"
			+"<p>Payment Status : Successful</p>" 
			+"<p>Estimated Delivery: 3–5 Business Days</p><br>"
			+"<p><b>Best Regards,</b></p>"
			+"<p>Customer Support Team</p>";
	
	
	//String msg =null;
	

	public Boolean sendMailForProductOrder(ProductOrder order, String status) throws UnsupportedEncodingException, MessagingException {
		
//		msg = "<p>Dear Customer,</p>"
//				+ "<p>Thank you for shopping with us !! <b>[[orderStatus]]</b>.</p>" +
//				"<p>We are pleased to inform you that your order for the [[productName]] has been successfully placed.</p><br>"+
//				"<p><b>Order Details:</b></p>"
//				+"<p>Name : [[productName]]</p>"
//				+"<p>Category : [[category]]</p>"
//				+"<p>Quantity : [[quantity]]</p>"
//				+"<p>Price : [[price]]</p>"
//				+"<p>Payment Type : [[paymentType]]</p>"
//				+"<p><b>Best Regards,</b></p>"
//				+"<p>Customer Support Team</p>";
				
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		
		helper.setFrom("wishesakanshagupta918@gmail.com", "Shopping Cart");
		System.out.println(" "+ order.getOrderAddress().getEmail());
		System.out.println(" email "+ order.getOrderAddress().getEmail());
		if(order.getOrderAddress().getEmail() == null || order.getOrderAddress().getEmail().trim().isEmpty()) {
		    throw new RuntimeException("Email address is empty");
		}
		
		helper.setTo("kumarankitx022@gmail.com");
		
		//helper.setFrom("kikot94479@hutudns.com", "FlipKart Cart");
		//helper.setTo("stylishraj9546@gmail.com"); //to whom you want to send
		
//		msg = msg.replace("[[name]]", order.getOrderAddress().getFirstName());
//		msg = msg.replace("[[orderStatus]]", status);
//		msg = msg.replace("[[productName]]", order.getProduct().getTitle());
//		msg = msg.replace("[[category]]", order.getProduct().getCategory());
//		msg = msg.replace("[[quantity]]", order.getQuantity().toString());
//		msg = msg.replace("[[price]]", order.getPrice().toString());
//		msg = msg.replace("[[paymentType]]", order.getPaymentType());
		
		helper.setSubject("Product Order Status");                      //change subject also
		helper.setText(msg, true);
		mailSender.send(message);
		return true;
	}
	
	public UserDtls getLoggedInUserDetails(Principal p) {
		String email = p.getName();
		UserDtls userDtls = userService.getUserByEmail(email);
		return userDtls;
	}
	
	
}
