package cn.edu.mju.band.controller;

import cn.edu.mju.band.buss.UserBuss;
import cn.edu.mju.band.dto.LoginForm;
import cn.edu.mju.band.entity.User;
import cn.edu.mju.band.interceptor.LoginInterceptor;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LoginController {
    private final UserBuss userBuss;

    public LoginController(UserBuss userBuss) {
        this.userBuss = userBuss;
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        // 页面用 th:object="${form}" 绑定，必须先放一个空对象，否则表单渲染取不到字段
        model.addAttribute("form", new LoginForm());
        return "login";
    }

    /**
     * 环节四 + 环节五：@Valid 校验登录表单；校验通过后查库；
     * 登录成功才写入 Session 标记，LoginInterceptor 依赖该标记判断登录态。
     */
    @PostMapping("/doLogin")
    public String doLogin(@Valid @ModelAttribute("form") LoginForm form,
                          BindingResult result,
                          HttpSession session,
                          Model model) {
        if (result.hasErrors()) {
            // 校验失败：保留用户已填内容，错误信息由 th:errors 在字段旁回显
            return "login";
        }
        User user = userBuss.login(form.getUsername(), form.getPassword());
        if (user == null) {
            model.addAttribute("error", "用户名或密码错误");
            return "login";
        }
        session.setAttribute(LoginInterceptor.SESSION_FLAG, Boolean.TRUE);
        session.setAttribute(LoginInterceptor.SESSION_USER_ID, user.getId());
        session.setAttribute(LoginInterceptor.SESSION_LOGIN_USER, user.getUsername());
        return "redirect:/index";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
