package cn.edu.mju.band.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
    /** 同时接管根路径，访问 http://localhost:8080/ 直接进首页 */
    @GetMapping({"/", "/index"})
    public String index() {
        return "index";                 // -> templates/index.html
    }
}
