package com.mbrow233.familymap;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.mbrow233.familymap.net.ServerProxy;

import Request.RegisterRequest;
import Result.RegisterResult;

class RegisterTask implements Runnable {
    private final Handler messageHandler;
    private final RegisterRequest registerRequest;
    private final String serverHostText;
    private final String serverPortText;
    private String SUCCEED_KEY = "succeed_key";
    private String TAG = "RegisterTask";

    public RegisterTask(Handler messageHandler, RegisterRequest request, String serverHostText, String serverPortText) {
        this.messageHandler = messageHandler;
        this.registerRequest = request;
        this.serverHostText = serverHostText;
        this.serverPortText = serverPortText;
    }

    @Override
    public void run() {
        ServerProxy.setServerHostName(serverHostText);
        ServerProxy.setServerPortNumber(Integer.parseInt(serverPortText));
        RegisterResult result = ServerProxy.register(registerRequest);
        if (result.isSuccess()) {
            sendMessage(true, result);
        }
        else {
            sendMessage(false, result);
        }

    }

    private void sendMessage(boolean succeeded, RegisterResult result) {
        Message message = Message.obtain();

        Bundle messageBundle = new Bundle();
        messageBundle.putBoolean(SUCCEED_KEY, succeeded);
        message.setData(messageBundle);
        message.obj = result;

        messageHandler.sendMessage(message);
    }
}