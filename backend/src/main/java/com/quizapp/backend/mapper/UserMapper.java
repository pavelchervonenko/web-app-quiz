package com.quizapp.backend.mapper;

import com.quizapp.backend.dto.user.CurrentUserDTO;
import com.quizapp.backend.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    CurrentUserDTO toCurrentUserDTO(User user);
}
