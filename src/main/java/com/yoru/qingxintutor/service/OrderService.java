package com.yoru.qingxintutor.service;

import com.yoru.qingxintutor.exception.BusinessException;
import com.yoru.qingxintutor.mapper.ReservationMapper;
import com.yoru.qingxintutor.mapper.TeacherMapper;
import com.yoru.qingxintutor.mapper.UserMapper;
import com.yoru.qingxintutor.mapper.UserOrderMapper;
import com.yoru.qingxintutor.pojo.dto.request.OrderCreateRequest;
import com.yoru.qingxintutor.pojo.entity.ReservationEntity;
import com.yoru.qingxintutor.pojo.entity.UserOrderEntity;
import com.yoru.qingxintutor.pojo.result.OrderInfoResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        return entityToResult(order, userMapper.findById(order.getUserId())
                .orElseThrow(() -> new BusinessException("User not found"))
                .getUsername());
    }

    @Transactional
    public void payOrder(String userId, Long id) {
        UserOrderEntity order = orderMapper.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException("Order not found"));
        ReservationEntity reservation = reservationMapper.findByIdAndUserId(order.getReservationId(), userId)
                .orElseThrow(() -> new BusinessException("Reservation not found"));
        if (reservation.getState() != ReservationEntity.State.PENDING)
            throw new BusinessException("Order cant be paid because reservation has been confirmed/canceled");
        if (order.getState() == UserOrderEntity.State.PAID) {
            throw new BusinessException("You have paid the order");
        } else if (order.getState() == UserOrderEntity.State.PENDING) {
            orderMapper.updateState(id, UserOrderEntity.State.PAID);
            walletService.deductBalance(userId, order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())));
        } else if (order.getState() == UserOrderEntity.State.CANCELED) {
            throw new BusinessException("Order has been canceled");
        } else {
            throw new BusinessException("Unknown order state");
        }
    }

    @Transactional
    public void cancelOrder(String teacherUserId, Long id) {
        Long teacherId = teacherMapper.findTeacherIdByUserId(teacherUserId)
                .orElseThrow(() -> new BusinessException("Teacher not found"));
        UserOrderEntity order = orderMapper.findByIdAndTeacherId(id, teacherId)
                .orElseThrow(() -> new BusinessException("Order not found"));
        ReservationEntity reservation = reservationMapper.findByIdAndTeacherId(order.getReservationId(), teacherId)
                .orElseThrow(() -> new BusinessException("Reservation not found"));
        if (reservation.getState() != ReservationEntity.State.PENDING)
            throw new BusinessException("Order cant be canceled because reservation has been confirmed/canceled");

        if (order.getState() == UserOrderEntity.State.PAID) {
            orderMapper.updateState(id, UserOrderEntity.State.CANCELED);
            walletService.addBalance(order.getUserId(), order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())));
        } else if (order.getState() == UserOrderEntity.State.PENDING) {
            orderMapper.updateState(id, UserOrderEntity.State.CANCELED);
        } else if (order.getState() == UserOrderEntity.State.CANCELED) {
            throw new BusinessException("You have canceled the order");
        } else {
            throw new BusinessException("Unknown order state");
        }
    }

    // 定时任务：取消超时未支付订单
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void cancelExpiredPendingOrders() {
        orderMapper.cancelExpiredPendingOrders();
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
