CREATE DATABASE IF NOT EXISTS `task_db`
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `task_db`;

CREATE TABLE IF NOT EXISTS `sys_user` (
    `id`        BIGINT       NOT NULL AUTO_INCREMENT,
    `username`  VARCHAR(50)  NOT NULL,
    `password`  VARCHAR(100) NOT NULL,
    `real_name` VARCHAR(50)  DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `task` (
    `id`        BIGINT        NOT NULL AUTO_INCREMENT,
    `title`     VARCHAR(120)  NOT NULL,
    `content`   VARCHAR(1000) DEFAULT NULL,
    `priority`  TINYINT       DEFAULT 2,
    `status`    TINYINT       DEFAULT 0,
    `task_date` DATE          DEFAULT NULL,
    `user_id`   BIGINT        DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_task_user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

INSERT INTO `sys_user` (`username`, `password`, `real_name`)
VALUES ('admin', '123456', '管理员')
ON DUPLICATE KEY UPDATE `real_name` = VALUES(`real_name`);

-- ============================================================
-- 预置演示数据：首次初始化时写入 5 条任务，便于演示与截图验收
-- 注意：仅首次执行；重复执行 schema.sql 会重复插入这些任务
-- user_id 通过变量取 admin 的真实主键，避免依赖自增 ID 恰好为 1
-- ============================================================
SET @admin_id = (SELECT `id` FROM `sys_user` WHERE `username` = 'admin' LIMIT 1);

INSERT INTO `task` (`title`, `content`, `priority`, `status`, `task_date`, `user_id`) VALUES
('完成实验报告',   '环节二技术文档撰写，含 README 与 API 手册', 3, 0, '2026-11-20', @admin_id),
('准备答辩 PPT',   '整理 CI 流水线截图与演进复盘',            2, 0, '2026-11-22', @admin_id),
('复习 JUnit5',    'Mockito 打桩、断言与极端场景测试',         2, 1, '2026-11-10', @admin_id),
('整理实验截图',   '按截图清单逐项存档，用于报告佐证',          1, 1, '2026-11-15', @admin_id),
('优化分页查询',   'keyword 与 status 组合条件，验证分页插件',  3, 0, '2026-11-25', @admin_id);

