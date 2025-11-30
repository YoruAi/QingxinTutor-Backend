package com.yoru.qingxintutor.mapper;

import com.yoru.qingxintutor.pojo.entity.UserOrderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserOrderMapper {
    /**
     * 根据订单ID和用户ID查询订单（用于权限校验：确保用户只能操作自己的订单）
     */
    Optional<UserOrderEntity> findByIdAndUserId(@Param("id") Long id, @Param("userId") String userId);

    /**
     * 查询某用户的所有订单（按创建时间倒序）
     */
    List<UserOrderEntity> findByUserId(@Param("userId") String userId);

    /**
     * 根据订单ID和教师ID查询订单（用于权限校验：确保教师只能操作自己课程相关的订单）
     */
    Optional<UserOrderEntity> findByIdAndTeacherId(@Param("id") Long id, @Param("teacherId") Long teacherId);

    /**
     * 查询某用户在指定状态下的所有订单（如 PENDING、PAID、CANCELLED）
     */
    List<UserOrderEntity> findByUserIdAndState(@Param("userId") String userId, @Param("state") String state);

    /**
     * 根据预约ID查询所有关联的订单
     */
    List<UserOrderEntity> findByReservationId(@Param("reservationId") Long reservationId);

    /**
     * 根据用户ID和预约ID联合查询订单（用于双重校验：确保用户与预约匹配）
     */
    List<UserOrderEntity> findByUserIdAndReservationId(@Param("userId") String userId, @Param("reservationId") Long reservationId);

    /**
     * 插入一条新订单记录
     */
    void insert(UserOrderEntity order);

    /**
     * 更新订单状态
     */
    int updateState(@Param("id") Long id, @Param("state") UserOrderEntity.State state);

    /**
     * 批量取消所有超时未支付的订单（创建时间超过15分钟且状态为 PENDING）
     */
    int cancelExpiredPendingOrders();

    /**
     * 检查某个预约下的所有订单是否均已支付（状态为 PAID）
     */
    boolean areAllOrdersPaid(@Param("reservationId") Long reservationId);

    List<UserOrderEntity> selectOrdersByTeacherId(
            @Param("teacherId") Long teacherId,
            @Param("reservationId") Long reservationId,
            @Param("state") UserOrderEntity.State state
    );

    List<UserOrderEntity> selectOrdersByUserId(
            @Param("userId") String userId,
            @Param("reservationId") Long reservationId,
            @Param("state") UserOrderEntity.State state
    );
}
