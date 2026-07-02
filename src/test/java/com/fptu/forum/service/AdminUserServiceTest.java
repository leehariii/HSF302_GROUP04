package com.fptu.forum.service;

import com.fptu.forum.entity.AuditLog;
import com.fptu.forum.entity.User;
import com.fptu.forum.enums.AuditAction;
import com.fptu.forum.enums.Role;
import com.fptu.forum.enums.UserStatus;
import com.fptu.forum.exception.BusinessException;
import com.fptu.forum.exception.ResourceNotFoundException;
import com.fptu.forum.repository.AuditLogRepository;
import com.fptu.forum.repository.UserRepository;
import com.fptu.forum.service.impl.AdminUserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test cho AdminUserService.
 * Test 11 ca ban quy dinh: ban, unban, promote, demote va audit log.
 */
@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    private User admin;
    private User memberUser;
    private User moderatorUser;
    private User bannedUser;

    @BeforeEach
    void setUp() {
        admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setRole(Role.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);

        memberUser = new User();
        memberUser.setId(2L);
        memberUser.setUsername("member1");
        memberUser.setRole(Role.MEMBER);
        memberUser.setStatus(UserStatus.ACTIVE);

        moderatorUser = new User();
        moderatorUser.setId(3L);
        moderatorUser.setUsername("mod1");
        moderatorUser.setRole(Role.MODERATOR);
        moderatorUser.setStatus(UserStatus.ACTIVE);

        bannedUser = new User();
        bannedUser.setId(4L);
        bannedUser.setUsername("banned1");
        bannedUser.setRole(Role.MEMBER);
        bannedUser.setStatus(UserStatus.BANNED);
    }

    // ==================== BAN USER ====================

    @Test
    @DisplayName("1. Ban MEMBER thanh cong")
    void banUser_success() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(2L)).thenReturn(Optional.of(memberUser));

        adminUserService.banUser(2L, "Vi pham noi quy", "admin");

        assertThat(memberUser.getStatus()).isEqualTo(UserStatus.BANNED);
        verify(userRepository).save(memberUser);
    }

    @Test
    @DisplayName("2. Ban luu dung reason, bannedBy, bannedAt")
    void banUser_savedFieldsCorrectly() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(2L)).thenReturn(Optional.of(memberUser));

        adminUserService.banUser(2L, "  Spam bai viet  ", "admin");

        assertThat(memberUser.getBannedReason()).isEqualTo("Spam bai viet");
        assertThat(memberUser.getBannedBy()).isEqualTo(admin);
        assertThat(memberUser.getBannedAt()).isNotNull();
    }

    @Test
    @DisplayName("3. Khong duoc tu ban chinh minh")
    void banUser_cannotBanSelf() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> adminUserService.banUser(1L, "test", "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("chính mình");
    }

    @Test
    @DisplayName("4. Khong duoc ban ADMIN khac")
    void banUser_cannotBanAdmin() {
        User anotherAdmin = new User();
        anotherAdmin.setId(5L);
        anotherAdmin.setUsername("admin2");
        anotherAdmin.setRole(Role.ADMIN);
        anotherAdmin.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(5L)).thenReturn(Optional.of(anotherAdmin));

        assertThatThrownBy(() -> adminUserService.banUser(5L, "test", "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Admin");
    }

    @Test
    @DisplayName("5. Khong ban lai user dang BANNED")
    void banUser_alreadyBanned() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(4L)).thenReturn(Optional.of(bannedUser));

        assertThatThrownBy(() -> adminUserService.banUser(4L, "test", "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("đã bị khóa");
    }

    // ==================== UNBAN USER ====================

    @Test
    @DisplayName("6. Unban user thanh cong")
    void unbanUser_success() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(4L)).thenReturn(Optional.of(bannedUser));

        adminUserService.unbanUser(4L, "admin");

        assertThat(bannedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(userRepository).save(bannedUser);
    }

    @Test
    @DisplayName("7. Unban xoa bannedReason, bannedBy, bannedAt")
    void unbanUser_clearsBanFields() {
        bannedUser.setBannedReason("Vi pham");
        bannedUser.setBannedBy(admin);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(4L)).thenReturn(Optional.of(bannedUser));

        adminUserService.unbanUser(4L, "admin");

        assertThat(bannedUser.getBannedReason()).isNull();
        assertThat(bannedUser.getBannedBy()).isNull();
        assertThat(bannedUser.getBannedAt()).isNull();
    }

    // ==================== PROMOTE ====================

    @Test
    @DisplayName("8. Promote MEMBER ACTIVE len MODERATOR thanh cong")
    void promote_memberActiveToModerator() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(2L)).thenReturn(Optional.of(memberUser));

        adminUserService.promoteToModerator(2L, "admin");

        assertThat(memberUser.getRole()).isEqualTo(Role.MODERATOR);
        verify(userRepository).save(memberUser);
    }

    @Test
    @DisplayName("9. Khong promote user BANNED")
    void promote_cannotPromoteBanned() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(4L)).thenReturn(Optional.of(bannedUser));

        assertThatThrownBy(() -> adminUserService.promoteToModerator(4L, "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("khóa");
    }

    // ==================== DEMOTE ====================

    @Test
    @DisplayName("10. Demote MODERATOR xuong MEMBER thanh cong")
    void demote_moderatorToMember() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(3L)).thenReturn(Optional.of(moderatorUser));

        adminUserService.demoteToMember(3L, "admin");

        assertThat(moderatorUser.getRole()).isEqualTo(Role.MEMBER);
        verify(userRepository).save(moderatorUser);
    }

    // ==================== AUDIT LOG ====================

    @Test
    @DisplayName("11. Moi thao tac admin deu ghi audit log")
    void everyAction_writesAuditLog() {
        // Ban
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(2L)).thenReturn(Optional.of(memberUser));
        adminUserService.banUser(2L, "test reason", "admin");

        // Verify auditLogRepository.save duoc goi
        ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(logCaptor.capture());
        AuditLog saved = logCaptor.getValue();

        assertThat(saved.getAction()).isEqualTo(AuditAction.BAN_USER.name());
        assertThat(saved.getActor()).isEqualTo(admin);
        assertThat(saved.getTargetUser()).isEqualTo(memberUser);
        assertThat(saved.getNote()).contains("test reason");
    }
}
