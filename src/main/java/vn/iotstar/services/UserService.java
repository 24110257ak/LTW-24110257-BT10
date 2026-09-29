package vn.iotstar.services;

import org.springframework.stereotype.Service;
import vn.iotstar.entity.User;
import vn.iotstar.repository.UserRepository;

import java.util.List;

/**
 * UserService - Business logic for user management
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Retrieve all registered users from the database.
     *
     * @return list of all User entities
     */
    public List<User> allUsers() {
        return userRepository.findAll();
    }
}
