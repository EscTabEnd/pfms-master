package cn.org.chenxh.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * @description 日志表
 * @author chenxh
 * @date 2019-1-21
 */
@Entity
@Table(name="logs")
@Data
public class Logs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    String userid;
    String uname;

    String log;

    String time;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }
}
