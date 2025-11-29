package com.yoru.qingxintutor.service;

import com.yoru.qingxintutor.exception.BusinessException;
import com.yoru.qingxintutor.mapper.SubjectMapper;
import com.yoru.qingxintutor.mapper.TeacherMapper;
import com.yoru.qingxintutor.mapper.TeacherSubjectMapper;
import com.yoru.qingxintutor.pojo.entity.SubjectEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubjectService {

    @Autowired
    private SubjectMapper subjectMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private TeacherSubjectMapper teacherSubjectMapper;

    public List<SubjectEntity> listAll() {
        return subjectMapper.findAll();
    }

    public SubjectEntity getById(Long id) {
        return subjectMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Subject not found"));
    }

    public SubjectEntity getByName(String subjectName) {
        Optional<SubjectEntity> subject = subjectMapper.findBySubjectName(subjectName);
        if (subject.isEmpty())
            throw new BusinessException("Subject not found");
        return subject.get();
    }

    /**
     * 根据id查询该教师所有科目
     */
    public List<SubjectEntity> getSubjectsByTeacherId(Long teacherId) {
        if (!teacherMapper.existsById(teacherId))
            throw new BusinessException("Teacher not found");
        return teacherSubjectMapper.findByTeacherId(teacherId);
    }
}
