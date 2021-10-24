package cn.org.chenxh.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * @description 部门
 * @author chenxh
 * @date 2019-1-21
 */
@Entity
@Table(name="depart")
@Data
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    private String dname;

    private int pid;

    private String url;

    int nop;//Number of people 人数


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDname() {
        return dname;
    }

    public void setDname(String dname) {
        this.dname = dname;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getNop() {
        return nop;
    }

    public void setNop(int nop) {
        this.nop = nop;
    }

}
