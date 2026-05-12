package com.app.cabbie.dto;

import com.app.cabbie.enums.RoleType;

import javax.management.relation.Role;
import java.util.List;
import java.util.Set;


public class UserRegisterDTO {

    private String name;
    private String email;
    private String phone;
    private String password;
    private RoleType role;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public RoleType getRole() {
        return role;
    }

    public void setRole(RoleType role) {
        this.role = role;
    }
}
