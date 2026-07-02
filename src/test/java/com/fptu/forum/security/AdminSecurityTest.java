package com.fptu.forum.security;

import com.fptu.forum.config.RoleHierarchyConfig;
import com.fptu.forum.config.SecurityConfig;
import com.fptu.forum.controller.AdminDashboardController;
import com.fptu.forum.dto.AdminDashboardDTO;
import com.fptu.forum.service.AdminDashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security test kiem tra phan quyen URL cho Admin module.
 * Su dung @WebMvcTest(AdminDashboardController) chi load controller can test,
 * tranh phai mock tat ca services cua toan bo app.
 *
 * Test 19, 20, 21.
 */
@WebMvcTest(controllers = AdminDashboardController.class)
@Import({SecurityConfig.class, RoleHierarchyConfig.class})
class AdminSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private AdminDashboardService adminDashboardService;

    @BeforeEach
    void setUp() {
        // Tao mock return value de Thymeleaf co the render template
        when(adminDashboardService.getStats()).thenReturn(new AdminDashboardDTO());
    }

    @Test
    @DisplayName("19. ADMIN truy cap /admin/dashboard - duoc phep (KHONG phai 403/401)")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void admin_canAccessDashboard() throws Exception {
        int status = mockMvc.perform(get("/admin/dashboard"))
                .andReturn().getResponse().getStatus();
        // ADMIN phai duoc Spring Security cho phep (200 hoac 500 do service mock null, khong phai 403/401)
        assertThat(status).as("ADMIN bi denied voi status " + status)
                .isNotIn(403, 401);
    }

    @Test
    @DisplayName("20. MEMBER bi 403 khi truy cap /admin/**")
    @WithMockUser(username = "member1", roles = {"MEMBER"})
    void member_cannotAccessAdmin() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("21. MODERATOR bi 403 khi truy cap /admin/**")
    @WithMockUser(username = "mod1", roles = {"MODERATOR"})
    void moderator_cannotAccessAdmin() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden());
    }
}
