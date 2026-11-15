package cn.edu.mju.band.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 环节三：MyBatis-Plus 拦截器链。
 * 分页插件必须显式注册，否则 Page 查询不会追加 LIMIT，SQL 会把整表数据查回内存。
 */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor pagination = new PaginationInnerInterceptor(DbType.MYSQL);
        // 单页最大条数保护：即使请求传 size=100000，也只取 500 条，防止被恶意拉库
        pagination.setMaxLimit(500L);
        // 超过最大页后返回第一页，避免越界查询得到空列表
        pagination.setOverflow(true);
        interceptor.addInnerInterceptor(pagination);
        return interceptor;
    }
}
