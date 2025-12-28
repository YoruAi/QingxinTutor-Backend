package com.yoru.qingxintutor.enums.convert;

import com.yoru.qingxintutor.pojo.entity.ReservationEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ReservationStateConvert implements Converter<String, ReservationEntity.State> {
    @SuppressWarnings({"NullableProblems", "ConstantValue"})
    @Override
    public ReservationEntity.State convert(String source) {
        if (source == null || source.isBlank()) {
            return null;
        }
        try {
            return ReservationEntity.State.valueOf(source.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid state value");
        }
    }
}