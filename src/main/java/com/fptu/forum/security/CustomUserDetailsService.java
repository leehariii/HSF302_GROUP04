package com.fptu.forum.security;

import com.fptu.forum.entity.User;
import com.fptu.forum.enums.UserStatus;
import com.fptu.forum.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Custom UserDetailsService: load user tu database va kiem tra trang thai BANNED.
 * Duoc Spring Security goi khi user dang nhap.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Tim user trong database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Khong tim thay tai khoan: " + username));

        // Neu user bi BANNED thi nem exception -> Spring Security se bao loi
        if (user.getStatus() == UserStatus.BANNED) {
            throw new UsernameNotFoundException(
                    "Tai khoan '" + username + "' da bi khoa (BANNED).");
        }

        // Gan quyen cho Spring Security theo pattern "ROLE_xxx"
        // Role hierarchy duoc cau hinh rieng trong RoleHierarchyConfig
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .build();
    }
}
