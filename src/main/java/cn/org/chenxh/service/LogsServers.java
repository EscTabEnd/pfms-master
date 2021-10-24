package cn.org.chenxh.service;

import cn.org.chenxh.entity.Logs;

import java.util.List;

public interface LogsServers {
    public Logs saveLogs(Logs Logs);
    public List<Logs> findLogsAll();
    public Logs findByUserid(String id);
    public  List<Logs> findByUname(String name);
}
