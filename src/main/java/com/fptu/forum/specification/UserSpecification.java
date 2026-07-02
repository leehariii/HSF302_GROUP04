package com.fptu.forum.specification;

import com.fptu.forum.entity.User;
import com.fptu.forum.enums.Role;
import com.fptu.forum.enums.UserStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Specification de build query dong cho UserRepository (Admin search/filter).
 */
public class UserSpecification {

    private UserSpecification() {}

    /**
     * Tao Specification filter user theo keyword (username, email, fullName),
     * role va status.
     */
    public static Specification<User> filter(String keyword, Role role, UserStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // keyword: tim trong username, email, fullName
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                Predicate byUsername = cb.like(cb.lower(root.get("username")), like);
                Predicate byEmail    = cb.like(cb.lower(root.get("email")), like);
                Predicate byFullName = cb.like(cb.lower(root.get("fullName")), like);
                predicates.add(cb.or(byUsername, byEmail, byFullName));
            }

            if (role != null) {
                predicates.add(cb.equal(root.get("role"), role));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
