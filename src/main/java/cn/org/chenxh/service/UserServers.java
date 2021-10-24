package cn.org.chenxh.service;

import cn.org.chenxh.entity.User;

import java.util.List;

public interface UserServers {
    public User saveUser(User user);
    public List<User> findUserAll();
    public List<User> findByDid(Integer id);
    public User findById(String id);
    public User findByUname(String uname);
    public User findByUnameAndPwd(String uname,String pwd);
}
