package com.soutvoid.ProjectSozy;

/**
 * Created by andrew on 25.01.15.
 */
public class LoginData {


    private String Address;
    private String User;
    private String Password;


    public String getUser() {
        return User;
    }
    public String getAddress() {
        return Address;
    }
    public String getPassword() {
        return Password;
    }
    public void setUser(String input) {
        User = input;
    }
    public void setAddress(String input) {
        Address = input;
    }
    public void setPassword(String input) {
        Password = input;
    }
}
