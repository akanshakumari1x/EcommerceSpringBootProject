package com.akan.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Data
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderAddress {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	private String firstName;
	
	private String lastName;
	
	private String email;
	
	private String mobileNo;
	
	private String address;
	
	private String city;
	
	private String state;
	
	private String pincode;
	
}
