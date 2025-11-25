CREATE DATABASE db_qingxin_tutor CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

USE db_qingxin_tutor;

-- 1. user 用户表
CREATE TABLE user
(
    id          CHAR(36) PRIMARY KEY  DEFAULT (UUID()),
    username    VARCHAR(50)  NOT NULL UNIQUE,
    nickname    VARCHAR(50),
    email       VARCHAR(100) NOT NULL UNIQUE,
    icon        VARCHAR(255),          -- should starts with "avatar/"
    address     VARCHAR(255),
    passwd_hash VARCHAR(255) NOT NULL, -- hashed
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 2. email_verification_code 邮箱验证码表
CREATE TABLE email_verification_code
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    email         VARCHAR(100) NOT NULL UNIQUE,
    code          CHAR(6)      NOT NULL,
    attempt_count INT          NOT NULL default 0, -- limit 5
    expire_time   DATETIME     NOT NULL,
    create_time   DATETIME     NOT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 3. teacher 教师信息表
CREATE TABLE teacher
(
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    phone               VARCHAR(20)             NOT NULL UNIQUE,
    nickname            VARCHAR(50),
    name                VARCHAR(50)             NOT NULL,
    gender              ENUM ('MALE', 'FEMALE') NOT NULL,
    birth_date          DATE                    NOT NULL,
    icon                VARCHAR(255),
    address             VARCHAR(255),
    teaching_experience TEXT,
    description         TEXT,
    grade               TINYINT UNSIGNED        NOT NULL CHECK (grade BETWEEN 1 AND 9),
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 4. subject 科目表
CREATE TABLE subject
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    subject_name VARCHAR(50) NOT NULL UNIQUE,
    description  VARCHAR(255)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 5. teacher_subject 教师-科目关联表
CREATE TABLE teacher_subject
(
    teacher_id  BIGINT NOT NULL,
    subject_id  BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (teacher_id, subject_id),
    INDEX idx_subject_id (subject_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 6. user_wallet 钱包信息表
CREATE TABLE user_wallet
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     CHAR(36)       NOT NULL UNIQUE,
    balance     DECIMAL(12, 2) NOT NULL DEFAULT 0.00, -- 余额
    points      INT            NOT NULL DEFAULT 0,    -- 积分
    create_time DATETIME                DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 7. user_order 用户订单表
CREATE TABLE user_order
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     CHAR(36)                              NOT NULL,
    item        VARCHAR(100)                          NOT NULL,
    quantity    INT                                   NOT NULL DEFAULT 1,
    price       DECIMAL(10, 2)                        NOT NULL DEFAULT 0.00,
    state       ENUM ('PENDING', 'PAID', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    create_time DATETIME                              NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_create (user_id, create_time),
    INDEX idx_user_id (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 8. user_voucher 奖学券表
CREATE TABLE user_voucher
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     CHAR(36)       NOT NULL,
    amount      DECIMAL(10, 2) NOT NULL, -- 面额
    create_time DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expire_time DATETIME       NOT NULL,
    UNIQUE KEY uk_user_money_create (user_id, amount, create_time),
    INDEX idx_user_id (user_id),
    INDEX idx_expire_time (expire_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 9. user_study_plan 用户学习计划表
CREATE TABLE user_study_plan
(
    id                     BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id                CHAR(36)     NOT NULL,
    subject_id             BIGINT       NOT NULL,
    title                  VARCHAR(100) NOT NULL,
    content                TEXT,
    target_completion_time DATETIME,
    reminder_time          DATETIME,
    create_time            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_title (user_id, title),
    INDEX idx_user_id (user_id),
    INDEX idx_subject_id (subject_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 10. reservation 预约表
CREATE TABLE reservation
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     CHAR(36)                                                NOT NULL,
    teacher_id  BIGINT                                                  NOT NULL,
    subject_id  BIGINT                                                  NOT NULL,
    start_time  DATETIME                                                NOT NULL,
    duration    INT                                                     NOT NULL COMMENT 'Unit-Minute', -- minute
    state       ENUM ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    create_time DATETIME                                                NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_teacher_subject_start (user_id, teacher_id, subject_id, start_time),
    INDEX idx_user_id (user_id),
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_start_time (start_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 11. teacher_review 教师评价表
CREATE TABLE teacher_review
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     CHAR(36)     NOT NULL,
    teacher_id  BIGINT       NOT NULL,
    rating      TINYINT      NOT NULL CHECK (rating BETWEEN 1 AND 5),
    title       VARCHAR(100) NOT NULL,
    content     TEXT,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_teacher (user_id, teacher_id),
    INDEX idx_user_id (user_id),
    INDEX idx_teacher_id (teacher_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 12. app_feedback 应用反馈表
CREATE TABLE app_feedback
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     CHAR(36)     NOT NULL,
    title       VARCHAR(100) NOT NULL,
    content     TEXT         NOT NULL,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_create (user_id, create_time),
    INDEX idx_user_id (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 13. forum 论坛板块表
CREATE TABLE forum
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 14. forum_message 论坛消息表
CREATE TABLE forum_message
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    forum_id    BIGINT   NOT NULL,
    user_id     CHAR(36),
    teacher_id  BIGINT,
    content     TEXT     NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_forum_id (forum_id),
    INDEX idx_create_time (create_time),
    CONSTRAINT chk_user_or_teacher
        CHECK (
            (user_id IS NOT NULL AND teacher_id IS NULL)
                OR
            (user_id IS NULL AND teacher_id IS NOT NULL)
            )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 15. notification 系统通知表
CREATE TABLE notification
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     CHAR(36)              DEFAULT NULL, -- NULL 表示全站通知，非 NULL 表示定向通知
    title       VARCHAR(100) NOT NULL,
    content     TEXT         NOT NULL,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_title_create (title, create_time),
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;



-- DANGEROUS ZONE --
-- 清空所有表
TRUNCATE TABLE user;
TRUNCATE TABLE email_verification_code;
TRUNCATE TABLE teacher;
TRUNCATE TABLE subject;
TRUNCATE TABLE teacher_subject;
TRUNCATE TABLE user_wallet;
TRUNCATE TABLE user_order;
TRUNCATE TABLE user_voucher;
TRUNCATE TABLE user_study_plan;
TRUNCATE TABLE reservation;
TRUNCATE TABLE teacher_review;
TRUNCATE TABLE app_feedback;
TRUNCATE TABLE forum;
TRUNCATE TABLE forum_message;
TRUNCATE TABLE notification;
-- DANGEROUS ZONE --


-- 测试样例
-- 1. 插入科目
INSERT INTO subject (subject_name, description)
VALUES ('数学', '中小学数学课程'),
       ('英语', 'K12英语教学'),
       ('物理', '初中高中物理'),
       ('化学', '中学化学基础与进阶'),
       ('语文', '中小学语文阅读与写作');

-- 2. 插入教师（注意 icon 格式）
INSERT INTO teacher (phone, nickname, name, gender, birth_date, icon, address, teaching_experience, description, grade)
VALUES ('13800138001', '张老师', '张伟', 'MALE', '1985-03-12', '/avatar/13800138001.png', '北京市海淀区',
        '10年高中数学教学经验，擅长高考压轴题讲解。', '耐心细致，注重思维训练。', 9),
       ('13800138002', '李老师', '李娜', 'FEMALE', '1990-07-25', '/avatar/13800138002.png', '上海市浦东新区',
        '8年初中英语教学，雅思7.5分。', '课堂生动有趣，提升学生兴趣。', 7),
       ('13800138003', '王老师', '王强', 'MALE', '1988-11-30', '/avatar/13800138003.png', '广州市天河区',
        '6年物理竞赛辅导经验。', '逻辑清晰，擅长实验教学。', 8),
       ('13800138004', '陈老师', '陈芳', 'FEMALE', '1992-01-15', '/avatar/13800138004.png', '深圳市南山区',
        '5年化学教学，熟悉新课标。', '善于联系生活实际讲解抽象概念。', 6);

-- 3. 获取教师ID并关联科目（假设自增ID从1开始）
INSERT INTO teacher_subject (teacher_id, subject_id)
VALUES (1, 1), -- 张老师 - 数学
       (1, 3), -- 张老师 - 物理
       (2, 2), -- 李老师 - 英语
       (2, 5), -- 李老师 - 语文
       (3, 3), -- 王老师 - 物理
       (3, 4), -- 王老师 - 化学
       (4, 4), -- 陈老师 - 化学
       (4, 1) -- 陈老师 - 数学
;
-- 4. 插入论坛板块
INSERT INTO forum (name, description)
VALUES ('学习交流', '讨论学习方法、解题技巧'),
       ('教师专区', '教师发布通知、资料分享'),
       ('意见反馈交流', '对平台功能提出建议并交流');

-- 5. 插入论坛消息（仅教师发布，user_id = NULL, teacher_id = 对应ID）
INSERT INTO forum_message (forum_id, user_id, teacher_id, content)
VALUES (1, NULL, 1, '大家好！我是张老师，欢迎在本板块提问数学难题，我会定期解答。'),
       (1, NULL, 2, '英语学习重在坚持！推荐每天背10个单词+听一段听力。'),
       (2, NULL, 3, '本周物理实验课资料已上传，请同学们提前预习。'),
       (2, NULL, 4, '化学方程式配平技巧：先看氧，再看氢，最后调整金属元素。'),
       (3, NULL, 1, '建议增加错题本功能，方便学生复习。');

-- 6. 插入全站通知（user_id = NULL）
INSERT INTO notification (user_id, title, content)
VALUES (NULL, '🎉 欢迎使用智慧教育平台！', '感谢您加入我们的在线学习社区！平台将持续优化，助力每一位学子成长。'),
       (NULL, '📢 教师招募公告', '我们正在招募优秀中小学教师，欢迎有志之士加入！详情请联系客服。'),
       (NULL, '✨ 新功能上线：学习计划提醒', '现在您可以创建学习计划并设置提醒，系统将准时通知您！'),
       (NULL, '🔒 账号安全提示', '请勿向他人泄露验证码，平台工作人员不会索要您的密码或验证码。');
