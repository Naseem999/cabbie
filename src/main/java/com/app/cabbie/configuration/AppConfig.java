package com.app.cabbie.configuration;

import com.app.cabbie.repository.UserRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {

    @Autowired
    UserRepository userRepository;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;


    @Bean
    public ModelMapper modelMapper(){
        return  new ModelMapper();
    }


    @Bean
    PasswordEncoder passwordEncoder(){
        return  new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)throws  Exception{
        return authenticationConfiguration.getAuthenticationManager();

    }

    @Bean
    public UserDetailsService userDetailsService(){
      return username -> userRepository.findByEmail(username).get();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider daoAuthenticationProvider=new DaoAuthenticationProvider(userDetailsService());
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }


    @Bean
    public RazorpayClient  razorpayClient() throws RazorpayException {
        return new RazorpayClient(keyId, keySecret);
    }


}
