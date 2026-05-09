package com.asrtek.automation.models;

/**
 * Represents a test user entry from Users.json.
 */
public class UserModel {

    private String userType;
    private String userName;
    private String pass;

    public UserModel() {}

    public UserModel(String userType, String userName, String pass) {
        this.userType = userType;
        this.userName = userName;
        this.pass = pass;
    }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getPass() { return pass; }
    public void setPass(String pass) { this.pass = pass; }

    @Override
    public String toString() {
        return "UserModel{userType='" + userType + "', userName='" + userName + "'}";
    }
}
