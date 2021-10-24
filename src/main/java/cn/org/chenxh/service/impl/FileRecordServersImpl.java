package cn.org.chenxh.service.impl;

import cn.org.chenxh.entity.Department;
import cn.org.chenxh.entity.DepartmentDao;
import cn.org.chenxh.entity.FileRecord;
import cn.org.chenxh.entity.FileRecordDao;
import cn.org.chenxh.service.DepartServers;
import cn.org.chenxh.service.FileRecordServers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("fileRecordServers")
public class FileRecordServersImpl implements FileRecordServers {
    @Autowired
    private FileRecordDao fileRecord;

    @Override
    public List<FileRecord> findAll() {
        return fileRecord.findAll();
    }

    @Override
    public FileRecord save(FileRecord f) {
        return fileRecord.save(f);
    }

    @Override
    public FileRecord findByCurname(String curname) {
        return fileRecord.findByCurname(curname);
    }

    @Override
    public List<FileRecord> findByUrlLike(String url) {
        return fileRecord.findByUrlLike("%"+url+"%");
    }

    @Override
    public FileRecord findByCurnameAndUname(String curname, String uname) {
        return fileRecord.findByCurnameAndUname(curname,uname);
    }

    @Override
    public FileRecord findByCurnameAndUnameAndUrlLike(String curname, String uname, String url) {
        return fileRecord.findByCurnameAndUnameAndUrlLike(curname,uname,"%"+url);
    }

    @Override
    public List<FileRecord> findByCurnameAndUrlLike(String curname, String url) {
        return fileRecord.findByCurnameAndUrlLike(curname,"%"+url+"%");
    }

    @Override
    public FileRecord findByUname(String uname) {
        return fileRecord.findByUname(uname);
    }

    @Override
    public List<FileRecord> findByCurnameLike(String curname) {
        return fileRecord.findByCurnameLike("%"+curname+"%");
    }

    @Override
    public List<FileRecord> findByUnameAndCurnameLike(String uname, String curname) {
        return fileRecord.findByUnameAndCurnameLike(uname,"%"+curname+"%");
    }

    @Override
    public FileRecord findById(Integer id) {
        return fileRecord.findById(id);
    }
}
