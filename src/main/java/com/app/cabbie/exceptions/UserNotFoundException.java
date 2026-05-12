package com.app.cabbie.exceptions;

public class UserNotFoundException extends RuntimeException{

    public UserNotFoundException(String message){
      super(message);
    }

}
