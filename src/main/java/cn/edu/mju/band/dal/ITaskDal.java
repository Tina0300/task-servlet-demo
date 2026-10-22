package cn.edu.mju.band.dal;

import cn.edu.mju.band.entity.Task;
import java.util.List;

public interface ITaskDal {
    List<Task> findAll();
    Task findById(Integer id);
    int add(Task task);
    int update(Task task);
    int deleteById(Integer id);
}
