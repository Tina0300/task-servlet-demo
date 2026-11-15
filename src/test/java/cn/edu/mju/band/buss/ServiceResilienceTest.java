package cn.edu.mju.band.buss;

import cn.edu.mju.band.buss.impl.TaskBussImpl;
import cn.edu.mju.band.buss.impl.UserBussImpl;
import cn.edu.mju.band.dal.TaskDao;
import cn.edu.mju.band.dal.UserDao;
import cn.edu.mju.band.entity.Task;
import cn.edu.mju.band.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 运用 Mockito 对持久层（本项目唯一的外部依赖）打桩，
 * 验证数据库宕机、断网等极端情况下，业务校验与异常处理仍稳定运行。
 * 环节三改造后，打桩方法名同步为 BaseMapper 的方法。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("外部依赖解耦与极端场景测试")
class ServiceResilienceTest {

    @Mock
    private TaskDao taskDao;

    @Mock
    private UserDao userDao;

    @InjectMocks
    private TaskBussImpl taskBuss;

    @InjectMocks
    private UserBussImpl userBuss;

    private Task newTask() {
        Task task = new Task();
        task.setTitle("测试任务");
        task.setContent("极端场景");
        task.setTaskDate(LocalDate.of(2026, 11, 3));
        task.setUserId(7L);
        return task;
    }

    private User newUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("123456");
        return user;
    }

    // ============ 数据库宕机：持久层全部抛 DataAccessResourceFailureException ============

    @Test
    @DisplayName("DB 宕机：add 抛异常，默认值逻辑仍执行，ArgumentCaptor 验证传入对象")
    void add_whenDatabaseDown_shouldThrowAndStillApplyDefaults() {
        Task task = newTask();
        doThrow(new DataAccessResourceFailureException("Communications link failure"))
                .when(taskDao).insert(any(Task.class));

        assertThrows(DataAccessResourceFailureException.class, () -> taskBuss.add(task));

        // 业务默认值在访问 DAO 之前已设置，证明业务逻辑不依赖数据库状态
        assertEquals(0, task.getStatus());
        assertEquals(2, task.getPriority());

        // 捕获真正传给持久层的对象，验证内容没有被异常破坏
        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskDao).insert(captor.capture());
        assertEquals("测试任务", captor.getValue().getTitle());
        assertEquals(7L, captor.getValue().getUserId());
    }

    @Test
    @DisplayName("DB 宕机：listByUser 抛异常而不是返回错误/空数据")
    void listByUser_whenDatabaseDown_shouldThrow() {
        when(taskDao.selectList(any())).thenThrow(
                new DataAccessResourceFailureException("connection lost"));

        assertThrows(DataAccessResourceFailureException.class,
                () -> taskBuss.listByUser(7L));
    }

    @Test
    @DisplayName("DB 宕机：listAll 抛异常")
    void listAll_whenDatabaseDown_shouldThrow() {
        when(taskDao.selectList(any())).thenThrow(
                new DataAccessResourceFailureException("database down"));

        assertThrows(DataAccessResourceFailureException.class, () -> taskBuss.listAll());
    }

    @Test
    @DisplayName("DB 宕机：get 抛异常")
    void get_whenDatabaseDown_shouldThrow() {
        when(taskDao.selectById(1L)).thenThrow(
                new DataAccessResourceFailureException("database down"));

        assertThrows(DataAccessResourceFailureException.class, () -> taskBuss.get(1L));
    }

    @Test
    @DisplayName("DB 宕机：update 抛异常")
    void update_whenDatabaseDown_shouldThrow() {
        doThrow(new DataAccessResourceFailureException("database down"))
                .when(taskDao).updateById(any(Task.class));

        assertThrows(DataAccessResourceFailureException.class,
                () -> taskBuss.update(newTask()));
    }

    @Test
    @DisplayName("DB 宕机：remove 抛异常")
    void remove_whenDatabaseDown_shouldThrow() {
        doThrow(new DataAccessResourceFailureException("database down"))
                .when(taskDao).deleteById(1L);

        assertThrows(DataAccessResourceFailureException.class, () -> taskBuss.remove(1L));
    }

    @Test
    @DisplayName("DB 宕机：分页查询抛异常，而不是返回空页误导用户")
    void page_whenDatabaseDown_shouldThrow() {
        when(taskDao.selectPage(any(), any())).thenThrow(
                new DataAccessResourceFailureException("database down"));

        assertThrows(DataAccessResourceFailureException.class,
                () -> taskBuss.page(1, 10, null, null, 7L));
    }

    // ============ 断网：获取不到连接，抛 CannotGetJdbcConnectionException ============

    @Test
    @DisplayName("断网：add 时连接获取失败，异常原样向上抛出")
    void add_whenNetworkOutage_shouldThrow() {
        doThrow(new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection"))
                .when(taskDao).insert(any(Task.class));

        CannotGetJdbcConnectionException ex = assertThrows(
                CannotGetJdbcConnectionException.class, () -> taskBuss.add(newTask()));
        assertEquals("Failed to obtain JDBC Connection", ex.getMessage());
    }

    // ============ 登录相关极端场景 ============

    @Test
    @DisplayName("DB 宕机：login 抛异常，与“密码错误返回 null”明确区分")
    void login_whenDatabaseDown_shouldThrow() {
        when(userDao.selectOne(any())).thenThrow(
                new DataAccessResourceFailureException("database down"));

        assertThrows(DataAccessResourceFailureException.class,
                () -> userBuss.login("admin", "123456"));
    }

    @Test
    @DisplayName("DB 宕机：空白用户名时业务校验先行，返回 null 且完全不访问 DAO")
    void login_whenUsernameBlankAndDatabaseDown_shouldNotTouchDao() {
        User result = userBuss.login("   ", "123456");

        assertEquals(null, result);
        verifyNoInteractions(userDao);
    }

    @Test
    @DisplayName("连续打桩：第一次登录 DB 宕机失败，恢复后第二次登录成功")
    void login_firstFailsThenRecovers_shouldSucceedSecondTime() {
        User user = newUser();
        when(userDao.selectOne(any()))
                .thenThrow(new DataAccessResourceFailureException("temporary outage"))
                .thenReturn(user);

        assertThrows(DataAccessResourceFailureException.class,
                () -> userBuss.login("admin", "123456"));
        User second = userBuss.login("admin", "123456");

        assertSame(user, second);
        verify(userDao, org.mockito.Mockito.times(2)).selectOne(any());
    }

    @Test
    @DisplayName("DB 宕机：getById 抛异常")
    void getById_whenDatabaseDown_shouldThrow() {
        when(userDao.selectById(1L)).thenThrow(
                new DataAccessResourceFailureException("database down"));

        assertThrows(DataAccessResourceFailureException.class, () -> userBuss.getById(1L));
    }
}
