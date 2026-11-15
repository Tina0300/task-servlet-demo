package cn.edu.mju.band.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 环节四：登录表单对象。字段名与页面 name 一致，
 * 校验失败时由 BindingResult 收集，回到登录页用 th:errors 回显。
 */
public class LoginForm {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过 50 个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在 6 - 100 个字符之间")
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
