package de.nak.iaa.sundenbock.service;

import de.nak.iaa.sundenbock.dto.mapper.UserMapper;
import de.nak.iaa.sundenbock.dto.UserDTO;
import de.nak.iaa.sundenbock.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }


    /**
     * Retrieves all users and converts them to safe DTOs.
     * @return A list of all users.
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserDTO)
                .collect(Collectors.toList());
    }
}
