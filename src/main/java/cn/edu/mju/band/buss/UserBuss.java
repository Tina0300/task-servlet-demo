package cn.edu.mju.band.buss;

import cn.edu.mju.band.entity.User;

public interface UserBuss {
    /** 校验通过返回用户对象，失败返回 null */
    User login(String username, String password);
    User getById(Long id);
}
