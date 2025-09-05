package com.groceryshop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    // Store OTPs temporarily (in production, use Redis or similar)
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public String generateAndSendOTP(String email) {
        String otp = String.format("%06d", random.nextInt(1000000));
        otpStorage.put(email, otp);

        // For demo purposes, just log the OTP
        System.out.println("OTP for " + email + ": " + otp);

        try {
            if (mailSender != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(email);
                message.setSubject("Grocery Shop - Login OTP");
                message.setText("Your OTP for login is: " + otp + "\nThis OTP is valid for 5 minutes.");
                mailSender.send(message);
            }
        } catch (Exception e) {
            System.out.println("Email sending failed, but OTP generated: " + otp);
        }

        return otp; // Return for demo purposes
    }

    public boolean verifyOTP(String email, String otp) {
        String storedOtp = otpStorage.get(email);
        if (storedOtp != null && storedOtp.equals(otp)) {
            otpStorage.remove(email); // Remove after successful verification
            return true;
        }
        return false;
    }

    public void sendOrderNotification(String adminEmail, String orderDetails) {
        try {
            if (mailSender != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(adminEmail);
                message.setSubject("New Order Received - Grocery Shop");
                message.setText("New order received:\n\n" + orderDetails);
                mailSender.send(message);
            }
            System.out.println("Order notification sent to admin: " + adminEmail);
        } catch (Exception e) {
            System.out.println("Failed to send order notification: " + e.getMessage());
        }
    }

    public void sendOrderConfirmation(String customerEmail, String orderDetails, String deliveryTime) {
        try {
            if (mailSender != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(customerEmail);
                message.setSubject("Order Confirmed - Grocery Shop");
                message.setText("Your order has been confirmed!\n\n" + 
                               "Order Details:\n" + orderDetails + 
                               "\n\nEstimated Delivery Time: " + deliveryTime);
                mailSender.send(message);
            }
            System.out.println("Order confirmation sent to customer: " + customerEmail);
        } catch (Exception e) {
            System.out.println("Failed to send order confirmation: " + e.getMessage());
        }
    }
}
