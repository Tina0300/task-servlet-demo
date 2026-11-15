package cn.edu.mju.band.buss;

import cn.edu.mju.band.entity.Task;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * 环节三：新增 page 方法承载分页 + 条件查询，
 * 控制器只传检索条件，具体 SQL 由 LambdaQueryWrapper 生成。
 */
public interface TaskBuss {
    List<Task> listByUser(Long userId);
    List<Task> listAll();
    Task get(Long id);
    void add(Task task);
    void update(Task task);
    void remove(Long id);

    /**
     * 分页 + 条件查询。
     *
     * @param current 当前页码，从 1 开始
     * @param size    每页条数
     * @param keyword 标题/内容模糊关键字，可为空
     * @param status  状态过滤 0 未完成 / 1 已完成，null 表示不限制
     * @param userId  所属用户，null 表示不限制（管理视角）
     */
    IPage<Task> page(long current, long size, String keyword, Integer status, Long userId);
}
