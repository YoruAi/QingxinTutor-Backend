package com.yoru.qingxintutor.enums.convert;

import com.yoru.qingxintutor.enums.NotificationType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class NotificationTypeConvert implements Converter<String, NotificationType> {
    @SuppressWarnings({"NullableProblems"})
    @Override
    public NotificationType convert(String source) {
        return NotificationType.fromString(source);
    }
}