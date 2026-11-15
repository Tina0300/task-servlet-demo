package cn.edu.mju.band.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 环节三：实体与表的映射改由 MyBatis-Plus 注解声明，不再依赖 mapper XML 的 resultMap。
 * 环节四：@NotBlank / @Size 是服务端校验规则的唯一来源，前端 required 只是体验优化。
 */
@TableName("task")
public class Task implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 与 DDL 的 AUTO_INCREMENT 对齐，插入后主键回填 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "任务标题不能为空")
    @Size(max = 120, message = "标题长度不能超过 120 个字符")
    private String title;

    @Size(max = 1000, message = "内容长度不能超过 1000 个字符")
    private String content;

    /** 1 低 / 2 中 / 3 高 */
    private Integer priority;

    /** 0 未完成 / 1 已完成 */
    private Integer status;

    /** 表单提交 yyyy-MM-dd，必须声明 ISO 格式，否则绑定失败返回 400 */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @TableField("task_date")
    private LocalDate taskDate;

    @TableField("user_id")
    private Long userId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDate getTaskDate() { return taskDate; }
    public void setTaskDate(LocalDate taskDate) { this.taskDate = taskDate; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    /** 模板里直接判断状态，避免页面写三元表达式 */
    public boolean isFinished() { return status != null && status == 1; }
}
