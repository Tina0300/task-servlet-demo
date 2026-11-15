package cn.edu.mju.band.controller;

import cn.edu.mju.band.buss.TaskBuss;
import cn.edu.mju.band.entity.Task;
import cn.edu.mju.band.interceptor.LoginInterceptor;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/task")
public class TaskController {
    private final TaskBuss taskBuss;

    public TaskController(TaskBuss taskBuss) {
        this.taskBuss = taskBuss;
    }

    /**
     * 环节三：列表改为分页 + 条件查询。
     * 三个查询参数都是可选的，不传就等价于「第一页、每页 10 条、无过滤」。
     */
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

    /** 新增表单：GET /task/add */
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("task", new Task());
        model.addAttribute("opt", "add");
        return "add_update";
    }

    /** 修改表单：GET /task/update/5?opt=update */
    @GetMapping("/update/{id}")
    public String updateForm(@PathVariable("id") Long id,
                             @RequestParam(value = "opt", defaultValue = "update") String opt,
                             Model model) {
        Task task = taskBuss.get(id);
        if (task == null) {
            return "redirect:/task";
        }
        model.addAttribute("task", task);
        model.addAttribute("opt", opt);
        return "add_update";
    }

    /**
     * 环节四：@Valid 触发实体上的约束注解，BindingResult 必须紧跟其后。
     * 有错误时不进业务层，直接把带错误信息的表单回显给用户；
     * 前端 required 只能挡住普通用户，绕过浏览器直接发 POST 时，只有这里的服务端校验生效。
     */
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("task") Task task,
                       BindingResult result,
                       @RequestParam("opt") String opt,
                       HttpSession session,
                       Model model,
                       RedirectAttributes ra) {
        if (result.hasErrors()) {
            // opt 决定标题显示「新增」还是「修改」，回显时必须重新带上
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

    /** 删除：POST /task/delete（用 POST 避免爬虫/预加载误删） */
    @PostMapping("/delete")
    public String delete(@RequestParam("id") Long id, RedirectAttributes ra) {
        taskBuss.remove(id);
        ra.addFlashAttribute("msg", "删除成功");
        return "redirect:/task";
    }
}
