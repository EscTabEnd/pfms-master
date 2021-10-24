package cn.org.chenxh.service.impl;

import cn.org.chenxh.entity.UserDao;
import cn.org.chenxh.entity.Logs;
import cn.org.chenxh.entity.LogsDao;
import cn.org.chenxh.entity.User;
import cn.org.chenxh.service.LogsServers;
import cn.org.chenxh.service.UserServers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("logsServers")
public class LogsServersImpl implements LogsServers {
    @Autowired
    private LogsDao logsDao;



    @Override
    public Logs saveLogs(Logs logs) {
        return logsDao.save(logs);
    }

    @Override
    public List<Logs> findLogsAll() {
        return logsDao.findAll();
    }

    @Override
    public Logs findByUserid(String id) {
        return logsDao.findByUserid(id);
    }

    @Override
    public List<Logs> findByUname(String name) {
        return logsDao.findByUname(name);
    }
}
