package com.yoru.qingxintutor.annotation;

import java.lang.annotation.*;

// See com.yoru.qingxintutor.aop.AntiReplayAspect
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AntiReplay {
}
