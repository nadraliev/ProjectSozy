package com.soutvoid.ProjectSozy;

/**
 * Created by andrew on 25.01.15.
 */
public class LoginData {


    private String Address;
    private String User;
    private String Password;


    protected String getUser() {
        return User;
    }
    protected String getAddress() {
        return Address;
    }
    protected String getPassword() {
        return Password;
    }
    protected void setUser(String input) {
        User = input;
    }
    protected void setAddress(String input) {
        Address = input;
    }
    protected void setPassword(String input) {
        Password = input;
    }
}
