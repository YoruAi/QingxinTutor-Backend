package com.yoru.qingxintutor.service;

import com.yoru.qingxintutor.exception.BusinessException;
import com.yoru.qingxintutor.mapper.UserVoucherMapper;
import com.yoru.qingxintutor.pojo.entity.UserVoucherEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VoucherService {

    public static final int voucherExpireIn = 3;    // days

    @Autowired
    private UserVoucherMapper voucherMapper;

    @Autowired
    private WalletService walletService;

    public List<UserVoucherEntity> listAllByUserId(String userId) {
        voucherMapper.deleteExpired();
        return voucherMapper.findByUserId(userId);
    }

    public UserVoucherEntity findById(String userId, Long id) {
        voucherMapper.deleteExpired();
        UserVoucherEntity voucher = voucherMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Voucher not found"));
        if (!userId.equals(voucher.getUserId()))
            throw new BusinessException("Voucher not found");
        if (voucher.getExpireTime().isBefore(LocalDateTime.now()))
            throw new BusinessException("Voucher is expired");
        return voucher;
    }

    @Transactional
    public void useVoucher(String userId, Long id) {
        voucherMapper.deleteExpired();
        UserVoucherEntity voucher = voucherMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Voucher not found"));
        if (!userId.equals(voucher.getUserId()))
            throw new BusinessException("Voucher not found");
        if (voucher.getExpireTime().isBefore(LocalDateTime.now()))
            throw new BusinessException("Voucher is expired");
        voucherMapper.deleteById(id);
        walletService.addBalance(userId, voucher.getAmount());
    }

    public UserVoucherEntity issue(String userId, BigDecimal amount) {
        LocalDateTime now = LocalDateTime.now();
        UserVoucherEntity voucher = UserVoucherEntity.builder()
                .userId(userId)
                .amount(amount)
                .createTime(now)
                .expireTime(now.plusDays(voucherExpireIn))
                .build();
        voucherMapper.insert(voucher);
        return voucher;
    }
}
