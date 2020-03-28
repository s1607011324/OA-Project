package com.oa.modular.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("sys_wxinfo")
public class WXInfo {
    //@TableId(type = IdType.AUTO)
    private String openid;
    private String nickname;
    private Double sex;
    //@TableField(value = "headimgurl")
    private String headimgurl;
    private String country;
    private String province;
    private String city;
    private String eventkey;
    private Long binduserid;

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Double getSex() {
        return sex;
    }

    public void setSex(Double sex) {
        this.sex = sex;
    }

    public String getHeadimgurl() {
        return headimgurl;
    }

    public void setHeadimgurl(String headimgurl) {
        this.headimgurl = headimgurl;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getEventkey() {
        return eventkey;
    }

    public void setEventkey(String eventkey) {
        this.eventkey = eventkey;
    }

    public Long getBinduserid() {
        return binduserid;
    }

    public void setBinduserid(Long binduserid) {
        this.binduserid = binduserid;
    }

    @Override
    public String toString() {
        return "WXInfo{" +
                "openid=" + openid +
                ", nickname='" + nickname + '\'' +
                ", sex=" + sex +
                ", headimgurl='" + headimgurl + '\'' +
                ", country='" + country + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", eventkey='" + eventkey + '\'' +
                ", binduserid='" + binduserid + '\'' +
                '}';
    }
}
