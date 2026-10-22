package cn.edu.mju.band.buss;

import cn.edu.mju.band.entity.Task;
import java.util.List;

public interface ITaskBuss {
    List<Task> list();
    boolean add(Task task);
    boolean update(Task task);
    Task findById(int id);
    boolean delete(int id);
    Task findById(Integer id);
}
