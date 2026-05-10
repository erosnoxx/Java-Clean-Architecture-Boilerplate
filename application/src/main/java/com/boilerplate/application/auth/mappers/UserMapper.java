package com.boilerplate.application.auth.mappers;

import com.boilerplate.application.auth.schemas.response.UserResponse;
import com.boilerplate.application.common.mappers.BaseMapper;
import com.boilerplate.domain.auth.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UserMapper extends BaseMapper {
    @Mapping(target = "name", expression = "java(fromStringVO(user.getName()))")
    @Mapping(target = "email", expression = "java(fromStringVO(user.getEmail()))")
    UserResponse toResponse(User user);
}
