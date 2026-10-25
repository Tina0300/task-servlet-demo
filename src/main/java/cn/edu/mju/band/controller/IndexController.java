package cn.edu.mju.band.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping({"/", "/index"})
    public String index(HttpSession session) {
        if (session != null && session.getAttribute("loginUser") != null) {
            return "redirect:/task";
        }
        return "redirect:/login";
    }
}
