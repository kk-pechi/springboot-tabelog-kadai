package com.example.nagoyameshi.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendResetPasswordEmail(String toEmail, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("【NAGOYAMESHI】パスワード再設定リンク");
        message.setText("以下のリンクからパスワードを再設定してください：\n\n" + resetLink + "\n\n※このリンクは1時間で失効します。");
        mailSender.send(message);
    }
}