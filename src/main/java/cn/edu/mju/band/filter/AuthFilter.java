package cn.edu.mju.band.filter;

/**
 * 环节五：本类已被 cn.edu.mju.band.interceptor.LoginInterceptor + WebConfig 取代。
 * 这里刻意去掉 @Component / @Order 与 Filter 接口实现，保证它不会再被 Servlet 容器注册，
 * 因此不存在「过滤器 + 拦截器」双重拦截的问题。
 * 该文件仅为兼容历史引用而保留，可直接删除。
 */
@Deprecated
public final class AuthFilter {

    private AuthFilter() {
    }
}
