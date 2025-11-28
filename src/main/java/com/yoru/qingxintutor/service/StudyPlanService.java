package com.yoru.qingxintutor.service;

import com.yoru.qingxintutor.exception.BusinessException;
import com.yoru.qingxintutor.mapper.SubjectMapper;
import com.yoru.qingxintutor.mapper.UserStudyPlanMapper;
import com.yoru.qingxintutor.pojo.dto.request.StudyPlanCreateRequest;
import com.yoru.qingxintutor.pojo.dto.request.StudyPlanUpdateRequest;
import com.yoru.qingxintutor.pojo.entity.SubjectEntity;
import com.yoru.qingxintutor.pojo.entity.UserStudyPlanEntity;
import com.yoru.qingxintutor.pojo.result.StudyPlanInfoResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudyPlanService {
    @Autowired
    private UserStudyPlanMapper studyPlanMapper;

    @Autowired
    private SubjectMapper subjectMapper;

    public List<StudyPlanInfoResult> listAll(String userId) {
        return studyPlanMapper.findByUserId(userId)
                .stream()
                .map(studyPlan -> entityToResult(studyPlan,
                        subjectMapper.findById(studyPlan.getSubjectId())
                                .orElseThrow(() -> new BusinessException("Subject not found"))
                                .getSubjectName()))
                .toList();
    }

    public List<StudyPlanInfoResult> listAllBySubjectName(String userId, String subjectName) {
        return studyPlanMapper.findByUserIdAndSubjectId(userId,
                        subjectMapper.findBySubjectName(subjectName.trim())
                                .orElseThrow(() -> new BusinessException("Subject not found"))
                                .getId())
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

    public void deleteById(String userId, Long id) {
        UserStudyPlanEntity studyPlan = studyPlanMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Study plan not found"));
        if (!userId.equals(studyPlan.getUserId()))
            throw new BusinessException("Study plan not found");
        studyPlanMapper.deleteById(id);
    }


    private static StudyPlanInfoResult entityToResult(UserStudyPlanEntity entity, String subjectName) {
        return StudyPlanInfoResult.builder()
                .id(entity.getId())
                .subjectName(subjectName)
                .title(entity.getTitle())
                .content(entity.getContent())
                .targetCompletionTime(entity.getTargetCompletionTime())
                .reminderTime(entity.getReminderTime())
                .build();
    }
}
