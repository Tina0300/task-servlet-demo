package cn.edu.mju.band.controller;

import cn.edu.mju.band.buss.TaskBuss;
import cn.edu.mju.band.entity.Task;
import cn.edu.mju.band.interceptor.LoginInterceptor;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Objects;

@Controller
@RequestMapping("/task")
public class TaskController {

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);

    private final TaskBuss taskBuss;

    public TaskController(TaskBuss taskBuss) {
        this.taskBuss = taskBuss;
    }

    @GetMapping
    public String list(@RequestParam(value = "current", defaultValue = "1") long current,
                       @RequestParam(value = "size", defaultValue = "10") long size,
                       @RequestParam(value = "keyword", required = false) String keyword,
                       @RequestParam(value = "status", required = false) Integer status,
                       HttpSession session,
                       Model model) {
        Long userId = (Long) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        IPage<Task> page = taskBuss.page(current, size, keyword, status, userId);
        model.addAttribute("page", page);
        model.addAttribute("tasks", page.getRecords());
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        return "task";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("task", new Task());
        model.addAttribute("opt", "add");
        return "add_update";
    }

    /**
     * 查看编辑表单前校验任务归属。
     * 归属不一致或数据缺失时一律拒绝，并记录审计日志。
     */
    @GetMapping("/update/{id}")
    public String updateForm(@PathVariable("id") Long id,
                             @RequestParam(value = "opt", defaultValue = "update") String opt,
                             HttpSession session,
                             RedirectAttributes ra,
                             Model model) {
        Long userId = (Long) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        Task task = taskBuss.get(id);

        if (task == null) {
            ra.addFlashAttribute("msg", "任务不存在或已被删除");
            return "redirect:/task";
        }
        if (!Objects.equals(task.getUserId(), userId)) {
            log.warn("越权编辑被拦截 taskId={} ownerId={} currentUserId={}",
                    id, task.getUserId(), userId);
            ra.addFlashAttribute("msg", "无权操作该任务");
            return "redirect:/task";
        }

        model.addAttribute("task", task);
        model.addAttribute("opt", opt);
        return "add_update";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("task") Task task,
                       BindingResult result,
                       @RequestParam("opt") String opt,
                       HttpSession session,
                       Model model,
                       RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("opt", opt);
            return "add_update";
        }
        if ("add".equalsIgnoreCase(opt)) {
            task.setUserId((Long) session.getAttribute(LoginInterceptor.SESSION_USER_ID));
            taskBuss.add(task);
            ra.addFlashAttribute("msg", "新增成功");
        } else {
            taskBuss.update(task);
            ra.addFlashAttribute("msg", "修改成功");
        }
        return "redirect:/task";
    }

    /**
     * 删除前校验任务归属，三种结果分别给出明确提示，越权尝试留痕。
     */
    @PostMapping("/delete")
    public String delete(@RequestParam("id") Long id,
                         HttpSession session,
                         RedirectAttributes ra) {
        Long userId = (Long) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        Task task = taskBuss.get(id);

        if (task == null) {
            ra.addFlashAttribute("msg", "任务不存在或已被删除");
            return "redirect:/task";
        }
        if (!Objects.equals(task.getUserId(), userId)) {
            log.warn("越权删除被拦截 taskId={} ownerId={} currentUserId={}",
                    id, task.getUserId(), userId);
            ra.addFlashAttribute("msg", "无权操作该任务");
            return "redirect:/task";
        }

        taskBuss.remove(id);
        ra.addFlashAttribute("msg", "删除成功");
        return "redirect:/task";
    }
}
