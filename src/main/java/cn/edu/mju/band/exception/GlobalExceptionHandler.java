package cn.edu.mju.band.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 环节四：全局统一异常处理。
 * 有了 @ControllerAdvice，控制器里不用再逐个 try-catch，异常出口只有一处，
 * user 看到的永远是同一张错误页，而堆栈只进日志。
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 表单绑定/校验失败（MethodArgumentNotValidException 是它的子类） */
    @ExceptionHandler(BindException.class)
    public String handleBindException(BindException ex, HttpServletRequest request, Model model) {
        log.warn("表单校验失败 path={} errors={}", request.getRequestURI(), ex.getBindingResult().getErrorCount());
        model.addAttribute("status", 400);
        model.addAttribute("message", "提交的数据不符合要求，请检查后重新填写。");
        model.addAttribute("path", request.getRequestURI());
        return "error";
    }

    /** 业务参数非法，例如业务层主动抛出的 IllegalArgumentException */
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request, Model model) {
        log.warn("业务参数非法 path={} msg={}", request.getRequestURI(), ex.getMessage());
        model.addAttribute("status", 400);
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("path", request.getRequestURI());
        return "error";
    }

    /** 兜底：数据库不可用、空指针等未预期异常，只暴露一句话，堆栈留在日志 */
    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, HttpServletRequest request, Model model) {
        log.error("未处理异常 path={}", request.getRequestURI(), ex);
        model.addAttribute("status", 500);
        model.addAttribute("message", "系统开小差了，请稍后重试或联系管理员。");
        model.addAttribute("path", request.getRequestURI());
        return "error";
    }
}
