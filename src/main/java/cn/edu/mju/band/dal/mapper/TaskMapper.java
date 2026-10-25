package cn.edu.mju.band.dal.mapper;

import cn.edu.mju.band.entity.Task;

import java.util.List;

/**
 * MyBatis Mapper 接口（数据访问层 SQL 映射）
 * SQL 定义在 resources/mapper/TaskMapper.xml 中，接口只声明方法
 */
public interface TaskMapper {

    List<Task> list();

    Task getById(Integer id);

    int add(Task task);

    int update(Task task);

    int delete(Integer id);
}
