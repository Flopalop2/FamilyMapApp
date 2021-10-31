package com.mbrow233.familymap.net;

import com.mbrow233.familymap.net.ServerProxy;

import junit.framework.TestCase;

import java.util.List;

import Model.Event;
import Model.User;
import Request.LoginRequest;
import Request.RegisterRequest;
import Result.AllPersonResults;
import Result.EventsResult;
import Result.LoginResult;
import Result.RegisterResult;

public class ServerProxyTest extends TestCase {
    private User bestUser;
    private LoginRequest request;
    private LoginRequest badRequest;
    private RegisterRequest registerRequest;

    public void testLogin() {
        LoginResult result = ServerProxy.login(request);
        LoginResult resultFail = ServerProxy.login(badRequest);

        assertEquals(bestUser.getPersonID(), result.getPersonID());
        assertTrue(resultFail.getErrorMessage().toLowerCase().contains("error"));
    }

    public void testRegister() {
        RegisterResult result = ServerProxy.register(registerRequest);
        assertEquals("username4", result.getUsername());
        assertNotSame(bestUser.getPersonID(), result.getPersonID());

        RegisterResult badResult = ServerProxy.register(new RegisterRequest(null, null, null, null, null, null));
        assertTrue(badResult.getMessage().toLowerCase().contains("error"));
    }

    public void testGetAllPeople() {
        AllPersonResults all= ServerProxy.getAllPeople();
        assertFalse(all.isSuccess());

        assertTrue(all.getMessage().toLowerCase().contains("error"));
    }

    public void testGetAllEvents() {
        EventsResult result = ServerProxy.getAllEvents();
        assertFalse(result.isSuccess());

        assertTrue(result.getMessage().toLowerCase().contains("error"));
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        bestUser = new User("username", "password", "doe@email.com", "John", "Doe", "m", "w4o7k3w0j1y2o1r7");
        request = new LoginRequest("username", "password");
        badRequest = new LoginRequest("test", "test");
        registerRequest = new RegisterRequest("username4", "password2", "doe@email.com", "John", "Doe", "m");
        ServerProxy.setServerHostName("localhost");
        ServerProxy.setServerPortNumber(8080);
    }
}