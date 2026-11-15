package cn.edu.mju.band.dal;

import cn.edu.mju.band.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 环节三：原 selectByUsername 由业务层用 LambdaQueryWrapper 表达，
 * 简单条件查询不再需要单独写 DAO 方法，接口只剩 BaseMapper 的通用能力。
 */
public interface UserDao extends BaseMapper<User> {
}
