package com.yoru.qingxintutor.config.convert;

import com.yoru.qingxintutor.pojo.entity.UserOrderEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class OrderStateConvert implements Converter<String, UserOrderEntity.State> {
    @SuppressWarnings({"NullableProblems", "ConstantValue"})
    @Override
    public UserOrderEntity.State convert(String source) {
        if (source == null || source.isBlank()) {
            return null;
        }
        try {
            return UserOrderEntity.State.valueOf(source.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid state value");
        }
    }
}
