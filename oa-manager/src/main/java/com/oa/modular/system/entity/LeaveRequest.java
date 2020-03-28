package com.oa.modular.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sun.org.apache.xerces.internal.xs.StringList;

import javax.validation.constraints.Negative;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@TableName("sys_leave_request")
public class LeaveRequest implements Serializable {

    /*编号*/
    //@TableId(type = IdType.AUTO)
    private Long id;
    /*标题*/
    private String title;
    /*开始时间*/
    @TableField(value = "startdate")
    private Date startDate;
    /*结束时间*/
    @TableField(value = "enddate")
    private Date endDate;
    /*请假天数*/
    private String dates;
    /*请假备注信息*/
    private String note;
    /*用户编号*/
    private Long uid;
    /*请假状态*/
    private String status;
    /*临时变量--任务名称*/
    @TableField(exist = false)
    private String TaskName;
    /*领导审批*/
    @TableField(exist = false)
    private String flow;
    /*多个领导审批意见*/
    @TableField(exist = false)
    private List approval_opinions;
    /*单个领导审批意见*/
    @TableField(exist = false)
    private String approval_opinion;
    /*存放附件的集合*/
    @TableField(exist = false)
    private List fileImages;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getDates() {
        return dates;
    }

    public void setDates(String dates) {
        this.dates = dates;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTaskName() {
        return TaskName;
    }

    public void setTaskName(String taskName) {
        TaskName = taskName;
    }

    public String getFlow() {
        return flow;
    }

    public void setFlow(String flow) {
        this.flow = flow;
    }

    public List getApproval_opinions() {
        return approval_opinions;
    }
    public String getApproval_opinion() {
        return approval_opinion;
    }

    public void setApproval_opinions(List approval_opinions) {
        this.approval_opinions = approval_opinions;
    }
    public void setApproval_opinion(String approval_opinion) {
        this.approval_opinion = approval_opinion;
    }

    public List getFileImages() {
        return fileImages;
    }

    public void setFileImages(List fileImages) {
        this.fileImages = fileImages;
    }
}
