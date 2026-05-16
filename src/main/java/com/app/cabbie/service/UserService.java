package com.app.cabbie.service;

import com.app.cabbie.dto.UserLoginDTO;
import com.app.cabbie.dto.UserRegisterDTO;
import com.app.cabbie.dto.UserResiterationResponseDTO;
import com.app.cabbie.dto.UserUpdateDTO;
import com.app.cabbie.enums.RoleType;
import com.app.cabbie.exceptions.UserNotFoundException;
import com.app.cabbie.model.Driver;
import com.app.cabbie.model.User;
import com.app.cabbie.repository.DriverRepository;
import com.app.cabbie.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final DriverService driverService;

     @Autowired

    public UserService(ModelMapper modelMapper, UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, DriverService driverService){
        this.userRepository=userRepository;
        this.modelMapper=modelMapper;
        this.passwordEncoder=passwordEncoder;
        this.authenticationManager=authenticationManager;
        this.driverService=driverService;
    }

    @Transactional
    public UserResiterationResponseDTO registerUser(UserRegisterDTO userRegisterDTO) {

        userRepository.findByEmail(userRegisterDTO.getEmail())
                .ifPresent((user)->
                {
                    throw new RuntimeException("User With email => "+userRegisterDTO.getEmail()+" already exists");
                });


        User newUser=User.builder()
                .name(userRegisterDTO.getName())
                .email(userRegisterDTO.getEmail())
                .phone(userRegisterDTO.getPhone())
                .password(passwordEncoder.encode(userRegisterDTO.getPassword()))
                .role(userRegisterDTO.getRole())
                .build();

        User savedUser=userRepository.save(newUser);

        if(newUser.getRole()==RoleType.DRIVER && null != savedUser){
              driverService.createNewDriver(savedUser.getId());
        }
        return  modelMapper.map(savedUser,UserResiterationResponseDTO.class);
    }

    public User userLogin(UserLoginDTO userLoginDTO){
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userLoginDTO.getEmail(),userLoginDTO.getPassword()));
        return userRepository.findByEmail(userLoginDTO.getEmail()).get();
    }

    public User getUserById(Long id){
      return userRepository.findById(id).orElseThrow(()-> new UserNotFoundException("User Not Found With Id:"+ id));
    }

    @Transactional
    public User deleteUserById(Long id){
        User deletedUser= userRepository.findById(id).orElseThrow(()-> new UserNotFoundException("User Not Found With Id:"+ id));
        userRepository.deleteById(id);
        return deletedUser;
    }

    @Transactional
    public User updateUser(Long id, UserUpdateDTO userUpdateDTO) {

         User newUpdateUser=userRepository.findById(id).orElseThrow(()-> new UserNotFoundException("User Not Found With Id:"+ id));

      if(null != userUpdateDTO.getName()  && !userUpdateDTO.getName().isEmpty()){
          newUpdateUser.setName(userUpdateDTO.getName());
      }
      if(null != userUpdateDTO.getPhone() && !userUpdateDTO.getPhone().isEmpty()){
          newUpdateUser.setPhone(userUpdateDTO.getPhone());
      }
      if(null != userUpdateDTO.getPassword() && !userUpdateDTO.getPassword().isEmpty()){
         newUpdateUser.setPassword(passwordEncoder.encode(userUpdateDTO.getPassword()));
      }

      return  userRepository.save(newUpdateUser);
    }



}
