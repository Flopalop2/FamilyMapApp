package com.mbrow233.familymap.net;

import android.util.Log;

import com.mbrow233.familymap.data.DataCache;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;

import Request.JSonSerialize;
import Request.LoginRequest;
import Request.RegisterRequest;
import Result.AllPersonResults;
import Result.EventsResult;
import Result.LoginResult;
import Result.RegisterResult;

public class ServerProxy {
    public static String serverHostName;
    public static int serverPortNumber;
    private static String authToken;

    private static final String TAG = "ServerProxy";

    public static String getServerHostName() {
        return serverHostName;
    }

    public static void setServerHostName(String serverHostName) {
        ServerProxy.serverHostName = serverHostName;
    }

    public static int getServerPortNumber() {
        return serverPortNumber;
    }

    public static void setServerPortNumber(int serverPortNumber) {
        ServerProxy.serverPortNumber = serverPortNumber;
    }

    public static LoginResult login(LoginRequest r) {
        //serialize request
        JSonSerialize<LoginRequest> serial = new JSonSerialize<>();
        String reqData = serial.serialize(r);
        Log.i(TAG,"logging in ");

        //make http request
        try {
            URL url = new URL("http://" + serverHostName + ":" + serverPortNumber + "/user/login");

            HttpURLConnection http = (HttpURLConnection)url.openConnection();

            http.setRequestMethod("POST");
            http.setDoOutput(true);	// There is a request body

            //http.addRequestProperty("Authorization", authToken);
            http.addRequestProperty("Content-Type", "application/json");

            http.connect(); //why does this take so long to throw if server is off? actually never mind only on emulators...

            OutputStream reqBody = http.getOutputStream();
            writeString(reqData, reqBody);
            reqBody.close();

            InputStream respBody;
            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Log.i(TAG,"Login request successful.");
                respBody = http.getInputStream();
            }
            else {
                Log.e(TAG,"ERROR: " + http.getResponseMessage());
                respBody = http.getErrorStream();
            }


            //deserialize result
            JSonSerialize<LoginResult> deSerial = new JSonSerialize<>();
            LoginResult result = deSerial.deserialize(respBody, LoginResult.class);

            DataCache.getInstance().setBasePerson(result.getPersonID());

            authToken = result.getAuthtoken();

            return result;
        }
        catch (IOException e) {
            e.printStackTrace();
            LoginResult result = new LoginResult("Cannot connect to server");
            return result;
        }

        //return null;
    }

    public static RegisterResult register(RegisterRequest r) {
        //serialize request
        JSonSerialize<RegisterRequest> serial = new JSonSerialize<>();
        String reqData = serial.serialize(r);
        Log.i(TAG,"registering");
        //make http request
        try {
            URL url = new URL("http://" + serverHostName + ":" + serverPortNumber + "/user/register");

            HttpURLConnection http = (HttpURLConnection)url.openConnection();

            http.setRequestMethod("POST");
            http.setDoOutput(true);	// There is a request body

            //http.addRequestProperty("Authorization", authToken);
            http.addRequestProperty("Content-Type", "application/json");

            http.connect();

            OutputStream reqBody = http.getOutputStream();
            writeString(reqData, reqBody);
            reqBody.close();

            InputStream respBody;
            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Log.i(TAG,"Register request successful.");
                respBody = http.getInputStream();
            }
            else {
                Log.e(TAG,"ERROR: " + http.getResponseMessage());
                respBody = http.getErrorStream();
            }

            //deserialize result
            JSonSerialize<RegisterResult> deSerial = new JSonSerialize<>();
            RegisterResult result = deSerial.deserialize(respBody, RegisterResult.class);

            authToken = result.getAuthtoken();

            return result;
        }
        catch (IOException e) {
            e.printStackTrace();
            RegisterResult result = new RegisterResult("Cannot connect to server");
            return result;
        }

        //return null;
    }

    public static AllPersonResults getAllPeople() {
        //make http request
        try {
            URL url = new URL("http://" + serverHostName + ":" + serverPortNumber + "/person");

            HttpURLConnection http = (HttpURLConnection)url.openConnection();

            http.setRequestMethod("GET");
            http.setDoOutput(false);	// There is a request body

            http.addRequestProperty("Authorization", authToken);
            http.addRequestProperty("Content-Type", "application/json");

            http.connect();


            InputStream respBody;
            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Log.i(TAG,"Person retrieval successful.");
                respBody = http.getInputStream();
            }
            else {
                Log.e(TAG,"ERROR: " + http.getResponseMessage());
                respBody = http.getErrorStream();
            }

            //deserialize result
            JSonSerialize<AllPersonResults> deSerial = new JSonSerialize<>();
            AllPersonResults result = deSerial.deserialize(respBody, AllPersonResults.class);

            return result;
        }
        catch (IOException e) {
            e.printStackTrace();
            AllPersonResults result = new AllPersonResults("Cannot connect to server");
            return result;
        }
        //return null;
    }

    public static EventsResult getAllEvents() {
        //make http request
        try {
            URL url = new URL("http://" + serverHostName + ":" + serverPortNumber + "/event");

            HttpURLConnection http = (HttpURLConnection)url.openConnection();

            http.setRequestMethod("GET");
            http.setDoOutput(false);	// There is a request body

            http.addRequestProperty("Authorization", authToken);
            http.addRequestProperty("Content-Type", "application/json");

            http.connect();

            InputStream respBody;
            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Log.i(TAG,"Event retrieval successful.");
                respBody = http.getInputStream();
            }
            else {
                Log.e(TAG,"ERROR: " + http.getResponseMessage());
                respBody = http.getErrorStream();
            }

            //deserialize result
            JSonSerialize<EventsResult> deSerial = new JSonSerialize<>();
            EventsResult result = deSerial.deserialize(respBody, EventsResult.class);

            return result;
        }
        catch (IOException e) {
            e.printStackTrace();
            EventsResult result = new EventsResult("Cannot connect to server");
            return result;
        }
        //return null;
    }
    //add logout that removes authtoken from database...


    private static String readString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStreamReader sr = new InputStreamReader(is);
        char[] buf = new char[1024];
        int len;
        while ((len = sr.read(buf)) > 0) {
            sb.append(buf, 0, len);
        }
        return sb.toString();
    }

    private static void writeString(String str, OutputStream os) throws IOException {
        OutputStreamWriter sw = new OutputStreamWriter(os);
        sw.write(str);
        sw.flush();
    }

}
