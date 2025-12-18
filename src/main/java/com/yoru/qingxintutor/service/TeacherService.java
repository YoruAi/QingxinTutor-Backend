package com.yoru.qingxintutor.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yoru.qingxintutor.exception.BusinessException;
import com.yoru.qingxintutor.mapper.SubjectMapper;
import com.yoru.qingxintutor.mapper.TeacherMapper;
import com.yoru.qingxintutor.mapper.TeacherSubjectMapper;
import com.yoru.qingxintutor.pojo.dto.request.TeacherSearchRequest;
import com.yoru.qingxintutor.pojo.dto.request.TeacherUpdateRequest;
import com.yoru.qingxintutor.pojo.entity.SubjectEntity;
import com.yoru.qingxintutor.pojo.entity.TeacherEntity;
import com.yoru.qingxintutor.pojo.result.TeacherInfoResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Supplier;

@Service
public class TeacherService {
    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private TeacherSubjectMapper teacherSubjectMapper;
    @Autowired
    private SubjectMapper subjectMapper;
    @Autowired
    private AvatarService avatarService;

    /**
     * 根据user_id查询该教师信息
     */
    public TeacherInfoResult getInfoByUserId(String userId) {
        Optional<TeacherInfoResult> teacher = teacherMapper.findByUserId(userId);
        if (teacher.isEmpty())
            throw new BusinessException("Teacher not found");
        return teacher.get();
    }

    /**
     * 根据id查询该教师信息
     */
    public TeacherInfoResult getInfoById(Long id) {
        Optional<TeacherInfoResult> teacher = teacherMapper.findById(id);
        if (teacher.isEmpty())
            throw new BusinessException("Teacher not found");
        return teacher.get();
    }

    /**
     * 根据teacher_id查询该教师名
     */
    public String getNameById(Long id) {
        Optional<String> teacherName = teacherMapper.findNameById(id);
        if (teacherName.isEmpty())
            throw new BusinessException("Teacher not found");
        return teacherName.get();
    }

    /**
     * 根据user_id更新该教师信息
     */
    @Transactional
    public void updateInfoByUserId(String userId, TeacherUpdateRequest request) {
        // 根据 user_id 查询教师id
        Long teacherId = teacherMapper.findTeacherIdByUserId(userId)
                .orElseThrow(() -> new BusinessException("Teacher not found"));
        if (request.getPhone() != null) {
            Optional<TeacherInfoResult> teacher = teacherMapper.findByPhone(request.getPhone());
            if (teacher.isPresent() && !userId.equals(teacher.get().getTeacher().getUserId())) {
                throw new BusinessException("Phone is repeated with other teachers' phone");
            }
        }
        TeacherEntity updateTeacher = TeacherEntity.builder()
                .id(teacherId)
                .userId(userId)
                .phone(request.getPhone())
                .nickname(request.getNickname())
                .name(request.getName())
                .gender(request.getGender())
                .birthDate(request.getBirthDate())
                .address(request.getAddress())
                .teachingExperience(request.getTeachingExperience())
                .description(request.getDescription())
                .grade(request.getGrade())
                .build();
        teacherMapper.update(updateTeacher);

        // 更新科目信息
        List<Long> currentSubjectIds = teacherSubjectMapper.findByTeacherId(teacherId)
                .stream()
                .map(SubjectEntity::getId)
                .toList();
        Set<Long> currentSet = new HashSet<>(currentSubjectIds);
        // 获取请求中的新 subjectId 列表
        List<String> subjectNames = request.getSubjectNames();
        List<Long> newSubjectIds = new ArrayList<>();
        if (subjectNames != null && !subjectNames.isEmpty()) {
            List<String> validSubjects = subjectNames.stream()
                    .filter(s -> s != null && !s.isBlank())
                    .map(String::trim)
                    .distinct()
                    .toList();

            newSubjectIds = subjectMapper.findIdsByNames(validSubjects);
            if (newSubjectIds.size() != validSubjects.size()) {
                throw new BusinessException("One or more subjects do not exist");
            }
        }
        Set<Long> newSet = new HashSet<>(newSubjectIds);
        // 计算差异
        Set<Long> toDelete = new HashSet<>(currentSet);
        toDelete.removeAll(newSet);
        Set<Long> toInsert = new HashSet<>(newSet);
        toInsert.removeAll(currentSet);
        // 执行删除
        if (!toDelete.isEmpty())
            teacherSubjectMapper.deleteByTeacherIdAndSubjectIds(teacherId, new ArrayList<>(toDelete));
        // 执行插入
        if (!toInsert.isEmpty())
            teacherSubjectMapper.batchInsert(teacherId, new ArrayList<>(toInsert));
    }

    /**
     * 根据user_id更新教师头像
     */
    public void updateAvatarByUserId(String userId, String icon, String oldIcon) {
        if (!icon.startsWith("/avatar/"))
            throw new BusinessException("Avatar URL error, please contact admin");
        Long teacherId = teacherMapper.findTeacherIdByUserId(userId)
                .orElseThrow(() -> new BusinessException("Teacher not found"));
        teacherMapper.updateIconById(teacherId, icon);
        avatarService.deleteAvatar(oldIcon);
    }

    /**
     * 查询所有优秀 (avg_rating>=4.00) 教师信息
     */
    public PageInfo<TeacherInfoResult> listExcellent(Integer pageNum, Integer pageSize) {
        return queryPageByIds(teacherMapper::findExcellentIds, pageNum, pageSize);
    }

    /**
     * 查询所有教师信息
     */
    public PageInfo<TeacherInfoResult> listAll(Integer pageNum, Integer pageSize) {
        return queryPageByIds(teacherMapper::findAllIds, pageNum, pageSize);
    }

    /**
     * 根据搜索条件查询教师信息
     */
    public PageInfo<TeacherInfoResult> filter(TeacherSearchRequest request, Integer pageNum, Integer pageSize) {
        return queryPageByIds(() -> teacherMapper.findIdsByCriteria(request), pageNum, pageSize);
    }

    private PageInfo<TeacherInfoResult> queryPageByIds(
            Supplier<List<Long>> idSupplier,
            Integer pageNum, Integer pageSize) {
        // 1. 分页查 ID
        Page<Long> idPage = PageHelper.startPage(pageNum, pageSize);
        List<Long> teacherIds = idSupplier.get();
        long total = idPage.getTotal();

        // 2. 查详情
        List<TeacherInfoResult> teachers = teacherIds.isEmpty()
                ? Collections.emptyList()
                : teacherMapper.findByIds(teacherIds);

        // 3. 构造分页结果
        Page<TeacherInfoResult> page = new Page<>(pageNum, pageSize);
        page.addAll(teachers);
        page.setTotal(total);
        return new PageInfo<>(page);
    }
}
