package eu.siacs.conversations.application;

import android.app.Application;

import eu.siacs.conversations.entities.Account;

public class CustomApplication extends Application {

    private static CustomApplication customApplication;

    private static Account userAccount;

    public CustomApplication getInstance(){
        return this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static Account getUserAccount() {
        return userAccount;
    }

    public static void setUserAccount(Account account) {
        userAccount = account;
    }
}
