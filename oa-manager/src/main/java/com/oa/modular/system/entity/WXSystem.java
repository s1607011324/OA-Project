package com.oa.modular.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.sql.Time;
import java.util.Date;

@TableName(value = "sys_wxsystem")
public class WXSystem {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String pwd;
    private long allow;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public long getAllow() {
        return allow;
    }

    public void setAllow(long allow) {
        this.allow = allow;
    }
}
