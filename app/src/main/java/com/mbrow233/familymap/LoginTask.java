package com.mbrow233.familymap;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.mbrow233.familymap.net.ServerProxy;

import Request.LoginRequest;
import Result.LoginResult;

class LoginTask implements Runnable {
    private final Handler messageHandler;
    private final LoginRequest loginRequest;
    private final String serverHostText;
    private final String serverPortText;
    private String SUCCEED_KEY = "succeed_key";
    private String TAG = "LoginTask";

    public LoginTask(Handler messageHandler, LoginRequest request, String serverHostText, String serverPortText) {
        this.messageHandler = messageHandler;
        this.loginRequest = request;
        this.serverHostText = serverHostText;
        this.serverPortText = serverPortText;
    }

    @Override
    public void run() {
        ServerProxy.setServerHostName(serverHostText);
        ServerProxy.setServerPortNumber(Integer.parseInt(serverPortText));
        LoginResult result = ServerProxy.login(loginRequest);
        if (result.isSuccess()) {
            sendMessage(true, result);
        }
        else {
            sendMessage(false, result);
        }

    }

    private void sendMessage(boolean succeeded, LoginResult result) {
        Message message = Message.obtain();

        Bundle messageBundle = new Bundle();
        messageBundle.putBoolean(SUCCEED_KEY, succeeded);
        message.setData(messageBundle);
        message.obj = result;

        messageHandler.sendMessage(message);
    }
}