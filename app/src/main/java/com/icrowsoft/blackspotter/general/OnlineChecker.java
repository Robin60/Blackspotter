package com.icrowsoft.blackspotter.general;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class OnlineChecker {

    public static String Go_Online(String url) {

        String server_response = " ";
//		try {

        try {
            StringBuilder sb = new StringBuilder();

            BufferedInputStream bis = null;
            URL the_url = null;
            the_url = new URL(url);

            HttpURLConnection con = (HttpURLConnection) the_url.openConnection();
            int responseCode;

            con.setRequestMethod("POST");
            con.setConnectTimeout(10000);
            con.setReadTimeout(10000);

            responseCode = con.getResponseCode();

            if (responseCode == 200) {
                bis = new BufferedInputStream(con.getInputStream());
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(bis, "UTF-8"));
                String line = null;

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                bis.close();
            }
            server_response = sb.toString();
//			return server_response;
        } catch (MalformedURLException e) {
            My_Logger("MalformedURLException = " + e.getMessage());
            server_response = e.getMessage();
        } catch (ProtocolException e) {
            My_Logger("ProtocolException = " + e.getMessage());
            server_response = e.getMessage();
        } catch (UnsupportedEncodingException e) {
            My_Logger("UnsupportedEncodingException = " + e.getMessage());
            server_response = e.getMessage();
        } catch (IOException e) {
            My_Logger("IOException = " + e.getMessage());
            server_response = e.getMessage();
        }

//		} catch (ClientProtocolException e) {
//			My_Logger("ClientProtocolException = " + e.getMessage());
//			server_response = e.getMessage();
//		} catch (IOException e) {
//			My_Logger("IOException = " + e);
//			server_response = e.getMessage();
//		}

        return server_response;
    }

    public static void My_Logger(String message) {
        Log.d("Kibet", message);
    }
}
