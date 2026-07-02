package com.fptu.forum.dto.request;

import com.fptu.forum.enums.RestrictionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO nhan du lieu tu form mute user (Moderator).
 */
@Getter
@Setter
public class MuteRequest {

    @NotNull(message = "Vui long chon loai han che")
    private RestrictionType restrictionType;

    @Size(max = 500, message = "Ly do toi da 500 ky tu")
    private String reason;

    // Null = mute vinh vien
    private LocalDateTime endAt;
}
