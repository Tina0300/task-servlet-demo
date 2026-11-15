package cn.edu.mju.band.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Task 实体状态判断测试")
class TaskTest {

    @Test
    @DisplayName("status=1 时 isFinished 返回 true")
    void isFinished_shouldReturnTrue_whenFinished() {
        Task task = new Task();
        task.setStatus(1);

        assertTrue(task.isFinished());
    }

    @Test
    @DisplayName("status=0 时 isFinished 返回 false")
    void isFinished_shouldReturnFalse_whenUnfinished() {
        Task task = new Task();
        task.setStatus(0);

        assertFalse(task.isFinished());
    }

    @Test
    @DisplayName("status=null 时 isFinished 返回 false（边界）")
    void isFinished_shouldReturnFalse_whenNull() {
        Task task = new Task();
        task.setStatus(null);

        assertFalse(task.isFinished());
    }
}
