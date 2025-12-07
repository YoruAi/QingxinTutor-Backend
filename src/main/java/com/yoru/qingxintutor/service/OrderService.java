package com.yoru.qingxintutor.service;

import com.yoru.qingxintutor.exception.BusinessException;
import com.yoru.qingxintutor.mapper.*;
import com.yoru.qingxintutor.pojo.dto.request.OrderCreateRequest;
import com.yoru.qingxintutor.pojo.dto.request.OrderPayRequest;
import com.yoru.qingxintutor.pojo.entity.ReservationEntity;
import com.yoru.qingxintutor.pojo.entity.UserEmailEntity;
import com.yoru.qingxintutor.pojo.entity.UserEntity;
import com.yoru.qingxintutor.pojo.entity.UserOrderEntity;
import com.yoru.qingxintutor.pojo.result.OrderInfoResult;
import com.yoru.qingxintutor.utils.EmailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OrderService {

    @Autowired
    private UserOrderMapper orderMapper;

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private WalletService walletService;

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserEmailMapper emailMapper;

    @Autowired
    private EmailUtils emailUtils;

    public OrderInfoResult getTeacherOrder(String teacherUserId, Long id) {
        UserOrderEntity order = orderMapper.findByIdAndTeacherId(id, teacherMapper.findTeacherIdByUserId(teacherUserId)
                .orElseThrow(() -> new BusinessException("Teacher not found"))
        ).orElseThrow(() -> new BusinessException("Order not found"));
        return entityToResult(order, userMapper.findById(order.getUserId())
                .orElseThrow(() -> new BusinessException("User not found"))
                .getUsername());
    }

    public OrderInfoResult getStudentOrder(String userId, Long id) {
        UserOrderEntity order = orderMapper.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException("Order not found"));
        return entityToResult(order, userMapper.findById(order.getUserId())
                .orElseThrow(() -> new BusinessException("User not found"))
                .getUsername());
    }

    public List<OrderInfoResult> listTeacherOrders(String teacherUserId, Long reservationId, UserOrderEntity.State state) {
        Long teacherId = teacherMapper.findTeacherIdByUserId(teacherUserId)
                .orElseThrow(() -> new BusinessException("Teacher not found"));
        return orderMapper.selectOrdersByTeacherId(teacherId, reservationId, state)
                .stream()
                .map(order -> entityToResult(order, userMapper.findById(order.getUserId())
                        .orElseThrow(() -> new BusinessException("User not found"))
                        .getUsername()))
                .toList();
    }

    public List<OrderInfoResult> listStudentOrders(String userId, Long reservationId, UserOrderEntity.State state) {
        return orderMapper.selectOrdersByUserId(userId, reservationId, state)
                .stream()
                .map(order -> entityToResult(order, userMapper.findById(order.getUserId())
                        .orElseThrow(() -> new BusinessException("User not found"))
                        .getUsername()))
                .toList();
    }

    public OrderInfoResult createOrder(String teacherUserId, Long reservationId, OrderCreateRequest request) {
        Long teacherId = teacherMapper.findTeacherIdByUserId(teacherUserId)
                .orElseThrow(() -> new BusinessException("Teacher not found"));
        ReservationEntity reservation = reservationMapper.findByIdAndTeacherId(reservationId, teacherId)
                .orElseThrow(() -> new BusinessException("Reservation not found"));
        if (reservation.getState() != ReservationEntity.State.PENDING)
            throw new BusinessException("Cant create order for this reservation any more");
        UserOrderEntity order = UserOrderEntity.builder()
                .userId(reservation.getUserId())
                .reservationId(reservationId)
                .item(request.getItem())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .state(UserOrderEntity.State.PENDING)
                .createTime(LocalDateTime.now())
                .build();
        orderMapper.insert(order);

        UserEntity student = userMapper.findById(order.getUserId())
                .orElseThrow(() -> new BusinessException("User not found"));
        BigDecimal total = order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity()));
        String title = "新的未支付订单";
        String content = String.format("您有新的订单需要支付：单价 %.2f 元，数量 %d，总计 %.2f 元，请及时完成支付。",
                order.getPrice(),
                order.getQuantity(),
                total);
        notificationService.createPersonalNotification(order.getUserId(), title, content);
        emailMapper.selectByUserId(student.getId()).map(UserEmailEntity::getEmail).ifPresent(
                email -> emailUtils.sendOrderCreatedToStudent(email,
                        student.getUsername(),
                        order.getPrice(),
                        order.getQuantity(),
                        total
                )
        );


        return entityToResult(order, userMapper.findById(order.getUserId())
                .orElseThrow(() -> new BusinessException("User not found"))
                .getUsername());
    }

    @Transactional
    public void payOrder(String userId, Long id, OrderPayRequest request) {
        UserOrderEntity order = orderMapper.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException("Order not found"));
        ReservationEntity reservation = reservationMapper.findByIdAndUserId(order.getReservationId(), userId)
                .orElseThrow(() -> new BusinessException("Reservation not found"));
        if (reservation.getState() != ReservationEntity.State.PENDING)
            throw new BusinessException("Order cant be paid because reservation has been confirmed/cancelled");

        UserOrderEntity.State state = order.getState();
        if (state == UserOrderEntity.State.PAID) {
            throw new BusinessException("You have paid the order");
        } else if (state == UserOrderEntity.State.PENDING) {
            BigDecimal needPay = order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity()));
            Set<Long> voucherIds = new LinkedHashSet<>(request.getVoucherIds());
            for (Long voucherId : voucherIds) {
                BigDecimal amount = voucherService.findById(userId, voucherId).getAmount();
                needPay = needPay.subtract(amount);
                voucherService.useVoucher(userId, voucherId);
            }
            needPay = needPay.max(BigDecimal.ZERO);
            walletService.deductBalance(userId, needPay);
            orderMapper.updateState(id, UserOrderEntity.State.PAID);
        } else if (state == UserOrderEntity.State.CANCELLED) {
            throw new BusinessException("Order has been cancelled");
        } else {
            throw new BusinessException("Unknown order state");
        }
    }

    @Transactional
    public void cancelOrderByTeacher(String teacherUserId, Long id) {
        Long teacherId = teacherMapper.findTeacherIdByUserId(teacherUserId)
                .orElseThrow(() -> new BusinessException("Teacher not found"));
        UserOrderEntity order = orderMapper.findByIdAndTeacherId(id, teacherId)
                .orElseThrow(() -> new BusinessException("Order not found"));
        ReservationEntity reservation = reservationMapper.findByIdAndTeacherId(order.getReservationId(), teacherId)
                .orElseThrow(() -> new BusinessException("Reservation not found"));
        if (reservation.getState() != ReservationEntity.State.PENDING)
            throw new BusinessException("Order cant be cancelled because reservation has been confirmed/cancelled");

        cancelOrder(order);

        String title = "退款已处理";
        String content = String.format("您的订单 %s 已成功退款 ¥%.2f。退款原因：%s。",
                order.getId(),
                order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())),
                "教师取消订单");
        notificationService.createPersonalNotification(order.getUserId(), title, content);
    }

    @Transactional
    public void cancelOrderByStudent(String userId, Long id) {
        UserOrderEntity order = orderMapper.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException("Order not found"));
        ReservationEntity reservation = reservationMapper.findByIdAndUserId(order.getReservationId(), userId)
                .orElseThrow(() -> new BusinessException("Reservation not found"));
        if (reservation.getState() != ReservationEntity.State.PENDING)
            throw new BusinessException("Order cant be cancelled because reservation has been confirmed/cancelled");

        cancelOrder(order);

        String title = "退款已处理";
        String content = String.format("您的订单 %s 已成功退款 ¥%.2f。退款原因：%s。",
                order.getId(),
                order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())),
                "学生取消订单");
        notificationService.createPersonalNotification(order.getUserId(), title, content);
    }

    // 定时任务 - 取消超时未支付订单
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void cancelExpiredPendingOrders() {
        int updated = orderMapper.cancelExpiredPendingOrders();
        if (updated != 0)
            log.debug("Scheduled task: success to auto cancel {} order(s)", updated);
    }


    @Transactional
    public void cancelOrder(UserOrderEntity order) {
        UserOrderEntity.State state = order.getState();
        Long orderId = order.getId();
        if (state == UserOrderEntity.State.PAID) {
            walletService.addBalance(order.getUserId(), order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())));
            orderMapper.updateState(orderId, UserOrderEntity.State.CANCELLED);
        } else if (state == UserOrderEntity.State.PENDING) {
            orderMapper.updateState(orderId, UserOrderEntity.State.CANCELLED);
        } else if (state == UserOrderEntity.State.CANCELLED) {
            throw new BusinessException("The order has been cancelled");
        } else {
            throw new BusinessException("Unknown order state");
        }
    }

    private static OrderInfoResult entityToResult(UserOrderEntity entity, String username) {
        return OrderInfoResult.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .username(username)
                .reservationId(entity.getReservationId())
                .item(entity.getItem())
                .quantity(entity.getQuantity())
                .price(entity.getPrice())
                .state(entity.getState())
                .createTime(entity.getCreateTime())
                .build();
    }
}
