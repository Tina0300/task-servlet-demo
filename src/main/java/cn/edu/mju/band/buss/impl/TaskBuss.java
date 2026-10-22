package cn.edu.mju.band.buss.impl;

import cn.edu.mju.band.dal.ITaskDal;
import cn.edu.mju.band.dal.impl.TaskDal;
import cn.edu.mju.band.entity.Task;

import java.util.List;

public class TaskBuss {
    private ITaskDal taskDal = new TaskDal();

    public List<Task> list(){
        return taskDal.findAll();
    }

    public Task getById(Integer id){
        return taskDal.findById(id);
    }

    public int add(Task task){
        return taskDal.add(task);
    }

    public int update(Task task){
        return taskDal.update(task);
    }

    public int delete(Integer id){
        return taskDal.deleteById(id);
    }
}