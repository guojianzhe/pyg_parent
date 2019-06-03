package cn.itcast.core.service;

import cn.itcast.core.pojo.user.User;

public interface UserService {
    void sendCode(String phone);

    Boolean checkSmsCode(String phone, String smscode);

    void add(User user);
}
