package com.akan.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.akan.model.UserDtls;
import com.akan.repository.UserRepository;
import com.akan.service.UserService;
import com.akan.util.AppConstant;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Component
public class AuthFailureHandlerImpl  extends SimpleUrlAuthenticationFailureHandler{

	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private UserService userService;
	
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		
		String email = request.getParameter("username");
		UserDtls userDtls = userRepo.findByEmail(email);
		
		if(userDtls != null) {
			
		  if(userDtls.getIsEnable()) {
			if(userDtls.getAccountNonLocked()) {
				if(userDtls.getFailedAttempt()< AppConstant.ATTEMPT_TIME) {
					userService.increaseFailedAttempt(userDtls);
				} else {
					userService.userAccountLock(userDtls);
					exception = new LockedException("Your Account is locked !! failed attempt 3");
				}
			}else {
				
				if(userService.unlockAccountTimeExpired(userDtls)) {
					exception = new LockedException("Your Account is unlocked. login again");
				}else {
					exception = new LockedException("Your Account is locked!! try after sometime.");
				  }
			    }
		   } else {
			  exception = new LockedException("Your Account is inActive");
	    	}
		}else {
			 exception = new LockedException("Email Or Password is invalid!!");
		}
		
		super.setDefaultFailureUrl("/signin?error");
		super.onAuthenticationFailure(request, response, exception);
	}

	
	
	
	
}
