package com.yoru.qingxintutor.utils;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Component
public class EmailUtils {

    @Value("${spring.mail.username}")
    private String from;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private TemplateEngine templateEngine;

    /**
     * 发送注册验证码
     */
    @Async
    public void sendRegisterCode(String to, String code) {
        String subject = "【Qingxin Tutor】注册验证码";
        String html = buildHtml("email/register-code", code);
        sendHtml(to, subject, html);
    }

    /**
     * 发送登录验证码
     */
    @Async
    public void sendLoginCode(String to, String code) {
        String subject = "【Qingxin Tutor】登录验证码";
        String html = buildHtml("email/login-code", code);
        sendHtml(to, subject, html);
    }

    /**
     * 发送忘记密码验证码
     */
    @Async
    public void sendResetPasswordCode(String to, String code) {
        String subject = "【Qingxin Tutor】重置密码验证码";
        String html = buildHtml("email/reset-password-code", code);
        sendHtml(to, subject, html);
    }

    /**
     * 发送注册成功通知邮件
     */
    @Async
    public void sendRegisterSuccess(String to, String username) {
        String subject = "【Qingxin Tutor】欢迎加入！注册成功";
        String html = buildRegisterSuccessHtml(username);
        sendHtml(to, subject, html);
    }

    private String buildRegisterSuccessHtml(String username) {
        Context context = new Context();
        context.setVariable("username", username);
        return templateEngine.process("email/register-success", context);
    }

    private String buildHtml(String templateName, String code) {
        Context context = new Context();
        context.setVariable("code", code);
        return templateEngine.process(templateName, context);
    }

    /**
     * 发送简单文本邮件
     *
     * @param to      收件人邮箱
     * @param subject 邮件主题
     * @param text    邮件正文
     */
    private void sendSimpleText(String to, String subject, String text) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from, "Qingxin Tutor");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false);
            javaMailSender.send(message);
            log.debug("Email send success. to={}, subject={}", to, subject);
        } catch (Exception e) {
            log.error("Email send error. to={}, subject={}, error: {}", to, subject, e.getMessage());
        }
    }

    /**
     * 发送 HTML 邮件（推荐用于验证码）
     *
     * @param to      收件人邮箱
     * @param subject 邮件主题
     * @param html    邮件正文（HTML 格式）
     */
    public void sendHtml(String to, String subject, String html) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from, "Qingxin Tutor");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            javaMailSender.send(message);
            log.debug("Email(html) send success. to={}, subject={}", to, subject);
        } catch (Exception e) {
            log.error("Email(html) send error. to={}, subject={}, error: {}", to, subject, e.getMessage());
        }
    }

}
