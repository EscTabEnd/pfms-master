package cn.org.chenxh.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name="filerecord")
@Data
public class FileRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column
    private String orgname; //初始名称
    @Column
    private String curname;//当前名称
    @Column
    private String uname;//上传人员
    @Column
    private String url;//文件路径
   @Column
    private Long length;//文件大小
    @Column
    private String size;//文件大小  带单
    @Column
    private String type;//文件类型，是文件夹还是文件
    @Column
    private String time;//上传时间

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrgname() {
        return orgname;
    }

    public void setOrgname(String orgname) {
        this.orgname = orgname;
    }

    public String getCurname() {
        return curname;
    }

    public void setCurname(String curname) {
        this.curname = curname;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
