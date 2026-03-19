package com.example.demo.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	@Autowired
	private JavaMailSender mailSender;

	public void sendOtp(String toEmail, String otp) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(toEmail);
		message.setSubject("EduPlatform - Login OTP Verification");
		message.setText("Your OTP is: " + otp + "\nValid for 5 minutes.");

		mailSender.send(message);
	}

	public void sendOfferLetter(String toEmail, String name, String internshipTitle) {

	    SimpleMailMessage message = new SimpleMailMessage();

	    message.setTo(toEmail);
	    message.setSubject("🎉 Congratulations! Internship Offer Letter");

	    message.setText(
	        "Dear " + name + ",\n\n" +
	        "We are pleased to inform you that you have been SELECTED for the internship:\n\n" +
	        internshipTitle + "\n\n" +
	        "Please login to EduPlatform to check further details.\n\n" +
	        "Best Regards,\nEduPlatform Team"
	    );

	    mailSender.send(message);
	}
	
	public void sendRejectionMail(String toEmail, String name, String internshipTitle) {

	    SimpleMailMessage message = new SimpleMailMessage();

	    message.setTo(toEmail);
	    message.setSubject("Internship Application Update");

	    message.setText(
	        "Dear " + name + ",\n\n" +
	        "Thank you for applying for the internship:\n" +
	        internshipTitle + "\n\n" +
	        "We regret to inform you that your application was not selected.\n\n" +
	        "We encourage you to apply again in future.\n\n" +
	        "Best Regards,\nEduPlatform Team"
	    );

	    mailSender.send(message);
	}
}
