package cn.org.chenxh.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileRecordDao extends JpaRepository<FileRecord ,Long> {
    @Override
    List<FileRecord > findAll();

    @Override
    Optional<FileRecord > findById(Long id);


    @Override
    <S extends FileRecord > S save(S s);

    public FileRecord  findByCurname(String curname);
    public FileRecord  findByUname(String uname);
    public List<FileRecord>  findByCurnameLike(String curname);
    public List<FileRecord>  findByUrlLike(String url);
    public List<FileRecord>  findByUnameAndCurnameLike(String uname,String curname);
    public FileRecord  findById(Integer id);
    public FileRecord  findByCurnameAndUname(String curname,String uname);
    public FileRecord  findByCurnameAndUnameAndUrlLike(String curname,String uname,String url);
    public List<FileRecord>  findByCurnameAndUrlLike(String curname,String url);

}
