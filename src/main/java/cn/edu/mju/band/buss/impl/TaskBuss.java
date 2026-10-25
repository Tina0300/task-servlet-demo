package cn.edu.mju.band.buss.impl;

import cn.edu.mju.band.buss.ITaskBuss;
import cn.edu.mju.band.dal.ITaskDal;
import cn.edu.mju.band.entity.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 业务层实现
 * 注入方式演示：本类使用 【构造器注入】(@Autowired 标注在构造器上，字段 final 不可变)，
 * 与 TaskDal 中的【setter 注入】形成对比（详见 TaskDal 注释）。
 */
@Service
public class TaskBuss implements ITaskBuss {

    private final ITaskDal taskDal;

    /** 构造器注入：Spring 容器在创建 Bean 时通过构造器传入 ITaskDal 实现 */
    @Autowired
    public TaskBuss(ITaskDal taskDal) {
        this.taskDal = taskDal;
    }

    @Override
    public List<Task> list() {
        return taskDal.list();
    }

    @Override
    public Task getById(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("任务ID不能为空");
        }
        return taskDal.getById(id);
    }

    @Override
    @Transactional
    public boolean add(Task task) {
        if (task == null || task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("任务标题不能为空");
        }
        task.setTitle(task.getTitle().trim());
        if (task.getStatus() == null) {
            task.setStatus((byte) 0);
        }
        return taskDal.add(task) > 0;
    }

    @Override
    @Transactional
    public boolean update(Task task) {
        if (task == null || task.getId() == null) {
            throw new IllegalArgumentException("任务ID不能为空");
        }
        Task oldTask = taskDal.getById(task.getId());
        if (oldTask == null) {
            throw new IllegalArgumentException("要更新的任务不存在");
        }
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("任务标题不能为空");
        }
        task.setTitle(task.getTitle().trim());
        return taskDal.update(task) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("任务ID不能为空");
        }
        return taskDal.delete(id) > 0;
    }

    @Override
    @Transactional
    public boolean startTask(Integer id) {
        Task task = getById(id);
        if (task == null) {
            return false;
        }
        if (task.getStatus() == null || task.getStatus() == 2) {
            return false;
        }
        task.setStatus((byte) 1);
        return taskDal.update(task) > 0;
    }

    @Override
    @Transactional
    public boolean finishTask(Integer id) {
        Task task = getById(id);
        if (task == null) {
            return false;
        }
        if (task.getStatus() == null || task.getStatus() == 2) {
            return false;
        }
        task.setStatus((byte) 2);
        return taskDal.update(task) > 0;
    }

    @Override
    @Transactional
    public boolean cancelTask(Integer id) {
        Task task = getById(id);
        if (task == null) {
            return false;
        }
        task.setStatus((byte) 0);
        return taskDal.update(task) > 0;
    }
}
