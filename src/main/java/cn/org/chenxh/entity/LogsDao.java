package cn.org.chenxh.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LogsDao extends JpaRepository<Logs,Long> {
    @Override
    List<Logs> findAll();

    @Override
    Optional<Logs> findById(Long aLong);

    @Override
    <S extends Logs> S save(S s);

    Logs findByUserid(String userid);

    List<Logs> findByUname(String uname);

}
