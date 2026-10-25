package cn.edu.mju.band.buss;

import cn.edu.mju.band.entity.Task;
import java.util.List;

public interface ITaskBuss {
    List<Task> list();
    Task getById(Integer id);
    boolean add(Task task);
    boolean update(Task task);
    boolean delete(Integer id);
    boolean startTask(Integer id);
    boolean finishTask(Integer id);
    boolean cancelTask(Integer id);
}
