package cn.edu.mju.band.config;

import cn.edu.mju.band.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 环节五：注册 LoginInterceptor 并声明白名单。
 * 白名单用 excludePathPatterns 表达，比原过滤器里的字符串前缀匹配更直观、也更不容易误放行。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;

    public WebConfig(LoginInterceptor loginInterceptor) {
        this.loginInterceptor = loginInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",          // 登录页
                        "/doLogin",        // 登录提交
                        "/logout",         // 退出登录
                        "/css/**",         // 静态资源
                        "/js/**",
                        "/images/**",
                        "/webjars/**",
                        "/favicon.ico",
                        "/error",          // 错误页，避免错误页自身被拦截后无限重定向
                        "/error/**"
                );
    }
}
