package com.oa.modular.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.modular.system.entity.FileInfo;
import com.oa.modular.system.entity.LeaveRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface LeaveRequestMapper extends BaseMapper<LeaveRequest> {
    /*
    按状态
    或开始时间
    或结束时间
    或申请人查询请假*/
    Page<Map<String, Object>> selectLeaveByCon(@Param("page") Page page, @Param("uid") String uid, @Param("status") String status, @Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 修改请假状态
     */
    int setStatus(@Param("leaveId") Long leaveId, @Param("status") String status);

}
