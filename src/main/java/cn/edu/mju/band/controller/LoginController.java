package cn.edu.mju.band.controller;

import cn.edu.mju.band.buss.IUserBuss;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @Autowired
    private IUserBuss userBuss;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String dologin(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "password", required = false) String password,
            HttpSession session
    ) {
        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            session.setAttribute("msg", "用户名或密码不能为空！");
            session.setAttribute("flag", false);
            return "redirect:/login";
        }

        if (userBuss.login(username.trim(), password)) {
            session.setAttribute("loginUser", username.trim());
            session.setAttribute("flag", true);
            session.removeAttribute("msg");
            return "redirect:/task";
        } else {
            session.setAttribute("msg", "用户名或密码错误！");
            session.setAttribute("flag", false);
            return "redirect:/login";
        }
    }
}
