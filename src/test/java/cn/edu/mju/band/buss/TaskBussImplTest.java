package cn.edu.mju.band.buss;

import cn.edu.mju.band.buss.impl.TaskBussImpl;
import cn.edu.mju.band.dal.TaskDao;
import cn.edu.mju.band.entity.Task;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 环节三改造后：持久层由手写 DAO 方法换成 BaseMapper，
 * 断言与打桩目标同步改为 insert / selectList / selectById / updateById / deleteById / selectPage。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TaskBussImpl 业务层测试")
class TaskBussImplTest {

    @Mock
    private TaskDao taskDao;

    @InjectMocks
    private TaskBussImpl taskBuss;

    private Task sampleTask;

    @BeforeEach
    void setUp() {
        // 每个测试方法执行前都会重建对象，保证用例之间状态互不污染
        sampleTask = new Task();
        sampleTask.setId(1L);
        sampleTask.setTitle("写实验报告");
        sampleTask.setContent("Spring Boot 迁移");
        sampleTask.setTaskDate(LocalDate.of(2026, 11, 3));
        sampleTask.setUserId(7L);
    }

    // ---------------- 正常流 ----------------

    @Test
    @DisplayName("add：status/priority 为空时自动补默认值 0 和 2，并调用一次 insert")
    void add_shouldSetDefaults_whenValuesNull() {
        sampleTask.setStatus(null);
        sampleTask.setPriority(null);

        taskBuss.add(sampleTask);

        assertEquals(0, sampleTask.getStatus());
        assertEquals(2, sampleTask.getPriority());
        verify(taskDao, times(1)).insert(sampleTask);
    }

    @Test
    @DisplayName("add：已提供 status/priority 时保留原值，不被默认值覆盖")
    void add_shouldKeepValues_whenProvided() {
        sampleTask.setStatus(1);
        sampleTask.setPriority(3);

        taskBuss.add(sampleTask);

        assertEquals(1, sampleTask.getStatus());
        assertEquals(3, sampleTask.getPriority());
        verify(taskDao, times(1)).insert(sampleTask);
    }

    @Test
    @DisplayName("异常流：标题为空白时 add 抛 IllegalArgumentException，且不访问 DAO")
    void add_shouldThrow_whenTitleBlank() {
        sampleTask.setTitle("   ");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> taskBuss.add(sampleTask));
        assertEquals("任务标题不能为空", ex.getMessage());
        // MyBatis-Plus 3.5.9 的 BaseMapper 同时有 insert(T) 与 insert(Collection<T>)，
        // 裸 any() 两个重载都匹配，编译期报「对 insert 的引用不明确」，必须带类型
        verify(taskDao, never()).insert(any(Task.class));
    }

    @Test
    @DisplayName("listByUser：传入 userId 时走条件构造器查询")
    void listByUser_shouldQueryByUserId_whenIdProvided() {
        List<Task> expected = List.of(sampleTask);
        when(taskDao.selectList(any())).thenReturn(expected);

        List<Task> actual = taskBuss.listByUser(7L);

        assertSame(expected, actual);
        verify(taskDao, times(1)).selectList(any());
    }

    @Test
    @DisplayName("listByUser：userId 为 null 时退化为全量查询（边界）")
    void listByUser_shouldQueryAll_whenIdNull() {
        when(taskDao.selectList(any())).thenReturn(List.of(sampleTask));

        List<Task> actual = taskBuss.listByUser(null);

        assertEquals(1, actual.size());
        verify(taskDao, times(1)).selectList(any());
    }

    @Test
    @DisplayName("listAll：直接委托给 selectList")
    void listAll_shouldDelegate() {
        when(taskDao.selectList(any())).thenReturn(List.of(sampleTask));

        List<Task> actual = taskBuss.listAll();

        assertEquals(1, actual.size());
        verify(taskDao, times(1)).selectList(any());
    }

    @Test
    @DisplayName("get：返回 DAO 查到的任务")
    void get_shouldReturnTask() {
        when(taskDao.selectById(1L)).thenReturn(sampleTask);

        Task actual = taskBuss.get(1L);

        assertSame(sampleTask, actual);
    }

    @Test
    @DisplayName("get：任务不存在时返回 null（边界）")
    void get_shouldReturnNull_whenMissing() {
        assertNull(taskBuss.get(99L));
        verify(taskDao, times(1)).selectById(99L);
    }

    @Test
    @DisplayName("update：委托给 BaseMapper 的 updateById")
    void update_shouldDelegate() {
        taskBuss.update(sampleTask);

        verify(taskDao, times(1)).updateById(sampleTask);
    }

    @Test
    @DisplayName("remove：委托给 BaseMapper 的 deleteById")
    void remove_shouldDelegate() {
        taskBuss.remove(1L);

        verify(taskDao, times(1)).deleteById(1L);
    }

    // ---------------- 环节三：分页与条件查询 ----------------

    @Test
    @DisplayName("page：页码与每页条数原样传给 selectPage")
    void page_shouldPassPaginationParams() {
        taskBuss.page(2, 5, "报告", 0, 7L);

        @SuppressWarnings("rawtypes")
        ArgumentCaptor<IPage> captor = ArgumentCaptor.forClass(IPage.class);
        verify(taskDao, times(1)).selectPage(captor.capture(), any());

        assertEquals(2L, captor.getValue().getCurrent());
        assertEquals(5L, captor.getValue().getSize());
    }

    @Test
    @DisplayName("page：非法的页码/条数被兜底为 1，避免生成非法 SQL")
    void page_shouldClampIllegalParams() {
        taskBuss.page(0, 0, null, null, null);

        @SuppressWarnings("rawtypes")
        ArgumentCaptor<IPage> captor = ArgumentCaptor.forClass(IPage.class);
        verify(taskDao).selectPage(captor.capture(), any());

        assertEquals(1L, captor.getValue().getCurrent());
        assertEquals(1L, captor.getValue().getSize());
    }

    // ---------------- 异常流 ----------------

    @Test
    @DisplayName("add：DAO 抛异常时业务方法原样抛出，不吞异常")
    void add_shouldPropagateException_whenDaoFails() {
        sampleTask.setStatus(null);
        sampleTask.setPriority(null);
        doThrow(new RuntimeException("database down"))
                .when(taskDao).insert(any(Task.class));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> taskBuss.add(sampleTask));
        assertEquals("database down", ex.getMessage());
    }

    @Test
    @DisplayName("remove：DAO 删除失败时异常原样抛出")
    void remove_shouldPropagateException_whenDaoFails() {
        doThrow(new RuntimeException("connection lost"))
                .when(taskDao).deleteById(1L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> taskBuss.remove(1L));
        assertEquals("connection lost", ex.getMessage());
    }
}
