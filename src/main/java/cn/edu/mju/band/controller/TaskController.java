package cn.edu.mju.band.controller;

import cn.edu.mju.band.buss.ITaskBuss;
import cn.edu.mju.band.entity.Task;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.SimpleDateFormat;
import java.util.List;

@Controller
public class TaskController {
    private final ITaskBuss taskBuss;

    /** 构造器注入：Spring 容器在创建 Bean 时通过构造器传入 ITaskBuss 实现 */
    @org.springframework.beans.factory.annotation.Autowired
    public TaskController(ITaskBuss taskBuss) {
        this.taskBuss = taskBuss;
    }

    @GetMapping("/task")
    public String list(Model model) {
        List<Task> taskList = taskBuss.list();
        model.addAttribute("lst", taskList);
        return "list";
    }

    @GetMapping("/task/add")
    public String add(Model model) {
        Task task = new Task();
        task.setTitle("");
        task.setPriority("1");
        task.setStatus((byte) 0);
        model.addAttribute("task", task);
        return "add";
    }

    @GetMapping("/task/update")
    public String update(@RequestParam("id") Integer id, Model model) {
        Task task = taskBuss.getById(id);
        model.addAttribute("task", task);
        return "add";
    }

    @GetMapping("/task/delete")
    public String delete(@RequestParam("id") Integer id) {
        taskBuss.delete(id);
        return "redirect:/task";
    }

    @GetMapping("/task/start")
    public String startTask(@RequestParam("id") Integer id) {
        taskBuss.startTask(id);
        return "redirect:/task";
    }

    @GetMapping("/task/finish")
    public String finishTask(@RequestParam("id") Integer id) {
        taskBuss.finishTask(id);
        return "redirect:/task";
    }

    @GetMapping("/task/cancel")
    public String cancelTask(@RequestParam("id") Integer id) {
        taskBuss.cancelTask(id);
        return "redirect:/task";
    }

    @PostMapping("/task/save")
    public String save(
            @RequestParam(value = "id", required = false) Integer id,
            @RequestParam("title") String title,
            @RequestParam("priority") String priority,
            @RequestParam("status") Byte status,
            @RequestParam(value = "dueDate", required = false) String dueDate
    ) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setPriority(priority);
        task.setStatus(status);

        try {
            if (dueDate != null && !dueDate.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                task.setDueDate(sdf.parse(dueDate));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("日期格式不正确", e);
        }

        if (id == null) {
            taskBuss.add(task);
        } else {
            taskBuss.update(task);
        }

        return "redirect:/task";
    }
}
