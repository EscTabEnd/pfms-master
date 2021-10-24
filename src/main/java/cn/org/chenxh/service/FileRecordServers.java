package cn.org.chenxh.service;

import cn.org.chenxh.entity.FileRecord;
import cn.org.chenxh.entity.Logs;

import java.util.List;
import java.util.Optional;

public interface FileRecordServers {
    public List<FileRecord> findAll();
    public FileRecord save(FileRecord f);
    public FileRecord  findByCurname(String curname);
    public List<FileRecord>  findByUrlLike(String url);
    public FileRecord  findByCurnameAndUname(String curname,String uname);
    public FileRecord  findByCurnameAndUnameAndUrlLike(String curname,String uname,String url);
    public List<FileRecord>  findByCurnameAndUrlLike(String curname,String url);
    public FileRecord  findByUname(String uname);
    public List<FileRecord>  findByCurnameLike(String curname);
    public List<FileRecord>  findByUnameAndCurnameLike(String uname,String curname);
    public FileRecord  findById(Integer id);
}
