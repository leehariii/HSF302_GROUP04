package com.fptu.forum.service;

import com.fptu.forum.dto.AdminDashboardDTO;

/**
 * Service lay so lieu thong ke cho Admin Dashboard.
 */
public interface AdminDashboardService {

    /**
     * Lay day du 7 so lieu thong ke, dung repository count truc tiep, khong load du lieu.
     */
    AdminDashboardDTO getStats();
}
