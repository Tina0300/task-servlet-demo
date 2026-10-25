package cn.edu.mju.band.dal.impl;

import cn.edu.mju.band.dal.ITaskDal;
import cn.edu.mju.band.dal.mapper.TaskMapper;
import cn.edu.mju.band.entity.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 数据访问层实现（MyBatis 版）
 * 通过注入 MyBatis Mapper 完成 SQL 操作，替代原 JDBC 手写实现
 *
 * 注入方式演示：本类使用 【setter 注入】（@Autowired 标注在 setter 方法上），
 * 与 TaskBuss 中的【构造器注入】形成对比：
 *   - setter 注入：字段可变，依赖可在初始化后更换，适合可选依赖；
 *   - 构造器注入：字段 final 不可变，依赖在创建时一次性确定，适合必选依赖（Spring 官方推荐）。
 */
@Repository
public class TaskDal implements ITaskDal {

    private TaskMapper taskMapper;

    /** setter 注入：Spring 容器在创建 Bean 后调用此方法注入 TaskMapper */
    @Autowired
    public void setTaskMapper(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    @Override
    public List<Task> list() {
        return taskMapper.list();
    }

    @Override
    public Task getById(Integer id) {
        return taskMapper.getById(id);
    }

    @Override
    public int add(Task task) {
        return taskMapper.add(task);
    }

    @Override
    public int update(Task task) {
        return taskMapper.update(task);
    }

    @Override
    public int delete(Integer id) {
        return taskMapper.delete(id);
    }
}
