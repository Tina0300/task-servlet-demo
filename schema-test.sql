-- ============================================================
-- Test environment schema: database task_db_test
-- Run inside mysql client: source D:/code_java/task-servlet-demo/task-servlet-demo/schema-test.sql
-- ============================================================
CREATE DATABASE IF NOT EXISTS `task_db_test`
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `task_db_test`;
SET NAMES utf8mb4;

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
VALUES ('admin', '123456', 'admin')
ON DUPLICATE KEY UPDATE `real_name` = VALUES(`real_name`);
