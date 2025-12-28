package com.yoru.qingxintutor.service;

import com.yoru.qingxintutor.exception.BusinessException;
import com.yoru.qingxintutor.mapper.SubjectMapper;
import com.yoru.qingxintutor.mapper.UserEmailMapper;
import com.yoru.qingxintutor.mapper.UserMapper;
import com.yoru.qingxintutor.mapper.UserStudyPlanMapper;
import com.yoru.qingxintutor.pojo.dto.request.StudyPlanCreateRequest;
import com.yoru.qingxintutor.pojo.dto.request.StudyPlanUpdateRequest;
import com.yoru.qingxintutor.pojo.entity.SubjectEntity;
import com.yoru.qingxintutor.pojo.entity.UserEmailEntity;
import com.yoru.qingxintutor.pojo.entity.UserEntity;
import com.yoru.qingxintutor.pojo.entity.UserStudyPlanEntity;
import com.yoru.qingxintutor.pojo.result.StudyPlanInfoResult;
import com.yoru.qingxintutor.utils.EmailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class StudyPlanService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserStudyPlanMapper studyPlanMapper;
    @Autowired
    private SubjectMapper subjectMapper;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private UserEmailMapper emailMapper;
    @Autowired
    private EmailUtils emailUtils;

    @Transactional(readOnly = true)
    public List<StudyPlanInfoResult> listAll(String userId, Boolean completed) {
        return studyPlanMapper.findByUserId(userId, completed)
                .stream()
                .map(studyPlan -> entityToResult(studyPlan,
                        subjectMapper.findById(studyPlan.getSubjectId())
                                .orElseThrow(() -> new BusinessException("Subject not found"))
                                .getSubjectName()))
                .toList();
    }

    public List<StudyPlanInfoResult> listAllBySubjectName(String userId, String subjectName, Boolean completed) {
        return studyPlanMapper.findByUserIdAndSubjectId(userId,
                        subjectMapper.findBySubjectName(subjectName.trim())
                                .orElseThrow(() -> new BusinessException("Subject not found"))
                                .getId(),
                        completed)
                .stream()
                .map(studyPlan -> entityToResult(studyPlan, subjectName.trim()))
                .toList();
    }

    public StudyPlanInfoResult findById(String userId, Long id) {
        UserStudyPlanEntity studyPlan = studyPlanMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Study plan not found"));
        if (!userId.equals(studyPlan.getUserId()))
            throw new BusinessException("Study plan not found");
        return entityToResult(studyPlan,
                subjectMapper.findById(studyPlan.getSubjectId())
                        .orElseThrow(() -> new BusinessException("Subject not found"))
                        .getSubjectName());
    }

    @Transactional
    public StudyPlanInfoResult create(String userId, StudyPlanCreateRequest request) {
        String subjectName = request.getSubjectName().trim();
        SubjectEntity subject = subjectMapper.findBySubjectName(subjectName)
                .orElseThrow(() -> new BusinessException("Subject not found"));

        // 检查是否已存在同名学习计划（唯一性约束）
        if (studyPlanMapper.existsByUserIdAndTitle(userId, request.getTitle())) {
            throw new BusinessException("Plan " + request.getTitle() + " has been created by you");
        }

        // 创建并保存实体
        UserStudyPlanEntity entity = UserStudyPlanEntity.builder()
                .userId(userId)
                .subjectId(subject.getId())
                .title(request.getTitle())
                .content(request.getContent())
                .targetCompletionTime(request.getTargetCompletionTime())
                .reminderTime(request.getReminderTime())
                .completed(false)
                .createTime(LocalDateTime.now())
                .build();
        studyPlanMapper.insert(entity);

        return entityToResult(entity, subject.getSubjectName());
    }

    public StudyPlanInfoResult update(String userId, Long id, StudyPlanUpdateRequest request) {
        Long subjectId = null;
        if (request.getSubjectName() != null) {
            String subjectName = request.getSubjectName().trim();
            subjectId = subjectMapper.findBySubjectName(subjectName)
                    .orElseThrow(() -> new BusinessException("Subject not found"))
                    .getId();
        }

        UserStudyPlanEntity studyPlan = studyPlanMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Study plan not found"));
        if (studyPlan.getCompleted().equals(Boolean.TRUE))
            throw new BusinessException("Study plan cannot be revised because it's completed");
        if (!userId.equals(studyPlan.getUserId()))
            throw new BusinessException("Study plan not found");

        // 检查是否已存在同名学习计划（唯一性约束）
        if (request.getTitle() != null &&
                !id.equals(studyPlanMapper.findByUserIdAndTitle(userId, request.getTitle()).orElse(id))) {
            throw new BusinessException("Plan " + request.getTitle() + " has been created by you");
        }

        // 创建并更新实体
        UserStudyPlanEntity entity = UserStudyPlanEntity.builder()
                .id(id)
                .subjectId(subjectId)
                .title(request.getTitle())
                .content(request.getContent())
                .targetCompletionTime(request.getTargetCompletionTime())
                .reminderTime(request.getReminderTime())
                .build();
        studyPlanMapper.update(entity);
        UserStudyPlanEntity result = studyPlanMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Study plan not found"));

        return entityToResult(result,
                subjectMapper.findById(result.getSubjectId())
                        .orElseThrow(() -> new BusinessException("Subject not found"))
                        .getSubjectName());
    }

    public void complete(String userId, Long id) {
        UserStudyPlanEntity studyPlan = studyPlanMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Study plan not found"));
        if (!userId.equals(studyPlan.getUserId()))
            throw new BusinessException("Study plan not found");
        if (studyPlan.getCompleted())
            throw new BusinessException("Study plan has been completed");
        studyPlan.setCompleted(true);
        studyPlanMapper.update(studyPlan);
    }

    public void deleteById(String userId, Long id) {
        UserStudyPlanEntity studyPlan = studyPlanMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Study plan not found"));
        if (!userId.equals(studyPlan.getUserId()))
            throw new BusinessException("Study plan not found");
        studyPlanMapper.deleteById(id);
    }

    // 定时任务 - 提醒学习计划
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    public void checkAndSendReminders() {
        List<UserStudyPlanEntity> plans = studyPlanMapper.findPlansToRemind();
        for (UserStudyPlanEntity plan : plans) {
            try {
                UserEntity student = userMapper.findById(plan.getUserId())
                        .orElseThrow(() -> new BusinessException("User not found"));
                String title = "学习计划提醒";
                String content = String.format("学习计划提醒: [计划标题] %s, [计划内容] %s, [计划完成时间] %s, [科目] %s",
                        plan.getTitle(), plan.getContent(),
                        plan.getTargetCompletionTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd/HH:mm")),
                        subjectMapper.findById(plan.getSubjectId()).map(SubjectEntity::getSubjectName).orElse("Unknown"));
                notificationService.createPersonalNotification(plan.getUserId(), title, content);
                emailMapper.selectByUserId(student.getId()).map(UserEmailEntity::getEmail).ifPresent(
                        email -> emailUtils.sendStudyPlanReminderToStudent(email,
                                student.getUsername(), plan.getTitle(), plan.getContent(), plan.getTargetCompletionTime())
                );
                log.debug("Scheduled task: success to send studyplan reminder for plan ID: {}", plan.getId());
            } catch (Exception e) {
                log.error("Failed to send reminder for plan ID: {}, {}", plan.getId()
                        , e.getMessage());
            }
        }
    }


    private static StudyPlanInfoResult entityToResult(UserStudyPlanEntity entity, String subjectName) {
        return StudyPlanInfoResult.builder()
                .id(entity.getId())
                .subjectName(subjectName)
                .title(entity.getTitle())
                .content(entity.getContent())
                .targetCompletionTime(entity.getTargetCompletionTime())
                .reminderTime(entity.getReminderTime())
                .completed(entity.getCompleted())
                .build();
    }
}
