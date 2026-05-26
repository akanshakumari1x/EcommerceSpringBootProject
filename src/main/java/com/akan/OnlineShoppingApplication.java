package com.akan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OnlineShoppingApplication {

	public static void main(String[] args) {
		
		 SpringApplication.run(OnlineShoppingApplication.class, args);
		 System.out.println(" online shopping ");
		 
		 // with this you can print all the beans present in your project
		 
			/*
			 * ConfigurableApplicationContext context =
			 * SpringApplication.run(OnlineShoppingApplication.class, args); String []
			 * allbeans = context.getBeanDefinitionNames();
			 * Arrays.stream(allbeans).sorted().forEach(System.out::println);
			 */ 
	}
}
