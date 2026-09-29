package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.User;

import java.util.Optional;

/**
 * JPA Repository for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their email address.
     *
     * @param email the email to search by
     * @return Optional containing the found user, or empty if not found
     */
    Optional<User> findByEmail(String email);
}
