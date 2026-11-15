package cn.edu.mju.band.buss.impl;

import cn.edu.mju.band.buss.TaskBuss;
import cn.edu.mju.band.dal.TaskDao;
import cn.edu.mju.band.entity.Task;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 环节三：全部持久层调用改为 BaseMapper 通用方法 + 条件构造器，
 * 不再有手写 SQL、不再有 DAO 接口方法名与 XML 的对应关系。
 */
@Service("taskBuss")
public class TaskBussImpl implements TaskBuss {
    private final TaskDao taskDao;

    /** 构造器注入：单构造器可省略 @Autowired */
    public TaskBussImpl(TaskDao taskDao) {
        this.taskDao = taskDao;
    }

    @Override
    public List<Task> listByUser(Long userId) {
        if (userId == null) {
            return listAll();
        }
        LambdaQueryWrapper<Task> wrapper = Wrappers.<Task>lambdaQuery()
                .eq(Task::getUserId, userId)
                .orderByAsc(Task::getStatus)
                .orderByAsc(Task::getTaskDate)
                .orderByDesc(Task::getId);
        return taskDao.selectList(wrapper);
    }

    @Override
    public List<Task> listAll() {
        return taskDao.selectList(Wrappers.<Task>lambdaQuery().orderByDesc(Task::getId));
    }

    @Override
    public Task get(Long id) {
        return id == null ? null : taskDao.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Task task) {
        // 合法性校验：标题不允许为空白，在访问持久层之前拦截
        if (task == null || task.getTitle() == null || task.getTitle().isBlank()) {
            throw new IllegalArgumentException("任务标题不能为空");
        }
        if (task.getStatus() == null) {
            task.setStatus(0);
        }
        if (task.getPriority() == null) {
            task.setPriority(2);
        }
        taskDao.insert(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Task task) {
        taskDao.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        taskDao.deleteById(id);
    }

    @Override
    public IPage<Task> page(long current, long size, String keyword, Integer status, Long userId) {
        // current/size 做兜底，避免前端传 0 或负数导致 SQL 拼接异常
        Page<Task> page = new Page<>(Math.max(current, 1L), Math.max(size, 1L));

        LambdaQueryWrapper<Task> wrapper = Wrappers.<Task>lambdaQuery();
        if (userId != null) {
            wrapper.eq(Task::getUserId, userId);
        }
        if (status != null) {
            wrapper.eq(Task::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            // and(...) 保证括号优先级正确：user/status 与「标题 or 内容」之间是 AND
            wrapper.and(w -> w.like(Task::getTitle, kw).or().like(Task::getContent, kw));
        }
        wrapper.orderByDesc(Task::getId);

        return taskDao.selectPage(page, wrapper);
    }
}
