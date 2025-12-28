package com.yoru.qingxintutor.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yoru.qingxintutor.exception.BusinessException;
import com.yoru.qingxintutor.mapper.TeacherMapper;
import com.yoru.qingxintutor.mapper.TeacherReviewMapper;
import com.yoru.qingxintutor.mapper.UserMapper;
import com.yoru.qingxintutor.pojo.request.ReviewCreateRequest;
import com.yoru.qingxintutor.pojo.request.ReviewUpdateRequest;
import com.yoru.qingxintutor.pojo.entity.TeacherReviewEntity;
import com.yoru.qingxintutor.pojo.result.ReviewInfoResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {
    @Autowired
    private TeacherReviewMapper reviewMapper;
    @Autowired
    private TeacherService teacherService;
    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private TeacherReviewMapper teacherReviewMapper;
    @Autowired
    private UserMapper userMapper;

    public List<ReviewInfoResult> listAllByUserId(String userId) {
        String username = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"))
                .getUsername();
        return reviewMapper.findByUserId(userId)
                .stream()
                .map(entity -> entityToResult(entity, username, teacherService.getNameById(entity.getTeacherId())))
                .toList();
    }

    public ReviewInfoResult findById(Long id) {
        TeacherReviewEntity review = reviewMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Review not found"));
        return entityToResult(review,
                userMapper.findById(review.getUserId())
                        .orElseThrow(() -> new BusinessException("User not found"))
                        .getUsername(),
                teacherService.getNameById(review.getTeacherId()));
    }

    /**
     * 根据id查询该教师所有评论
     */
    public PageInfo<ReviewInfoResult> findReviewsByTeacherId(Long teacherId, Integer pageNum, Integer pageSize) {
        String teacherName = teacherMapper.findNameById(teacherId)
                .orElseThrow(() -> new BusinessException("Teacher not found"));
        PageHelper.startPage(pageNum, pageSize);
        List<ReviewInfoResult> list = teacherReviewMapper.findByTeacherId(teacherId)
                .stream()
                .map(entity -> entityToResult(entity,
                        userMapper.findById(entity.getUserId())
                                .orElseThrow(() -> new BusinessException("User not found"))
                                .getUsername(),
                        teacherName))
                .toList();
        return new PageInfo<>(list);
    }

    /**
     * 根据id查询该教师我的评论
     */
    public ReviewInfoResult findReviewsByTeacherIdAndStudentId(Long teacherId, String studentId) {
        String teacherName = teacherMapper.findNameById(teacherId)
                .orElseThrow(() -> new BusinessException("Teacher not found"));
        return reviewMapper.findByUserIdAndTeacherId(studentId, teacherId)
                .map(entity -> entityToResult(entity,
                        userMapper.findById(entity.getUserId())
                                .orElseThrow(() -> new BusinessException("User not found"))
                                .getUsername(),
                        teacherName))
                .orElse(null);
    }

    @Transactional
    public ReviewInfoResult create(String userId, ReviewCreateRequest request) {
        String teacherName = teacherService.getNameById(request.getTeacherId());

        // 检查是否已存在对该教师的评价（唯一性约束）
        if (reviewMapper.findByUserIdAndTeacherId(userId, request.getTeacherId()).isPresent()) {
            throw new BusinessException("Teacher has been rated by you");
        }

        // 创建并保存实体
        TeacherReviewEntity entity = TeacherReviewEntity.builder()
                .userId(userId)
                .teacherId(request.getTeacherId())
                .title(request.getTitle())
                .content(request.getContent())
                .rating(request.getRating())
                .createTime(LocalDateTime.now())
                .build();
        reviewMapper.insert(entity);

        return entityToResult(entity, userMapper.findById(entity.getUserId())
                .orElseThrow(() -> new BusinessException("User not found"))
                .getUsername(), teacherName);
    }

    public ReviewInfoResult update(String userId, Long id, ReviewUpdateRequest request) {
        TeacherReviewEntity review = reviewMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Review not found"));
        if (!userId.equals(review.getUserId()))
            throw new BusinessException("Review not found");

        // 更新实体
        TeacherReviewEntity entity = TeacherReviewEntity.builder()
                .id(id)
                .title(request.getTitle())
                .content(request.getContent())
                .rating(request.getRating())
                .build();
        reviewMapper.update(entity);

        review = reviewMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Review not found"));
        return entityToResult(review, userMapper.findById(review.getUserId())
                .orElseThrow(() -> new BusinessException("User not found"))
                .getUsername(), teacherService.getNameById(review.getTeacherId()));
    }

    public void deleteById(String userId, Long id) {
        TeacherReviewEntity review = reviewMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Review not found"));
        if (!userId.equals(review.getUserId()))
            throw new BusinessException("Review not found");
        reviewMapper.deleteById(id);
    }

    private static ReviewInfoResult entityToResult(TeacherReviewEntity entity, String username, String teacherName) {
        return ReviewInfoResult.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .username(username)
                .teacherId(entity.getTeacherId())
                .teacherName(teacherName)
                .rating(entity.getRating())
                .title(entity.getTitle())
                .content(entity.getContent())
                .createTime(entity.getCreateTime())
                .build();
    }
}
