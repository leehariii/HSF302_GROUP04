package com.fptu.forum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;

/**
 * Cau hinh Role Hierarchy: ADMIN > MODERATOR > MEMBER.
 * Khi khai bao vay, ADMIN tu dong co quyen cua MODERATOR va MEMBER,
 * MODERATOR tu dong co quyen cua MEMBER.
 */
@Configuration
public class RoleHierarchyConfig {

    @Bean
    public RoleHierarchy roleHierarchy() {
        // Format: ROLE_CAP_TREN > ROLE_CAP_DUOI
        return RoleHierarchyImpl.fromHierarchy(
                "ROLE_ADMIN > ROLE_MODERATOR\n" +
                "ROLE_MODERATOR > ROLE_MEMBER"
        );
    }
}
