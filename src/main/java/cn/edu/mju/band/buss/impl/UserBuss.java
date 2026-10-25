package cn.edu.mju.band.buss.impl;

import cn.edu.mju.band.buss.IUserBuss;
import org.springframework.stereotype.Service;

@Service
public class UserBuss implements IUserBuss {

    @Override
    public boolean login(String username, String password) {
        return "admin".equals(username) && "123456".equals(password);
    }
}