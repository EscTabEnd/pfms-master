package cn.org.chenxh.service.impl;

import cn.org.chenxh.entity.Department;
import cn.org.chenxh.entity.DepartmentDao;
import cn.org.chenxh.entity.Logs;
import cn.org.chenxh.entity.LogsDao;
import cn.org.chenxh.service.DepartServers;
import cn.org.chenxh.service.LogsServers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("departServers")
public class DepartServersImpl implements DepartServers {
    @Autowired
    private DepartmentDao depart;


    @Override
    public Department saveDepartment(Department department) {
        return depart.save(department);
    }

    @Override
    public List<Department> findAll() {
        return depart.findAll();
    }

    @Override
    public Department findByPid(Integer id) {
        return depart.findByPid(id);
    }

    @Override
    public Department findById(Integer id) {
        return depart.findById(id);
    }

    @Override
    public Department findByDname(String name) {
        return depart.findByDname(name);
    }
}
