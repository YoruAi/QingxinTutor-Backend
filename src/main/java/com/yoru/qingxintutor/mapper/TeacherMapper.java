package com.yoru.qingxintutor.mapper;

import com.yoru.qingxintutor.pojo.dto.request.TeacherSearchRequest;
import com.yoru.qingxintutor.pojo.entity.TeacherEntity;
import com.yoru.qingxintutor.pojo.result.TeacherInfoResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface TeacherMapper {

    int insert(TeacherEntity teacher);

    int update(TeacherEntity teacher);

    int updateByUserId(TeacherEntity teacher);

    int updateIconByUserId(@Param("userId") String userId, @Param("icon") String icon);

    Optional<String> findNameById(@Param("id") Long id);

    /**
     * 根据 ID 查询老师是否存在
     */
    boolean existsById(@Param("id") Long id);

    Optional<Long> findTeacherIdByUserId(@Param("userId") String userId);

    Optional<String> findUserIdByTeacherId(@Param("id") Long id);

    // 以下返回TeacherInfoResult封装 //
    Optional<TeacherInfoResult> findById(@Param("id") Long id);

    Optional<TeacherInfoResult> findByUserId(@Param("userId") String userId);

    Optional<TeacherInfoResult> findByPhone(@Param("phone") String phone);

    List<TeacherInfoResult> findByGrade(@Param("grade") Integer grade);

    List<TeacherInfoResult> findBySubjectId(@Param("subjectId") Long subjectId);

    List<TeacherInfoResult> findBySubjectName(@Param("subjectName") String subjectName);

    List<TeacherInfoResult> findAll();

    List<Long> findExcellentIds();

    List<Long> findIdsByCriteria(TeacherSearchRequest request);

    List<TeacherInfoResult> findByIds(@Param("list") List<Long> list);

    List<Long> findAllIds();

    long countAll();
}
