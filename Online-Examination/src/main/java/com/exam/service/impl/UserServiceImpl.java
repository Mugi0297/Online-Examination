package com.exam.service.impl;

import com.exam.model.User;
import com.exam.repository.UserRepository;
import com.exam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setVerified(false);
        user.setVerificationCode(UUID.randomUUID().toString());
        user.setExpiryTime(LocalDateTime.now().plusMinutes(10)); // 10 min expiry
        userRepository.save(user);
        sendVerificationEmail(user);
        return user;
    }

    private void sendVerificationEmail(User user) {
        String subject = "Email Verification";
        String verificationLink = "http://localhost:8080/verify?code=" + user.getVerificationCode();
        String message = "Click the link to verify your email: <a href=\"" + verificationLink + "\">Verify</a>";

        try {
            MimeMessage mail = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mail, true);
            helper.setTo(user.getEmail());
            helper.setSubject(subject);
            helper.setText(message, true);
            mailSender.send(mail);
        } catch (MessagingException e) {
            throw new RuntimeException("Email sending failed");
        }
    }

    @Override
    public boolean verifyEmail(String code) {
        Optional<User> userOptional = userRepository.findByVerificationCode(code);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getExpiryTime().isBefore(LocalDateTime.now())) {
                return false; // Expired link
            }
            user.setVerified(true);
            user.setVerificationCode(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }

	@Override
	public User findByEmail(String email) {
		// TODO Auto-generated method stub
		return null;
	}

   
}
