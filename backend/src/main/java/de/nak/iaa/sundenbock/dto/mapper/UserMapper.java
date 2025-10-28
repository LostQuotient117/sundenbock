package de.nak.iaa.sundenbock.dto.mapper;

import de.nak.iaa.sundenbock.dto.UserDTO;
import de.nak.iaa.sundenbock.model.user.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toUserDTO(User user);
    User toUser(UserDTO userDTO);
}
