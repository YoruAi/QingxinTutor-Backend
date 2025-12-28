package com.yoru.qingxintutor.service;

import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import com.yoru.qingxintutor.exception.BusinessException;
import com.yoru.qingxintutor.mapper.TeacherMapper;
import com.yoru.qingxintutor.mapper.TeacherReviewMapper;
import com.yoru.qingxintutor.pojo.entity.ForumEntity;
import com.yoru.qingxintutor.pojo.entity.ReservationEntity;
import com.yoru.qingxintutor.pojo.entity.TeacherReviewEntity;
import com.yoru.qingxintutor.pojo.result.ForumMessageInfoResult;
import com.yoru.qingxintutor.pojo.result.ReservationInfoResult;
import com.yoru.qingxintutor.pojo.result.StudyPlanInfoResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AnalyticsService {
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private ForumService forumService;
    @Autowired
    private ForumMessageService forumMessageService;
    @Autowired
    private StudyPlanService studyPlanService;
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private TeacherReviewMapper reviewMapper;

    private final String model;
    private final ArkService arkService;

    public AnalyticsService(@Value("${app.ai.api-key}") String apiKey,
                            @Value("${app.ai.base-url}") String baseUrl,
                            @Value("${app.ai.model}") String model) {
        this.model = model;
        arkService = ArkService.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .build();
    }

    private String analyze(String data) {
        ChatMessage userMessage = ChatMessage.builder()
                .role(ChatMessageRole.USER)
                .content(data)
                .build();
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(model)
                .messages(List.of(userMessage))
                .build();
        try {
            // 获取响应并打印每个选择的消息内容
            StringBuilder resultBuilder = new StringBuilder();
            for (var choice : arkService.createChatCompletion(chatCompletionRequest).getChoices()) {
                resultBuilder.append(choice.getMessage().getContent());
            }
            return resultBuilder.toString();
        } catch (Exception e) {
            log.error("Error when ai request: {}", e.getMessage());
            return "AI生成时出错，请稍后重试。";
        } finally {
            arkService.shutdownExecutor();
        }
    }

    public String learningRecommend(String userId) {
        try {
            List<StudyPlanInfoResult> plans = studyPlanService.listAll(userId, null)
                    .stream().limit(6).toList();
            List<ReservationInfoResult> reservations = reservationService.listStudentReservations(userId, null)
                    .stream().limit(6).toList();

            // === 构建学习计划摘要 ===
            StringBuilder planSummary = new StringBuilder();
            long totalPlans = plans.size();
            long completedPlans = plans.stream().filter(p -> Boolean.TRUE.equals(p.getCompleted())).count();
            planSummary.append("最近的 ").append(totalPlans).append(" 项学习计划，完成了 ").append(completedPlans).append(" 项。");
            if (!plans.isEmpty()) {
                Set<String> subjects = plans.stream()
                        .map(StudyPlanInfoResult::getSubjectName)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                if (!subjects.isEmpty()) {
                    planSummary.append("涉及科目：").append(String.join("、", subjects)).append("。");
                }
                // 列出已完成计划的标题
                List<String> completedTitles = plans.stream()
                        .filter(p -> Boolean.TRUE.equals(p.getCompleted()))
                        .map(StudyPlanInfoResult::getTitle)
                        .filter(Objects::nonNull)
                        .toList();
                if (!completedTitles.isEmpty()) {
                    planSummary.append("已完成计划：《").append(String.join("》《", completedTitles)).append("》。");
                }
                // 列出未完成计划的标题
                List<String> incompleteTitles = plans.stream()
                        .filter(p -> !Boolean.TRUE.equals(p.getCompleted()))
                        .map(StudyPlanInfoResult::getTitle)
                        .filter(Objects::nonNull)
                        .toList();
                if (!incompleteTitles.isEmpty()) {
                    planSummary.append("待完成计划：《").append(String.join("》《", incompleteTitles)).append("》。");
                }
            }

            // === 构建预约记录摘要 ===
            StringBuilder reservationSummary = new StringBuilder();
            long totalRes = reservations.size();
            long completedRes = reservations.stream()
                    .filter(r -> ReservationEntity.State.COMPLETED.equals(r.getState()))
                    .count();
            long cancelledRes = reservations.stream()
                    .filter(r -> ReservationEntity.State.CANCELLED.equals(r.getState()))
                    .count();
            reservationSummary.append("最近 ").append(totalRes).append(" 次预约，完成 ").append(completedRes)
                    .append(" 次，取消 ").append(cancelledRes).append(" 次。");
            // 列出具体预约（时间、科目、状态）
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("M月d日HH:mm");
            List<String> recentDetails = reservations.stream()
                    .map(r -> {
                        String time = r.getStartTime().format(dtf);
                        String subject = r.getSubjectName() != null ? r.getSubjectName() : "未知科目";
                        String status = switch (r.getState()) {
                            case PENDING -> "待确认";
                            case CONFIRMED -> "已确认待上课";
                            case COMPLETED -> "已结课";
                            case CANCELLED -> "被拒绝/取消";
                        };
                        return String.format("%s %s课 %s", time, subject, status);
                    })
                    .collect(Collectors.toList());
            if (!recentDetails.isEmpty()) {
                reservationSummary.append("具体如下：").append(String.join("；", recentDetails)).append("。");
            }

            String prompt = """
                    你是一位资深教育顾问。请根据以下学生近期数据，生成一份个性化的学习提升建议：
                    【近期学习计划情况摘要】
                    %s
                    【近期预约记录摘要】
                    %s
                    要求：
                    1. 结合具体计划标题和预约科目，指出可能的薄弱环节；
                    2. 推荐1-2个具体学习目标（如“掌握二次函数图像变换”）；
                    3. 语言亲切、鼓励式，不超过200字，记住一定不要使用markdown！
                    """.formatted(planSummary.toString(), reservationSummary.toString());

            log.debug("AI request to generate learning recommend - Prompt length {}", prompt.length());
            return analyze(prompt);
        } catch (Exception e) {
            log.error("Failed to generate learning recommend for user {}: {}", userId, e.getMessage());
            return "生成学习建议时出错，请稍后重试。";
        }
    }

    // 定时任务 - 每周一凌晨2点AI整理论坛消息
    @Scheduled(cron = "0 0 2 * * MON")
    public void generateWeeklyDigest() {
        try {
            // 1. 获取所有板块的本周消息
            List<ForumEntity> forums = forumService.listAll();
            List<String> summaries = new ArrayList<>();
            for (ForumEntity forum : forums) {
                List<ForumMessageInfoResult> messages = forumMessageService.listWeeklyByForumId(forum.getId());
                if (messages.isEmpty()) continue;
                // 提取纯文本内容
                List<String> contents = messages.stream()
                        .map(ForumMessageInfoResult::getContent)
                        .filter(content -> content != null && !content.trim().isEmpty())
                        .map(String::trim)
                        .limit(50)
                        .toList();
                // 构建板块摘要
                String blockSummary = "【" + forum.getName() + "】\n" +
                        contents.stream()
                                .map(msg -> "• " + msg)
                                .collect(Collectors.joining("\n"));
                summaries.add(blockSummary);
            }
            if (summaries.isEmpty()) {
                log.debug("No forum messages this week, skip digest generation.");
                return;
            }
            String messageSummary = String.join("\n\n", summaries);

            // 用 AI 生成摘要
            String prompt = """
                    以下是本周学习论坛中的聊天记录：
                    ---
                    %s
                    ---
                    请整理成一份温暖、简洁的周报，要求：
                    - 标题为“本周讨论热点”
                    - 提炼 1~3 个核心话题（例如：“如何提高英语听力？”、“二次函数图像怎么画？”）
                    - 对每个话题，简要说明学生关注点或老师建议（如有）
                    - 最后加一句鼓励语，如“你们的每一次提问，都是进步的开始！”
                    - 全文不超过 200 字，记住一定不要使用markdown！
                    """.formatted(messageSummary);

            log.debug("AI request to generate weekly digest - Prompt length {}", prompt.length());
            String digest = analyze(prompt);
            // 插入通知表（全站通知）
            notificationService.createGlobalNotification("本周讨论热点", digest);
            log.debug("Scheduled task - Success to generate weekly digest");
        } catch (Exception e) {
            log.error("Fail to generate weekly digest, {}", e.getMessage());
        }
    }

    /**
     * 为教师生成课程排期优化建议
     */
    public String generateSchedulingAdvice(String teacherUserId) {
        Long teacherId = teacherMapper.findTeacherIdByUserId(teacherUserId)
                .orElseThrow(() -> new BusinessException("Teacher not found"));
        try {
            // 获取教师的有效预约记录
            List<ReservationInfoResult> reservations = reservationService.listTeacherReservations(teacherUserId, null).stream()
                    .filter(r -> r.getStartTime() != null && r.getState() != null)
                    .limit(30)
                    .toList();
            if (reservations.isEmpty()) {
                return "暂无足够数据";
            }
            // === 将每条记录转为自然语言描述 ===
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy年MM月dd日(E) HH:mm");
            StringBuilder recordsText = new StringBuilder();
            for (ReservationInfoResult r : reservations) {
                String timeStr = r.getStartTime().format(dtf);
                String subject = r.getSubjectName() != null ? r.getSubjectName() : "未指定科目";
                String status = switch (r.getState()) {
                    case COMPLETED -> "已上课";
                    case CONFIRMED -> "已确认";
                    case CANCELLED -> "已取消";
                    case PENDING -> "待确认";
                };
                recordsText.append("- ").append(timeStr)
                        .append("，")
                        .append(subject)
                        .append("课，")
                        .append(status)
                        .append("\n");
            }

            String prompt = """
                    你是一位教务分析师，请根据以下教师近期的学生预约记录，分析学生的预约行为：
                    ---
                    %s
                    ---
                    要求：
                    1. 找出最热门的预约时段（如“周六上午”、“工作日晚上”），计算其占比；
                    2. 如果数据不足或分布均匀，直接说“暂无明显高峰”。
                    3. 为教师建议预约课排期，不超过 200 字，记住一定不要使用markdown！
                    """.formatted(recordsText.toString());

            log.debug("AI request to generate scheduling advice - Prompt length {}", prompt.length());
            return analyze(prompt);
        } catch (Exception e) {
            log.error("Failed to generate scheduling advice for teacher {}: {}", teacherUserId, e.getMessage());
            return "生成排期建议时出错，请稍后重试。";
        }
    }

    /**
     * 为教师生成教学反馈周报摘要（基于评价内容）
     */
    public String generateTeachingFeedbackSummary(String teacherUserId) {
        Long teacherId = teacherMapper.findTeacherIdByUserId(teacherUserId)
                .orElseThrow(() -> new BusinessException("Teacher not found"));
        try {
            // 获取最近 20 条评价
            List<TeacherReviewEntity> reviews = reviewMapper.findByTeacherId(teacherId)
                    .stream()
                    .limit(30)
                    .toList();
            if (reviews.isEmpty()) {
                return "暂无学生评价。";
            }

            // 构建自然语言评价列表
            StringBuilder feedbackText = new StringBuilder();
            for (TeacherReviewEntity r : reviews) {
                String ratingStr = (r.getRating() != null) ? "【" + r.getRating() + "星】" : "【未评分】";
                String title = r.getTitle() != null ? r.getTitle().trim() : "";
                String content = r.getContent() != null ? r.getContent().trim() : "";
                String line = ratingStr + " " + (!title.isEmpty() ? title + ": " : "") + content;
                feedbackText.append("- ").append(line).append("\n");
            }

            String prompt = """
                    你是一位教育质量分析师，请分析以下学生对教师的近期评价：
                    ---
                    %s
                    ---
                    要求：
                    1. 进行情感分析和关键词提取；
                    2. 输出格式如下：
                       “评价关键词：耐心（+12次）、讲解清晰（+8次）、语速快（-3次）”
                       “建议：适当放慢语速，尤其在讲难题时。”
                    3. 正面词用“+次数”，负面词用“-次数”；
                    4. 给出的建议需具体、可操作；
                    5. 总字数不超过200字，记住一定不要使用markdown！
                    """.formatted(feedbackText.toString());

            log.debug("AI request to generate teaching feedback summary - Prompt length {}", prompt.length());
            return analyze(prompt);
        } catch (Exception e) {
            log.error("Failed to generate teaching feedback summary for teacher {}: {}", teacherId, e.getMessage());
            return "生成教学反馈摘要时出错，请稍后重试。";
        }
    }
}
