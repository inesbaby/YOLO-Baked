package com.example.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.entity.Role;
import com.example.entity.RoleName;
import com.example.entity.User;
import com.example.exception.AppException;
import com.example.exception.ResourceNotFoundException;
import com.example.payload.ApiResponse;
import com.example.payload.JwtAuthenticationResponse;
import com.example.payload.LoginRequest;
import com.example.payload.SignUpRequest;
import com.example.repository.RoleRepository;
import com.example.repository.UserRepository;
import com.example.security.JwtTokenProvider;
import com.example.service.NotificationService;
import com.example.util.CookieUtil;

//https://www.callicoder.com/spring-boot-spring-security-jwt-mysql-react-app-part-2/
@RestController
@RequestMapping("/api/auth")

//try1
public class AuthController {

	private static final Logger logger = LoggerFactory.getLogger(DiaryController.class);

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UserRepository userRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	JwtTokenProvider tokenProvider;

	@Autowired
	NotificationService notificationService;

	@Autowired
	CookieUtil cookieUtil;

	@PreAuthorize("hasRole('USER')")
	@GetMapping("/private")
	public String privateArea() {
		System.out.println("privateArea");
		return "bojour";

	}

	// 登入
	// Session Management using Spring Session with JDBC DataStore
	// https://sivalabs.in/2018/02/session-management-using-spring-session-jdbc-datastore/
	@PostMapping("/login")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request,
			HttpServletResponse response) {

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		System.out.println(authentication);

		System.out.println(authentication.getAuthorities());// [ROLE_USER]
		String jwt = tokenProvider.generateToken(authentication);

		cookieUtil.setLoginTokenCookie(jwt, response);

		return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
	}

	// 註冊
	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
//		String [] arrayRole = new String[2];
//		arrayRole[0] = "ROLE_USER";
//		arrayRole[1] = "ROLE_ADMIN";
//		for (int i = 0; i <= 1; i++) {
//			Role setRole = new Role();
//			setRole.setName(arrayRole[i]);
//			roleRepository.save(setRole);
//		}
		if (userRepository.existsByUsername(signUpRequest.getUsername())) {
			return new ResponseEntity(new ApiResponse(false, "Username is already taken!"), HttpStatus.BAD_REQUEST);
		}

		if (userRepository.existsByEmail(signUpRequest.getEmail())) {
			return new ResponseEntity(new ApiResponse(false, "Email Address already in use!"), HttpStatus.BAD_REQUEST);
		}

		User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(), signUpRequest.getPassword());

		user.setPassword(passwordEncoder.encode(user.getPassword()));

		Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
				.orElseThrow(() -> new AppException("User Role not set."));

		System.out.println(userRole);

		user.setRoles(Collections.singleton(userRole));
		System.out.println(Collections.singleton(userRole));// [com.example.entity.Role@3eb19954]

		User result = userRepository.save(user);

		URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/user/{username}")
				.buildAndExpand(result.getUsername()).toUri();
		System.out.println(location);
		// http://localhost:8080/api/user/testin221111

		// 信箱寄送
//		try {
//			notificationService.sendNotification(user);
//		} catch (MailException e) {
//			logger.info("Error sending email" + e.getMessage());
//		}

		return ResponseEntity.created(location).body(new ApiResponse(true, "User registered successfully"));
	}

	// 更改使用者密碼
	@PutMapping("user/{username}")
	public User update(@PathVariable String username, @RequestBody User user) {
		return userRepository.findByUsername(username).map(users -> {
			users.setPassword(passwordEncoder.encode(user.getPassword()));
			return userRepository.save(users);
		}).orElseThrow(() -> new ResourceNotFoundException("Username" + username + "not found", null, user));

	}
	
	

	// 刪除
	@RequestMapping(value = "user/{username}", method = RequestMethod.DELETE)
	public void delete(@PathVariable String username) {
		userRepository.deleteById(username);

	}

}
