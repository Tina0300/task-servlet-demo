CREATE DATABASE IF NOT EXISTS taskmaster;
USE taskmaster;

CREATE TABLE IF NOT EXISTS t_task (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    priority VARCHAR(20) NOT NULL DEFAULT '1',
    due_date DATE NULL,
    create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO t_task (title, status, priority, due_date) VALUES
('学习 Spring MVC', 1, '2', '2026-10-27'),
('完成任务管理系统', 0, '3', '2026-10-30'),
('整理代码结构', 1, '1', '2026-10-26');

