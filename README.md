# QingxinTutor-Backend

Name: QingxinTutor

Author: yoru

Description:
倾心家教（QingxinTutor）是一款面向学生家长的在线家教匹配平台。家长可按科目、年级等维度筛选教师，查看教师履历、过往评价及“优秀教师”推荐；
系统支持预约课时、学习计划制定、奖学券激励及消息通知等核心功能，打造闭环式家教服务体验。

BasePackageName: com.yoru.qingxintutor

TechStack:

- backend: Java SpringBoot, MyBatis, MySQL

**See frontend**: https://github.com/YoruAi/QingxinTutor-Frontend

**See api docs**: https://documenter.getpostman.com/view/38185893/2sB3dHXDkL

See DB Design in database.sql.

All entities are in com.yoru.qingxintutor.pojo.

Completed function modules: Auth, Teacher/Student Role, Subjects, Teachers, Reservation & Orders, User, Wallet,
Messages, Plans, AI insight.

## 部署指南

> 有基础的开发者可以选择修改配置并使用其他方案。

1. 申请并配置163邮箱SMTP，填写邮箱账号与SMTP授权码；

   *`spring.mail.username`

   *`spring.mail.password`

2. 创建数据库(MySQL)并填入数据库访问相关配置；

   `spring.datasource.url`

   *`spring.datasource.username`

   *`spring.datasource.password`

3. 申请 AI API，访问火山引擎 Ark 平台，开通模型并填入配置；

   *`app.ai.api-key`

   `app.ai.base-url`

   `app.ai.model`

4. 设置JWT密钥。

   *`app.jwt.secret`

5. 设置文件上传本地目录`UPLOAD_BASE_DIR`环境变量如`C:/Temp`。

6. 创建GitHub OAuth应用，并填入配置；

   `app.oauth.github.client-id`

   *`app.oauth.github.client-secret`

   `app.oauth.github.redirect-url`

7. 使用Jasypt加密所有敏感值（例如以上加*号的配置）并设置`JASYPT_KEY`密钥环境变量与加密时使用的加密参数；

   `jasypt.encryptor.**`

If you want to revise or add some functions, be **careful** to use the unused mapper method because they are all
unverified, there may be errors.

---

Completed. New features will be added occasionally.