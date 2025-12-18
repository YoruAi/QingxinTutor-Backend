package com.yoru.qingxintutor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoru.qingxintutor.exception.BusinessException;
import com.yoru.qingxintutor.mapper.*;
import com.yoru.qingxintutor.pojo.dto.request.MessageCreateRequest;
import com.yoru.qingxintutor.pojo.dto.request.ReservationCreateRequest;
import com.yoru.qingxintutor.pojo.entity.*;
import com.yoru.qingxintutor.pojo.result.PrivateMessageInfoResult;
import com.yoru.qingxintutor.pojo.result.ReservationInfoResult;
import com.yoru.qingxintutor.utils.EmailUtils;
import com.yoru.qingxintutor.websocket.PrivateChatWebSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ReservationService {
    @Autowired
    private UserOrderMapper orderMapper;
    @Autowired
    private ReservationMapper reservationMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private TeacherSubjectMapper teacherSubjectMapper;
    @Autowired
    private SubjectMapper subjectMapper;
    @Autowired
    private WalletService walletService;
    @Autowired
    private VoucherService voucherService;
    @Autowired
    private PrivateChatService chatService;
    @Autowired
    private PrivateChatWebSocket privateChatWebSocket;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private UserEmailMapper emailMapper;
    @Autowired
    private EmailUtils emailUtils;

    @Transactional(readOnly = true)
    public List<ReservationInfoResult> listStudentReservations(String userId, ReservationEntity.State state) {
        String username = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"))
                .getUsername();
        return reservationMapper.findByUserIdAndState(userId, state)
                .stream()
                .map(reservation -> entityToResult(reservation,
                        username,
                        teacherMapper.findNameById(reservation.getTeacherId())
                                .orElseThrow(() -> new BusinessException("Teacher not found")),
                        subjectMapper.findById(reservation.getSubjectId())
                                .orElseThrow(() -> new BusinessException("Subject not found"))
                                .getSubjectName())
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationInfoResult> listTeacherReservations(String teacherUserId, ReservationEntity.State state) {
        Long teacherId = teacherMapper.findTeacherIdByUserId(teacherUserId)
                .orElseThrow(() -> new BusinessException("Teacher not found"));
        String teacherName = teacherMapper.findNameById(teacherId)
                .orElseThrow(() -> new BusinessException("Teacher not found"));
        return reservationMapper.findByTeacherIdAndState(teacherId, state)
                .stream()
                .map(reservation -> entityToResult(reservation,
                        userMapper.findById(reservation.getUserId())
                                .orElseThrow(() -> new BusinessException("User not found"))
                                .getUsername(),
                        teacherName,
                        subjectMapper.findById(reservation.getSubjectId())
                                .orElseThrow(() -> new BusinessException("Subject not found"))
                                .getSubjectName())
                )
                .toList();
    }

    public ReservationInfoResult getStudentReservation(String userId, Long id) {
        String username = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"))
                .getUsername();
        ReservationEntity reservation = reservationMapper.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException("Reservation not found"));
        return entityToResult(reservation, username,
                teacherMapper.findNameById(reservation.getTeacherId())
                        .orElseThrow(() -> new BusinessException("Teacher not found")),
                subjectMapper.findById(reservation.getSubjectId())
                        .orElseThrow(() -> new BusinessException("Subject not found"))
                        .getSubjectName());
    }

    public ReservationInfoResult getTeacherReservation(String teacherUserId, Long id) {
        Long teacherId = teacherMapper.findTeacherIdByUserId(teacherUserId)
                .orElseThrow(() -> new BusinessException("Teacher not found"));
        String teacherName = teacherMapper.findNameById(teacherId)
                .orElseThrow(() -> new BusinessException("Teacher not found"));
        ReservationEntity reservation = reservationMapper.findByIdAndTeacherId(id, teacherId)
                .orElseThrow(() -> new BusinessException("Reservation not found"));
        return entityToResult(reservation,
                userMapper.findById(reservation.getUserId())
                        .orElseThrow(() -> new BusinessException("User not found"))
                        .getUsername(),
                teacherName,
                subjectMapper.findById(reservation.getSubjectId())
                        .orElseThrow(() -> new BusinessException("Subject not found"))
                        .getSubjectName());
    }

    public ReservationInfoResult createReservation(String userId, ReservationCreateRequest request) {
        String username = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"))
                .getUsername();
        String teacherName = teacherMapper.findNameById(request.getTeacherId())
                .orElseThrow(() -> new BusinessException("Teacher not found"));
        Long subjectId = subjectMapper.findBySubjectName(request.getSubjectName().trim())
                .orElseThrow(() -> new BusinessException("Subject not found"))
                .getId();
        if (teacherSubjectMapper.findByTeacherId(request.getTeacherId())
                .stream()
                .noneMatch(subject -> subject.getId().equals(subjectId)))
            throw new BusinessException("The teacher doesnt teach the subject");
        if (reservationMapper.existsConflict(request.getTeacherId(),
                request.getStartTime(),
                request.getStartTime().plusMinutes(request.getDuration())))
            throw new BusinessException("Reservation time conflicts with the teacher's time, " +
                    "please select a different time period");
        ReservationEntity reservation = ReservationEntity.builder()
                .userId(userId)
                .teacherId(request.getTeacherId())
                .subjectId(subjectId)
                .startTime(request.getStartTime())
                .duration(request.getDuration())
                .state(ReservationEntity.State.PENDING)
                .createTime(LocalDateTime.now())
                .build();
        reservationMapper.insert(reservation);

        Long chatId = chatService.getOrCreateChat(userId, request.getTeacherId()).getId();
        PrivateMessageInfoResult message = chatService.insert(userId, chatId,
                MessageCreateRequest.builder()
                        .content("学生向您发起了新的预约请求")
                        .build()
        );
        try {
            privateChatWebSocket.broadcast(chatId, objectMapper.writeValueAsString(message));
        } catch (Exception e) {
            log.error("Error when broadcast new reservation message to private-chat {}: {}", chatId, e.getMessage());
        }

        String teacherUserId = teacherMapper.findUserIdByTeacherId(reservation.getTeacherId())
                .orElseThrow(() -> new BusinessException("Teacher not found"));
        String title = "新预约请求";
        String content = String.format("学生 %s 向您预约了课程，时间：%s，时长：%d 分钟，请及时处理。",
                username,
                reservation.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                reservation.getDuration());
        notificationService.createPersonalNotification(teacherUserId, title, content);
        emailMapper.selectByUserId(teacherUserId).map(UserEmailEntity::getEmail).ifPresent(
                email -> emailUtils.sendNewReservationRequestToTeacher(email,
                        teacherName,
                        username,
                        reservation.getStartTime(),
                        reservation.getDuration())
        );

        return entityToResult(reservation, username, teacherName, request.getSubjectName().trim());
    }

    @Transactional
    public void cancelReservationByStudent(String userId, Long id) {
        ReservationEntity reservation = reservationMapper.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException("Reservation not found"));

        cancelReservation(reservation);

        String title = "预约已取消";
        String content = String.format("预约 %d 已被学生取消，可进入预约页面查看详情。", reservation.getId());
        notificationService.createPersonalNotification(teacherMapper.findUserIdByTeacherId(reservation.getTeacherId())
                .orElseThrow(() -> new BusinessException("User not found")), title, content);
    }

    @Transactional
    public void cancelReservationByTeacher(String teacherUserId, Long id) {
        Long teacherId = teacherMapper.findTeacherIdByUserId(teacherUserId)
                .orElseThrow(() -> new BusinessException("Teacher not found"));
        ReservationEntity reservation = reservationMapper.findByIdAndTeacherId(id, teacherId)
                .orElseThrow(() -> new BusinessException("Reservation not found"));

        cancelReservation(reservation);

        String title = "预约被拒绝";
        String content = String.format("预约 %d 已被教师拒绝，相关退款已退回钱包余额，可进入预约页面查看详情。", reservation.getId());
        notificationService.createPersonalNotification(teacherMapper.findUserIdByTeacherId(reservation.getTeacherId())
                .orElseThrow(() -> new BusinessException("User not found")), title, content);
    }

    @Transactional
    public void confirmReservation(String teacherUserId, Long id) {
        Long teacherId = teacherMapper.findTeacherIdByUserId(teacherUserId)
                .orElseThrow(() -> new BusinessException("Teacher not found"));
        ReservationEntity reservation = reservationMapper.findByIdAndTeacherId(id, teacherId)
                .orElseThrow(() -> new BusinessException("Reservation not found"));
        if (reservation.getState() != ReservationEntity.State.PENDING)
            throw new BusinessException("Only pending reservation can be confirmed");
        if (reservation.getStartTime().isBefore(LocalDateTime.now()))
            throw new BusinessException("The start time has passed");
        if (reservationMapper.existsConflict(teacherId,
                reservation.getStartTime(),
                reservation.getStartTime().plusMinutes(reservation.getDuration())))
            throw new BusinessException("Reservation time conflicts with an confirmed reservation, " +
                    "please cancel the reservation");
        if (!orderMapper.hasNoPendingOrders(id))
            throw new BusinessException("One or more orders are not paid by student yet.");
        reservationMapper.updateState(id, ReservationEntity.State.CONFIRMED);

        String title = "预约已确认";
        String contentStudent = String.format("预约 %d 已确认，请及时与教师联系。可进入预约页面查看详情。", reservation.getId());
        notificationService.createPersonalNotification(reservation.getUserId(), title, contentStudent);
    }

    @Transactional
    public void completeReservation(String teacherUserId, Long id) {
        Long teacherId = teacherMapper.findTeacherIdByUserId(teacherUserId)
                .orElseThrow(() -> new BusinessException("Teacher not found"));
        ReservationEntity reservation = reservationMapper.findByIdAndTeacherId(id, teacherId)
                .orElseThrow(() -> new BusinessException("Reservation not found"));
        if (reservation.getState() != ReservationEntity.State.CONFIRMED)
            throw new BusinessException("Only confirmed reservation can be completed");
        if (reservation.getStartTime().plusMinutes(reservation.getDuration()).isAfter(LocalDateTime.now()))
            throw new BusinessException("The lesson hasn't over");
        reservationMapper.updateState(id, ReservationEntity.State.COMPLETED);
        UserVoucherEntity voucher = voucherService.issue(reservation.getUserId(), BigDecimal.TEN);

        UserEntity student = userMapper.findById(reservation.getUserId())
                .orElseThrow(() -> new BusinessException("User not found"));
        String title = "预约课程已结课";
        String contentStudent = String.format("恭喜您！预约课程 %d 已结课，相关奖学券已发放，请进入钱包界面查收。", reservation.getId());
        notificationService.createPersonalNotification(reservation.getUserId(), title, contentStudent);
        emailMapper.selectByUserId(student.getId()).map(UserEmailEntity::getEmail).ifPresent(
                email -> emailUtils.sendCourseCompletedWithVoucherToStudent(email,
                        student.getUsername(), reservation.getId(), voucher.getAmount())
        );
    }


    // 定时任务 - 课程开始前发送上课提醒
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    public void sendClassReminders() {
        reservationMapper.findReservationsStartingInNext10Minutes().forEach(
                reservation -> {
                    try {
                        UserEntity student = userMapper.findById(reservation.getUserId())
                                .orElseThrow(() -> new BusinessException("User not found"));
                        String teacherName = teacherMapper.findNameById(reservation.getTeacherId())
                                .orElseThrow(() -> new BusinessException("Teacher not found"));
                        notificationService.createPersonalNotification(reservation.getUserId(),
                                "课程开始提醒",
                                "您的课程将在十分钟后开始! 请记得准时参加课程。");
                        emailMapper.selectByUserId(student.getId()).map(UserEmailEntity::getEmail).ifPresent(
                                email -> emailUtils.sendLessonReminder(email,
                                        student.getUsername(),
                                        teacherName,
                                        reservation.getStartTime(),
                                        reservation.getDuration()
                                )
                        );
                        log.debug("Scheduled task: success to send lesson reminder for reservation ID: {}", reservation.getId());
                    } catch (Exception e) {
                        log.error("Failed to send reminder for reservation ID: {}, {}", reservation.getId()
                                , e.getMessage());
                    }
                }
        );
    }

    // 定时任务 - 课程结束后 12 小时自动标记为 COMPLETED
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void autoCompletedReservation() {
        reservationMapper.getReservationsReadyForCompletion()
                .forEach(reservation -> {
                    reservationMapper.updateState(reservation.getId(), ReservationEntity.State.COMPLETED);
                    UserVoucherEntity voucher = voucherService.issue(reservation.getUserId(), BigDecimal.TEN);

                    UserEntity student = userMapper.findById(reservation.getUserId())
                            .orElseThrow(() -> new BusinessException("User not found"));
                    String title = "预约课程已结课";
                    String contentStudent = String.format("恭喜您！预约课程 %d 已结课，相关奖学券已发放，请进入钱包界面查收。", reservation.getId());
                    notificationService.createPersonalNotification(reservation.getUserId(), title, contentStudent);
                    emailMapper.selectByUserId(student.getId()).map(UserEmailEntity::getEmail).ifPresent(
                            email -> emailUtils.sendCourseCompletedWithVoucherToStudent(email,
                                    student.getUsername(), reservation.getId(), voucher.getAmount())
                    );
                    log.debug("Scheduled task: success to auto complete reservation {}", reservation.getId());
                });
    }

    // 定时任务 - 超过课程开始时间自动标记为 CANCELLED
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void autoCancelExpiredReservation() {
        reservationMapper.getExpiredPendingReservations()
                .forEach(reservation -> {
                    try {
                        cancelReservation(reservation);
                        String title = "预约自动取消";
                        String contentStudent = String.format("预约 %d 因超时失效已被自动取消，相关退款已退回钱包余额，可进入预约页面查看详情。", reservation.getId());
                        notificationService.createPersonalNotification(reservation.getUserId(), title, contentStudent);

                        String contentTeacher = String.format("预约 %d 因超时失效已被自动取消，可进入预约页面查看详情。", reservation.getId());
                        notificationService.createPersonalNotification(teacherMapper.findUserIdByTeacherId(reservation.getTeacherId())
                                .orElseThrow(() -> new BusinessException("User not found")), title, contentTeacher);

                        log.debug("Scheduled task: success to auto cancel reservation {}", reservation.getId());
                    } catch (Exception e) {
                        log.warn("Failed to auto-cancel reservation ID {} : {}",
                                reservation.getId(), e.getMessage());
                    }
                });
    }


    @Transactional
    public void cancelReservation(ReservationEntity reservation) {
        ReservationEntity.State state = reservation.getState();
        if (state == ReservationEntity.State.PENDING) {
            Long reservationId = reservation.getId();
            orderMapper.findByReservationId(reservationId)
                    .forEach(order -> {
                        UserOrderEntity.State orderState = order.getState();
                        if (orderState == UserOrderEntity.State.PENDING) {
                            orderMapper.updateState(order.getId(), UserOrderEntity.State.CANCELLED);
                        } else if (orderState == UserOrderEntity.State.PAID) {
                            walletService.addBalance(order.getUserId(),
                                    order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())));
                            orderMapper.updateState(order.getId(), UserOrderEntity.State.CANCELLED);

                            String title = "退款已处理";
                            String content = String.format("您的订单 %s 已成功退款 ¥%.2f。退款原因：%s。",
                                    order.getId(),
                                    order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())),
                                    "预约被取消");
                            notificationService.createPersonalNotification(order.getUserId(), title, content);
                        } else if (orderState != UserOrderEntity.State.CANCELLED) {
                            log.warn("Unexpected order state {} for order ID {}, skipping cancellation",
                                    orderState, order.getId());
                        }
                    });
            reservationMapper.updateState(reservationId, ReservationEntity.State.CANCELLED);
        } else if (state == ReservationEntity.State.CONFIRMED || state == ReservationEntity.State.COMPLETED) {
            throw new BusinessException("Only pending reservation can be cancelled");
        } else if (state == ReservationEntity.State.CANCELLED) {
            throw new BusinessException("The reservation has been cancelled");
        } else {
            throw new BusinessException("Unknown reservation state");
        }
    }

    private static ReservationInfoResult entityToResult(ReservationEntity entity,
                                                        String username,
                                                        String teacherName,
                                                        String subjectName) {
        return ReservationInfoResult.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .username(username)
                .teacherId(entity.getTeacherId())
                .teacherName(teacherName)
                .subjectName(subjectName)
                .startTime(entity.getStartTime())
                .duration(entity.getDuration())
                .state(entity.getState())
                .build();
    }
}
