package eu.siacs.conversations.application;

import android.app.Application;

public class CustomApplication extends Application {

    private static CustomApplication customApplication;

    private String userPassword;

    public CustomApplication getInstance(){
        return this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }
}
