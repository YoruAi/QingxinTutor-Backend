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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
        String html = buildCodeHtml("email/register-code", code);
        sendHtml(to, subject, html);
    }

    /**
     * 发送登录验证码
     */
    @Async
    public void sendLoginCode(String to, String code) {
        String subject = "【Qingxin Tutor】登录验证码";
        String html = buildCodeHtml("email/login-code", code);
        sendHtml(to, subject, html);
    }

    /**
     * 发送忘记密码验证码
     */
    @Async
    public void sendResetPasswordCode(String to, String code) {
        String subject = "【Qingxin Tutor】重置密码验证码";
        String html = buildCodeHtml("email/reset-password-code", code);
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

    /**
     * 发送课程开始通知邮件
     */
    @Async
    public void sendLessonReminder(String to,
                                   String studentName,
                                   String teacherName,
                                   LocalDateTime startTime,
                                   Integer durationMinutes) {
        String subject = "【Qingxin Tutor】课程开始通知";
        String html = buildLessonReminderHtml(studentName,
                teacherName,
                startTime,
                durationMinutes.toString());
        sendHtml(to, subject, html);
    }

    /**
     * 发送“新预约请求”邮件给教师
     */
    @Async
    public void sendNewReservationRequestToTeacher(
            String to,
            String teacherName,
            String studentName,
            LocalDateTime startTime,
            Integer durationMinutes) {
        String subject = "【Qingxin Tutor】您有一条新的预约请求";
        String html = buildNewReservationRequestHtml(teacherName, studentName, startTime, durationMinutes);
        sendHtml(to, subject, html);
    }

    /**
     * 发送“新的订单”邮件给学生
     */
    @Async
    public void sendOrderCreatedToStudent(
            String to,
            String studentName,
            BigDecimal price,
            Integer quantity,
            BigDecimal totalAmount) {
        String subject = "【Qingxin Tutor】收到新的订单，请及时支付";
        String html = buildOrderCreatedHtml(studentName, price, quantity, totalAmount);
        sendHtml(to, subject, html);
    }

    /**
     * 发送“课程结课 + 奖学券到账”邮件给学生
     */
    @Async
    public void sendCourseCompletedWithVoucherToStudent(
            String to,
            String studentName,
            Long reservationId,
            BigDecimal voucherAmount) {
        String subject = "【Qingxin Tutor】课程结课通知";
        String html = buildCourseCompletedHtml(studentName, reservationId, voucherAmount);
        sendHtml(to, subject, html);
    }


    private String buildCourseCompletedHtml(
            String studentName,
            Long reservationId,
            BigDecimal voucherAmount) {
        Context context = new Context();
        context.setVariable("studentName", studentName);
        context.setVariable("reservationId", reservationId);
        context.setVariable("voucherAmount", voucherAmount.toString());
        return templateEngine.process("email/course-completed-voucher", context);
    }

    private String buildOrderCreatedHtml(
            String studentName,
            BigDecimal price,
            Integer quantity,
            BigDecimal totalAmount) {

        Context context = new Context();
        context.setVariable("studentName", studentName);
        context.setVariable("price", price.toString());
        context.setVariable("quantity", quantity);
        context.setVariable("totalAmount", totalAmount.toString());
        return templateEngine.process("email/order-created", context);
    }

    private String buildNewReservationRequestHtml(
            String teacherName,
            String studentName,
            LocalDateTime startTime,
            Integer durationMinutes) {
        String startTimeStr = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        Context context = new Context();
        context.setVariable("teacherName", teacherName);
        context.setVariable("studentName", studentName);
        context.setVariable("startTime", startTimeStr);
        context.setVariable("durationMinutes", durationMinutes);
        return templateEngine.process("email/reservation-request", context);
    }

    private String buildLessonReminderHtml(
            String studentName,
            String teacherName,
            LocalDateTime startTime,
            String durationMinutes) {
        String startTimeString = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        Context context = new Context();
        context.setVariable("studentName", studentName);
        context.setVariable("teacherName", teacherName);
        context.setVariable("startTime", startTimeString);
        context.setVariable("durationMinutes", durationMinutes);
        return templateEngine.process("email/lesson-reminder", context);
    }

    private String buildRegisterSuccessHtml(String username) {
        Context context = new Context();
        context.setVariable("username", username);
        return templateEngine.process("email/register-success", context);
    }

    private String buildCodeHtml(String templateName, String code) {
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
     * 发送 HTML 邮件
     *
     * @param to      收件人邮箱
     * @param subject 邮件主题
     * @param html    邮件正文（HTML 格式）
     */
    private void sendHtml(String to, String subject, String html) {
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
