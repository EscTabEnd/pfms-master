package cn.org.chenxh.service;

import cn.org.chenxh.entity.Department;

import java.util.List;

public interface DepartServers {
    public Department saveDepartment(Department Department);
    public List<Department> findAll();
    public Department findByPid(Integer id);
    public Department findById(Integer id);
    public Department findByDname(String id);
}
