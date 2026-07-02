-- ============================================================
-- FPTU Forum Database Schema
-- Database: FptuForumDB
-- SQL Server 2019+
-- ============================================================

USE master;
GO

IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'FptuForumDB')
BEGIN
    CREATE DATABASE FptuForumDB;
END
GO

USE FptuForumDB;
GO

-- ============================================================
-- Table: users
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='users' AND xtype='U')
BEGIN
    CREATE TABLE users (
        id          BIGINT IDENTITY(1,1) PRIMARY KEY,
        username    NVARCHAR(50)  NOT NULL UNIQUE,
        email       NVARCHAR(100) NOT NULL UNIQUE,
        password    NVARCHAR(255) NOT NULL,
        full_name   NVARCHAR(100),
        avatar_url  NVARCHAR(500),
        role        NVARCHAR(20)  NOT NULL DEFAULT 'MEMBER'
                        CHECK (role IN ('MEMBER', 'MODERATOR', 'ADMIN')),
        status      NVARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                        CHECK (status IN ('ACTIVE', 'BANNED')),
        banned_reason NVARCHAR(255) NULL,
        banned_by   BIGINT        NULL,
        banned_at   DATETIME2     NULL,
        created_at  DATETIME2     NOT NULL DEFAULT GETDATE(),
        updated_at  DATETIME2     NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_users_banned_by FOREIGN KEY (banned_by) REFERENCES users(id)
    );
END
GO

-- ============================================================
-- Table: topics
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='topics' AND xtype='U')
BEGIN
    CREATE TABLE topics (
        id          BIGINT IDENTITY(1,1) PRIMARY KEY,
        name        NVARCHAR(100) NOT NULL UNIQUE,
        description NVARCHAR(500),
        created_at  DATETIME2     NOT NULL DEFAULT GETDATE()
    );
END
GO

-- ============================================================
-- Table: posts
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='posts' AND xtype='U')
BEGIN
    CREATE TABLE posts (
        id          BIGINT IDENTITY(1,1) PRIMARY KEY,
        title       NVARCHAR(255) NOT NULL,
        content     NVARCHAR(MAX) NOT NULL,
        status      NVARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                        CHECK (status IN ('ACTIVE', 'HIDDEN', 'DELETED')),
        is_pinned   BIT           NOT NULL DEFAULT 0,
        view_count  INT           NOT NULL DEFAULT 0,
        image_url   NVARCHAR(255) NULL,
        moderation_reason NVARCHAR(255) NULL,
        moderated_by BIGINT       NULL,
        user_id     BIGINT        NOT NULL,
        topic_id    BIGINT        NOT NULL,
        created_at  DATETIME2     NOT NULL DEFAULT GETDATE(),
        updated_at  DATETIME2     NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_posts_user  FOREIGN KEY (user_id)  REFERENCES users(id),
        CONSTRAINT FK_posts_topic FOREIGN KEY (topic_id) REFERENCES topics(id),
        CONSTRAINT FK_posts_moderator FOREIGN KEY (moderated_by) REFERENCES users(id)
    );
END
GO

-- ============================================================
-- Table: comments
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='comments' AND xtype='U')
BEGIN
    CREATE TABLE comments (
        id                BIGINT IDENTITY(1,1) PRIMARY KEY,
        content           NVARCHAR(MAX) NOT NULL,
        status            NVARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                              CHECK (status IN ('ACTIVE', 'DELETED')),
        user_id           BIGINT        NOT NULL,
        post_id           BIGINT        NOT NULL,
        parent_comment_id BIGINT        NULL,
        moderated_by      BIGINT        NULL,
        created_at        DATETIME2     NOT NULL DEFAULT GETDATE(),
        updated_at        DATETIME2     NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_comments_user    FOREIGN KEY (user_id)           REFERENCES users(id),
        CONSTRAINT FK_comments_post    FOREIGN KEY (post_id)           REFERENCES posts(id),
        CONSTRAINT FK_comments_parent  FOREIGN KEY (parent_comment_id) REFERENCES comments(id),
        CONSTRAINT FK_comments_moderator FOREIGN KEY (moderated_by)    REFERENCES users(id)
    );
END
GO

-- ============================================================
-- Table: likes
-- Constraint: chi duoc troi toi post_id HOAC comment_id, khong duoc ca hai
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='likes' AND xtype='U')
BEGIN
    CREATE TABLE likes (
        id          BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id     BIGINT    NOT NULL,
        post_id     BIGINT    NULL,
        comment_id  BIGINT    NULL,
        created_at  DATETIME2 NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_likes_user    FOREIGN KEY (user_id)    REFERENCES users(id),
        CONSTRAINT FK_likes_post    FOREIGN KEY (post_id)    REFERENCES posts(id),
        CONSTRAINT FK_likes_comment FOREIGN KEY (comment_id) REFERENCES comments(id),
        -- Khong duoc ca hai NULL hoac ca hai NOT NULL
        CONSTRAINT CHK_likes_target CHECK (
            (post_id IS NOT NULL AND comment_id IS NULL) OR
            (post_id IS NULL    AND comment_id IS NOT NULL)
        ),
        -- Moi user chi like moi post/comment mot lan
        CONSTRAINT UQ_likes_user_post    UNIQUE (user_id, post_id),
        CONSTRAINT UQ_likes_user_comment UNIQUE (user_id, comment_id)
    );
END
GO

-- ============================================================
-- Table: saved_posts
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='saved_posts' AND xtype='U')
BEGIN
    CREATE TABLE saved_posts (
        id         BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id    BIGINT    NOT NULL,
        post_id    BIGINT    NOT NULL,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_saved_posts_user FOREIGN KEY (user_id) REFERENCES users(id),
        CONSTRAINT FK_saved_posts_post FOREIGN KEY (post_id) REFERENCES posts(id),
        CONSTRAINT UQ_saved_posts      UNIQUE (user_id, post_id)
    );
END
GO

-- ============================================================
-- Table: reports
-- Constraint: chi duoc troi toi post_id HOAC comment_id
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='reports' AND xtype='U')
BEGIN
    CREATE TABLE reports (
        id           BIGINT IDENTITY(1,1) PRIMARY KEY,
        reason       NVARCHAR(500) NOT NULL,
        status       NVARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                         CHECK (status IN ('PENDING', 'RESOLVED', 'REJECTED')),
        reporter_id  BIGINT        NOT NULL,
        post_id      BIGINT        NULL,
        comment_id   BIGINT        NULL,
        reviewed_by  BIGINT        NULL,
        reviewed_at  DATETIME2     NULL,
        created_at   DATETIME2     NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_reports_reporter   FOREIGN KEY (reporter_id) REFERENCES users(id),
        CONSTRAINT FK_reports_post       FOREIGN KEY (post_id)     REFERENCES posts(id),
        CONSTRAINT FK_reports_comment    FOREIGN KEY (comment_id)  REFERENCES comments(id),
        CONSTRAINT FK_reports_reviewer   FOREIGN KEY (reviewed_by) REFERENCES users(id),
        CONSTRAINT CHK_reports_target    CHECK (
            (post_id IS NOT NULL AND comment_id IS NULL) OR
            (post_id IS NULL    AND comment_id IS NOT NULL)
        )
    );
END
GO

-- ============================================================
-- Table: user_restrictions
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='user_restrictions' AND xtype='U')
BEGIN
    CREATE TABLE user_restrictions (
        id               BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id          BIGINT        NOT NULL,
        restriction_type NVARCHAR(20)  NOT NULL
                             CHECK (restriction_type IN ('MUTE_POST', 'MUTE_COMMENT')),
        reason           NVARCHAR(500),
        is_active        BIT           NOT NULL DEFAULT 1,
        start_at         DATETIME2     NOT NULL DEFAULT GETDATE(),
        end_at           DATETIME2     NULL,
        restricted_by    BIGINT        NOT NULL,
        CONSTRAINT FK_restrictions_user    FOREIGN KEY (user_id)    REFERENCES users(id),
        CONSTRAINT FK_restrictions_creator FOREIGN KEY (restricted_by) REFERENCES users(id)
    );
END
GO

-- ============================================================
-- Table: audit_logs
-- ============================================================
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='audit_logs' AND xtype='U')
BEGIN
    CREATE TABLE audit_logs (
        id              BIGINT IDENTITY(1,1) PRIMARY KEY,
        actor_id        BIGINT        NOT NULL,
        action          NVARCHAR(100) NOT NULL,
        target_user_id  BIGINT        NULL,
        target_post_id  BIGINT        NULL,
        target_comment_id BIGINT      NULL,
        note            NVARCHAR(MAX),
        created_at      DATETIME2     NOT NULL DEFAULT GETDATE(),
        CONSTRAINT FK_audit_actor          FOREIGN KEY (actor_id)          REFERENCES users(id),
        CONSTRAINT FK_audit_target_user    FOREIGN KEY (target_user_id)    REFERENCES users(id),
        CONSTRAINT FK_audit_target_post    FOREIGN KEY (target_post_id)    REFERENCES posts(id),
        CONSTRAINT FK_audit_target_comment FOREIGN KEY (target_comment_id) REFERENCES comments(id)
    );
END
GO

-- ============================================================
-- Seed data mau (tuy chon)
-- ============================================================
-- Tao admin default (password: Admin@123 encoded BCrypt)
IF NOT EXISTS (SELECT * FROM users WHERE username = 'admin')
BEGIN
    INSERT INTO users (username, email, password, full_name, role, status)
    VALUES (
        'admin',
        'admin@fpt.edu.vn',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH',
        'System Admin',
        'ADMIN',
        'ACTIVE'
    );
END

-- Tao mot so topic mau
IF NOT EXISTS (SELECT * FROM topics WHERE name = 'General')
BEGIN
    INSERT INTO topics (name, description) VALUES
    ('General',    N'Thảo luận chung cho mọi chủ đề'),
    ('Tech',       N'Công nghệ, lập trình, phần mềm'),
    ('Campus Life',N'Đời sống sinh viên FPTU'),
    ('Q&A',        N'Hỏi đáp học thuật và cuộc sống');
END
GO
