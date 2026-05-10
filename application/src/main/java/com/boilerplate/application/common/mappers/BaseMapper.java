package com.boilerplate.application.common.mappers;


import com.boilerplate.domain.common.vos.ValueObject;

public interface BaseMapper {

    default String fromStringVO(ValueObject<String> vo) {
        return vo == null ? null : vo.getValue();
    }

    default Integer fromIntegerVO(ValueObject<Integer> vo) {
        return vo == null ? null : vo.getValue();
    }
}
