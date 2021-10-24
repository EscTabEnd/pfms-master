package cn.org.chenxh.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * @description 人员
 * @author chenxh
 * @date 2019-1-21
 */
@Entity
@Table(name="user")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    /**
     * 账户
     */
    @Column
    private String uname;

    /**
     * 密码
     */
    @Column
    private String pwd;

    /**
     * 等级
     * */
    @Column
    private String rank;

    /**
     * 状态
     * */
    @Column
    private String status;


    @Column
    private int did;
    @Column
    private String dname;

    public User(){

    }
    public User(String uname, String pwd) {
        this.uname = uname;
        this.pwd = pwd;
    }
    public User(String uname, String pwd,String rank) {
        this.uname = uname;
        this.pwd = pwd;
        this.rank = rank;
    }
    public User(String uname, String pwd,String rank,String status) {
        this.uname = uname;
        this.pwd = pwd;
        this.rank = rank;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getDid() {
        return did;
    }

    public void setDid(int did) {
        this.did = did;
    }

    public String getDname() {
        return dname;
    }

    public void setDname(String dname) {
        this.dname = dname;
    }
}
