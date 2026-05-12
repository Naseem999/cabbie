package com.app.cabbie.service;

import com.app.cabbie.dto.UserLoginDTO;
import com.app.cabbie.dto.UserRegisterDTO;
import com.app.cabbie.dto.UserResiterationResponseDTO;
import com.app.cabbie.enums.RoleType;
import com.app.cabbie.exceptions.UserNotFoundException;
import com.app.cabbie.model.User;
import com.app.cabbie.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public UserService(ModelMapper modelMapper, UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager){
        this.userRepository=userRepository;
        this.modelMapper=modelMapper;
        this.passwordEncoder=passwordEncoder;
        this.authenticationManager=authenticationManager;
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

        return  modelMapper.map(userRepository.save(newUser),UserResiterationResponseDTO.class);
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
    public UserResiterationResponseDTO updateUser(Long id, UserRegisterDTO userRegisterDTO) {

        User newUpdateUser=userRepository.findById(id).orElseThrow(()-> new UserNotFoundException("User Not Found With Id:"+ id));

      if(userRegisterDTO.getName()!=null){
          newUpdateUser.setName(userRegisterDTO.getName());
      }
      if(userRegisterDTO.getPhone()!=null){
          newUpdateUser.setPhone(userRegisterDTO.getPhone());
      }
      if(userRegisterDTO.getPassword()!=null){
         newUpdateUser.setPassword(passwordEncoder.encode(userRegisterDTO.getPassword()));
      }

      if(!userRegisterDTO.getRole().name().isEmpty()){
          newUpdateUser.setRole(userRegisterDTO.getRole());
      }

      return  modelMapper.map(userRepository.save(newUpdateUser),UserResiterationResponseDTO.class);
    }



}
