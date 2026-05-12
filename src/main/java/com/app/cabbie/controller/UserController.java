package com.app.cabbie.controller;

import com.app.cabbie.dto.UserLoginDTO;
import com.app.cabbie.dto.UserRegisterDTO;
import com.app.cabbie.dto.UserResiterationResponseDTO;
import com.app.cabbie.model.User;
import com.app.cabbie.service.JWTService;
import com.app.cabbie.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Role;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JWTService jwtService;

    @PostMapping("/register")
    public ResponseEntity<UserResiterationResponseDTO> createUser(@RequestBody UserRegisterDTO userRegisterDTO){
      UserResiterationResponseDTO userResponse=userService.registerUser(userRegisterDTO);
      return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserLoginDTO dto){
        User user=userService.userLogin(dto);
        String jwtToken=jwtService.generateToken(user);
        return new ResponseEntity<>(jwtToken, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('PASSENGER','DRIVER','ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id){
        User user=userService.getUserById(id);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<User> deleteUserById(@PathVariable Long id){
        User user=userService.deleteUserById(id);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }


}
