package com.example.demo.Service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // ================= OTP =================
    public void sendOtp(String toEmail, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);
        message.setSubject("🔐 EduPlatform OTP Verification");

        message.setText(
                "Dear User,\n\n" +

                "Your One-Time Password (OTP) for login verification is:\n\n" +

                "🔑 OTP: " + otp + "\n\n" +

                "⏳ This OTP is valid for 5 minutes.\n\n" +

                "If you did not request this, please ignore this email.\n\n" +

                "Best Regards,\nEduPlatform Team"
        );

        mailSender.send(message);
    }
    
    public void sendAcceptedMail(String toEmail, String name, String title) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);
        message.setSubject("Application Accepted | " + title);

        message.setText(
                "Dear " + name + ",\n\n" +

                "Your application for the internship \"" + title + "\" has been accepted.\n\n" +

                "Next Step:\n" +
                "If the company has arranged an assessment test, you are required to complete it.\n" +
                "Otherwise, please wait for further instructions or contact the admin.\n\n" +

                "Please login to your EduPlatform account to continue.\n\n" +

                "Best Regards,\nEduPlatform Team"
        );

        mailSender.send(message);
    }
    
    
    // ================= OFFER EMAIL =================
    public void sendOfferLetter(String toEmail, String name,
                                String title, String role, String type,
                                String location, Integer stipend,
                                String duration, LocalDate startDate) {

        // ✅ SAFETY (DOUBLE CHECK)
        title = safe(title);
        role = safe(role);
        type = safe(type);
        location = safe(location);
        duration = safe(duration);
        stipend = (stipend != null) ? stipend : 0;
        startDate = (startDate != null) ? startDate : LocalDate.now();

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);
        message.setSubject("🎉 Offer Letter | " + title + " Internship");

        message.setText(
                "Dear " + name + ",\n\n" +

                "Congratulations! 🎉\n" +
                "We are delighted to offer you an internship opportunity at EduPlatform.\n\n" +

                "📌 Internship Details:\n" +
                "-----------------------------------\n" +
                "Title      : " + title + "\n" +
                "Role       : " + role + "\n" +
                "Type       : " + type + "\n" +
                "Location   : " + location + "\n" +
                "Stipend    : ₹" + stipend + " per month\n" +
                "Duration   : " + duration + "\n" +
                "Start Date : " + startDate + "\n" +
                "-----------------------------------\n\n" +

                "You have been selected based on your performance and profile.\n\n" +

                "👉 Please login to your EduPlatform account to view your offer letter and next steps.\n\n" +

                "We look forward to having you on our team!\n\n" +

                "Best Regards,\nEduPlatform Team\n"
        );

        mailSender.send(message);
    }

    // ================= REJECTION EMAIL =================
    public void sendRejectionMail(String toEmail, String name,
                                 String title, String role, String type) {

        title = safe(title);
        role = safe(role);
        type = safe(type);

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);
        message.setSubject("Application Update | " + title + " Internship");

        message.setText(
                "Dear " + name + ",\n\n" +

                "Thank you for applying for the following internship at EduPlatform:\n\n" +

                "📌 Internship Details:\n" +
                "-----------------------------------\n" +
                "Title : " + title + "\n" +
                "Role  : " + role + "\n" +
                "Type  : " + type + "\n" +
                "-----------------------------------\n\n" +

                "After careful consideration, we regret to inform you that your application was not selected at this time.\n\n" +

                "We truly appreciate your interest and encourage you to apply again for future opportunities.\n\n" +

                "Wishing you all the best in your journey ahead! 💼\n\n" +

                "Best Regards,\nEduPlatform Team\n"
        );

        mailSender.send(message);
    }
    
    public void sendTestSubmittedMail(String toEmail, String name, String title) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);
        message.setSubject("Test Submitted | " + title);

        message.setText(
                "Dear " + name + ",\n\n" +

                "Your assessment for the internship \"" + title + "\" has been successfully submitted.\n\n" +

                "Our team will now evaluate your responses.\n" +
                "You will be notified once the results are available.\n\n" +

                "Please keep checking your dashboard for updates.\n\n" +

                "Best Regards,\nEduPlatform Team"
        );

        mailSender.send(message);
    }
    
    public void sendTestPassedMail(String toEmail, String name, String title) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);
        message.setSubject("Assessment Result | " + title);

        message.setText(
                "Dear " + name + ",\n\n" +

                "We are pleased to inform you that you have successfully cleared the assessment for the internship \"" + title + "\".\n\n" +

                "You have moved to the next stage of the selection process.\n" +
                "Our team will review your performance and notify you regarding the final selection.\n\n" +

                "Please stay updated through your dashboard.\n\n" +

                "Best Regards,\nEduPlatform Team"
        );

        mailSender.send(message);
    }
    
    public void sendTestFailedMail(String toEmail, String name, String title) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);
        message.setSubject("Assessment Result | " + title);

        message.setText(
                "Dear " + name + ",\n\n" +

                "Thank you for completing the assessment for the internship \"" + title + "\".\n\n" +

                "After evaluation, we regret to inform you that you did not meet the required criteria for this opportunity.\n\n" +

                "We encourage you to continue improving your skills and apply again in the future.\n\n" +

                "Best Regards,\nEduPlatform Team"
        );

        mailSender.send(message);
    }
    
    

    // ================= HELPER =================
    private String safe(String value) {
        return (value != null && !value.isEmpty()) ? value : "N/A";
    }
}