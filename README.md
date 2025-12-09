# QingxinTutor-Backend

Name: QingxinTutor

Author: yoru

Description:
倾心家教（QingxinTutor）是一款面向学生家长的在线家教匹配平台。用户可按科目、年级等维度筛选教师，查看教师履历、过往评价及“优秀教师”推荐；
系统支持预约课时、学习计划制定、奖学券激励及消息通知等核心功能，打造闭环式家教服务体验。

BasePackageName: com.yoru.qingxintutor

TechStack:

- backend: Java SpringBoot, MyBatis, MySQL, Spring Security

**See frontend**: https://github.com/YoruAi/QingxinTutor-Frontend

**See api docs**: https://documenter.getpostman.com/view/38185893/2sB3dHXDkL

See DB Design in database.sql. All entities are in com.yoru.qingxintutor.pojo.

Completed function modules: Auth, Teacher/Student Role, Subjects, Teachers, Reservation & Orders, User, Wallet,
Messages, Plans, AI insight.

## 部署指南

请仔细检查`application.yml`文件中的配置，确保没有遗漏关键配置。

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

4. 设置 JWT 密钥。

   *`app.jwt.secret`

5. 设置文件上传本地目录`UPLOAD_BASE_DIR`环境变量如`C:/Temp`。

6. 创建 GitHub OAuth 应用，并填入配置；

   `app.oauth.github.client-id`

   *`app.oauth.github.client-secret`

   `app.oauth.github.redirect-url`

7. 使用Jasypt加密所有敏感值（例如以上加*号的配置）并设置`JASYPT_KEY`密钥环境变量与加密时使用的加密参数；

   `jasypt.encryptor.**`

If you want to revise or add some functions, be **careful** to use the unused mapper method because they are all
unverified, there may be errors.

该项目的安全性需要依靠于 HTTPS 协议，因此若有 SSL 证书的开发者请务必使用 Nginx 代理以支持 HTTPS 请求。

## 项目特点

1. 完好健壮的安全机制。包括 JWT 令牌管理、身份权限管理、参数校验、操作合法性校验、验证码发送检查、全局异常处理机制等等，避免因接口暴露而导致产生脏数据、扰乱系统正常运行状态。
2. 贴近实际的模拟支付机制。使用H5支付页+`/callback`模式，一旦申请到真实支付接口，即可迅速应用，无需修改前端代码。
3. 预约与订单管理功能。采用如下所示预约流程：

```text
用户向教师发起预约请求(reservation::pending)
--> 教师拒绝/用户取消预约(reservation::canceled) /end
| or
------> 教师看到后发起多个订单(order::pending)
    --> (optional)教师取消某一订单(order::canceled)
    --> (optional)订单超时15分钟被自动取消(order::canceled)
    --> (optional)预约已超过开始时间被自动取消(reservation::canceled) /end
    --> 用户看到订单后支付(order::paid)
    --> 用户取消预约(reservation::canceled)
    --> 退款流程(关联订单order::canceled, 若order::paid则退款) /end
    | or
    ------> 教师看到全部订单支付成功后(同时系统检查)确认预约请求(reservation::confirmed)
        // 之后不允许再发订单，不允许再取消预约与关联订单
        --> 系统发送10分钟上课提醒(仅reservation::confirmed)
        --> 完成课程后教师结课(reservation::completed)
        --> 系统发放奖学券 /end
```

4. 聊天与通知功能。通知分为全体与个人通知，全体通知可作为广播横幅出现于首页；消息分为师生私聊与论坛公开聊天，引入WebSocket协议，支持聊天窗口实时信息接收更新。
5. 教师评价功能。每位学生都可以对教师发起评价打分，教师页面将会展现所有评价与平均打分。
6. 学习计划功能。支持创建不同科目的学习计划，可设置提醒时间，系统将自动发出通知与邮件。
7. 邮件发送功能。在预约流程和学习计划提醒等功能中引入邮件提醒(需用户绑定邮箱)，避免遗漏重要通知信息。
8. 应用反馈功能。支持向应用发起反馈。
9. 钱包与奖学券功能。支持查看钱包余额，支持订单支付时使用奖学券抵扣金额。
10. AI分析功能。引入AI大语言模型，根据调出的数据分析用户，并给出建议。

---

Completed. New features will be added occasionally.

因作者经精力有限，还有很多可优化以及可拓展的地方，例如可引入Redis以进行验证码校验和防重放机制
(本项目目前只使用了timestamp限制防重放)、加入管理员身份等。希望我的代码具备良好的可拓展性、可重构性和可读性，能帮助到各位开发者们。

请勿照搬照抄，若有疑问请发表Discussions或提交Issue。欢迎各位开发者们批评指正。

[![wakatime](https://wakatime.com/badge/user/a043e842-cadf-4159-83cf-53e58cb1748b/project/59aa1813-b12c-4101-987e-40e356b30f33.svg)](https://wakatime.com/badge/user/a043e842-cadf-4159-83cf-53e58cb1748b/project/59aa1813-b12c-4101-987e-40e356b30f33)