package cn.org.chenxh.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentDao extends JpaRepository<Department,Long> {
    @Override
    List<Department> findAll();

    @Override
    Optional<Department> findById(Long aLong);


    @Override
    <S extends Department> S save(S s);

    Department findByPid(Integer pid);
    Department findByDname(String dname);
    Department findById(Integer aLong);
}
