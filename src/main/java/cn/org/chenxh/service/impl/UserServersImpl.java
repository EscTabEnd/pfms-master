package cn.org.chenxh.service.impl;

import cn.org.chenxh.entity.UserDao;
import cn.org.chenxh.entity.User;
import cn.org.chenxh.service.UserServers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("userServers")
public class UserServersImpl implements UserServers {
    @Autowired
    private UserDao userDao;


    @Override
    public User saveUser(User user) {
        return userDao.save(user);
    }

    @Override
    public List<User> findUserAll() {
        return userDao.findAll();
    }

    @Override
    public List<User> findByDid(Integer id) {
        return userDao.findByDid(id);
    }

    @Override
    public User findById(String id) {
        return userDao.findById(Integer.parseInt(id));
    }

    @Override
    public User findByUname(String uname) {
        return userDao.findByUname(uname);
    }

    @Override
    public User findByUnameAndPwd(String uname,String pwd) {
        return userDao.findByUnameAndPwd(uname,pwd);
    }


}
