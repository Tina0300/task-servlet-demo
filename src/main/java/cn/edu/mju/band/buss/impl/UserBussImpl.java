package cn.edu.mju.band.buss.impl;

import cn.edu.mju.band.buss.UserBuss;
import cn.edu.mju.band.dal.UserDao;
import cn.edu.mju.band.entity.User;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

/**
 * 环节三：登录的用户名查询由 LambdaQueryWrapper 表达，
 * 原先 UserDao.selectByUsername 的 DAO 方法与 UserMapper.xml 已一并删除。
 */
@Service("userBuss")
public class UserBussImpl implements UserBuss {
    private final UserDao userDao;

    public UserBussImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User login(String username, String password) {
        if (username == null || username.isBlank()) {
            return null;
        }
        User user = userDao.selectOne(Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, username.trim()));
        if (user == null) {
            return null;
        }
        // 教学示例用明文比对；生产环境请改为 BCrypt 等加盐哈希
        return password != null && password.equals(user.getPassword()) ? user : null;
    }

    @Override
    public User getById(Long id) {
        return id == null ? null : userDao.selectById(id);
    }
}
