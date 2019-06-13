package com.itcast.core.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;

/**
 * 自定义验证类
 * 再之前这里负责用户名密码的校验工作,并给当前用户赋予对应的访问权限
 * 现在cas和
 */
public class UserDetailServiceImpl implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //获取权限集合
        ArrayList<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();
        //向权限集合中加入访问权限
        authList.add(new SimpleGrantedAuthority("ROLE_USER"));


        return new User(username,"",authList);
    }
}
