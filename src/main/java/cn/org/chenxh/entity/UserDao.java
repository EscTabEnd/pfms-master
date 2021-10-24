package cn.org.chenxh.entity;

import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserDao extends JpaRepository<User,Long> {
    @Override
    List<User> findAll();

    @Override
    Optional<User> findById(Long aLong);

    @Override
    <S extends User> S save(S s);

    @Override
    <S extends User> Optional<S> findOne(Example<S> example);

    //相对于名字相等查询，参数为name
    User findByUname(String uname);

    User findByUnameAndPwd(String uname,String pwd);

    User findById(Integer integer);

    List<User> findByDid(Integer integer);

}
