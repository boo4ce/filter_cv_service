/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pdf_filter_restful.noti;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Admin
 */
@WebServlet(name = "EmailSender", urlPatterns = {"/emailsender/send"})
public class EmailSender extends HttpServlet {
    private ExecutorService service;

    @Override
    public void init() throws ServletException {
        service = Executors.newCachedThreadPool();
    }
    
    int j = 0;
    int failedAmount = 0;
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONArray reqData = new JSONArray(getBody(req));
        List<JSONObject> list = reqData.toList().stream().map((data) -> {
            return new JSONObject((Map)data);
        }).toList();
        
        int i = list.size();
        j = 0;
        for(JSONObject obj :list) {
            service.execute(() -> {
                try {
                    sendEmail(obj.getString("email"), obj.getString("textSend"));
                } catch (MessagingException ex) {
                    failedAmount++;
                } finally {
                    j++;
                }
            });
        }
        
        while(true) {
            if(j == i) {
                sendMessageToClient("Send emails successfully " + failedAmount, resp);
                break;
            }
        }
        
        sendMessageToClient("Emails are sending", resp);
    }
    
    
    public void sendEmail(String to, String content) throws AddressException, MessagingException {
        System.out.println("send");
        // Setup mail server
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", MailConfig.HOST_NAME);
        props.put("mail.smtp.socketFactory.port", MailConfig.SSL_PORT);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.port", MailConfig.SSL_PORT);
//        props.put("mail.store.protocol", "pop3");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.enable", "true");
        
        // Get the default Session object.
        Authenticator authenticator = new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(MailConfig.APP_EMAIL, MailConfig.APP_PASSWORD);
            }
        };

        Session session = Session.getInstance(props, authenticator);
        
        // Create a default MimeMessage object.
        MimeMessage message = new MimeMessage(session);

        // Set To: header field of the header.
        message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(to));

        // Create the message part 
        message.setSubject("Testing Subject");
        message.setText(content);

        // send message
        Transport.send(message);
           
    }
    
    public String getBody(HttpServletRequest request) {
        String body = null;
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;

        try {
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[256];
                int bytesRead = -1;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            } else {
                stringBuilder.append("");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        body = stringBuilder.toString();
        return body;
    }
    
    protected void sendMessageToClient(String message, HttpServletResponse _resp) {
        try ( PrintWriter out = _resp.getWriter()) {
            out.write(message);
            /* TODO output your page here. You may use following sample code. */
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    static class MailConfig {
        public static final String HOST_NAME = "smtp.gmail.com";
 
        public static final int SSL_PORT = 465; // Port for SSL

        public static final int TSL_PORT = 587; // Port for TLS/STARTTLS

        public static final String APP_EMAIL = "thisisname6969@gmail.com"; // your email

        public static final String APP_PASSWORD = "@Thisisname6969"; // your password
    }
}
