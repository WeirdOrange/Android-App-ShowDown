package com.example.showdown;

import com.example.showdown.BuildConfig;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.Random;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class OTPDialog {
    private static final String OTP_PREFS = "OTPPrefs";
    private static final long OTP_VALIDITY = 300000;

    public static String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(999999);
        return String.valueOf(otp);
    }

    public static void saveOTP(Context context, String email, String otp) {
        SharedPreferences prefs = context.getSharedPreferences(OTP_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("otp_" + email, otp);
        editor.putLong("otp_time_" + email, System.currentTimeMillis());
        editor.apply();
    }

    public static boolean verifyOTP(Context context, String email, String enteredOTP) {
        SharedPreferences pref = context.getSharedPreferences(OTP_PREFS,Context.MODE_PRIVATE);
        String savedOTP = pref.getString("otp_" + email, null);
        long savedTime = pref.getLong("otp_time_"+email,0);

        if (savedOTP == null) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - savedTime > OTP_VALIDITY) {
            return false;
        }
        return savedOTP.equals(enteredOTP);
    }

    public static void clearOTP(Context context, String email) {
        SharedPreferences pref = context.getSharedPreferences(OTP_PREFS,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove("otp_" + email);
        editor.remove("otp_time_" + email);
        editor.apply();
    }

    public static void sendOTPEmail(final String recipient, final String otp) {
        new Thread(() -> {
           try {
               Log.i("Send OTP","Generating Email");
               String senderEmail = BuildConfig.EMAIL_ADDRESS;
               String senderPassword = BuildConfig.EMAIL_APP_PASSWORD;

               Properties props = new Properties();
               props.put("mail.smtp.auth","true");
               props.put("mail.smtp.starttls.enable", "true");
               props.put("mail.smtp.host", "smtp.gmail.com");
               props.put("mail.smtp.port", "587");
               props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

               Session session = Session.getInstance(props, new Authenticator() {
                   @Override
                   protected PasswordAuthentication getPasswordAuthentication() {
                       return new PasswordAuthentication(senderEmail, senderPassword);
                   }
               });

               String cleanRecipient = recipient
                       .replaceAll("\\s", "")                // removes ALL whitespace (space/tab/newline)
                       .replace("\u200B", "");               // removes zero-width space (common issue)

               Message message = new MimeMessage(session);
               message.setFrom(new InternetAddress(senderEmail));
               message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(cleanRecipient, true));
               message.setSubject("Showdown - Email Verification OTP");

                String emailBody = "<!DOCTYPE html>"+
                        "<html><body style='font-family: Arial, sans-serif;'>" +
                        "<div style='max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f5f5f5;'>" +
                        "<h2 style='color: #333;'>Email Verification</h2>" +
                        "<p>Your OTP for email verification is:</p>" +
                        "<h1 style='color: #4CAF50; font-size: 36px; letter-spacing: 5px;'>" + otp + "</h1>" +
                        "<p>This OTP will expire in 5 minutes.</p>" +
                        "<p style='color: #666; font-size: 12px;'>If you didn't request this, please ignore this email.</p>" +
                        "</div></body></html>";

                message.setContent(emailBody, "text/html");
                Transport.send(message);
               Log.i("Send OTP","Sending Email");

           }catch (MessagingException e) {
               e.printStackTrace();
               Log.e("Send OTP","Email was not able to send " + e.getMessage());
               Log.d("Email Debug", "Sender email: [" + BuildConfig.EMAIL_ADDRESS + "]");
               Log.d("Email Debug", "Recipient email: [" + recipient + "]");
               Log.d("Email Debug", "Recipient length = " + recipient.length());
               for (int i = 0; i < recipient.length(); i++) {
                   Log.d("Email Debug", "Char[" + i + "] = " + (int) recipient.charAt(i));
               }
           }
        }).start();
    }
}
