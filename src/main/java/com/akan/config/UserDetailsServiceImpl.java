package com.akan.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.akan.model.UserDtls;
import com.akan.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private UserRepository userRepo;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
         
		UserDtls user = userRepo.findByEmail(username);

        if(user == null) {
        	throw new UsernameNotFoundException("user not found");
        }
		return new CustomUser(user);
		
	}

}
