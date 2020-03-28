package com.oa.modular.system.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.core.common.page.LayuiPageFactory;
import com.oa.core.util.CountDataTime;
import com.oa.modular.system.entity.FileInfo;
import com.oa.modular.system.entity.LeaveRequest;
import com.oa.modular.system.mapper.LeaveRequestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class LeaveRequestService extends ServiceImpl<LeaveRequestMapper, LeaveRequest> {

    private long leaveID = IdWorker.getId();

    @Autowired
    private FileInfoService fileInfoService;

    public Page<Map<String, Object>> selectLeave(String uid,String status,String startDate,String endDate){
        Page page = LayuiPageFactory.defaultPage();
        return this.baseMapper.selectLeaveByCon(page,uid,status,startDate,endDate);
    }

    /**
     * 请假申请
     * @param leaveRequest
     */
    public void addLeave(HttpServletRequest request, String flag, LeaveRequest leaveRequest) throws Exception {
        Long leaveId = (Long) request.getSession().getAttribute("leaveId");
        String dates = CountDataTime.getDatePoor(leaveRequest.getStartDate(),leaveRequest.getEndDate());
        System.out.println(dates);
        leaveRequest.setId(leaveId);
        leaveRequest.setDates(dates);
        leaveRequest.setNote(leaveRequest.getNote().replaceAll("& lt;", "<").replaceAll("& gt;", ">").replaceAll("&nbsp;"," "));
        this.save(leaveRequest);
        request.getSession().setAttribute("type","1");
        request.getSession().removeAttribute("leaveId");
    }


    /**
     * 文件批量上传
     * @param request
     * @param file
     * @return
     * @throws Exception
     */
    public FileInfo uploadsFiles(HttpServletRequest request, MultipartFile[] file) throws Exception {
        String type = (String) request.getSession().getAttribute("type");
        if (type!=null){
            leaveID = IdWorker.getId();
        }
        String FileID = "";
        File upload = fileInfoService.uploadFile();
        if (upload != null) {
            FileInfo fileInfo = new FileInfo();
            String TimeStamp = IdWorker.getIdStr();
            FileID = IdWorker.getIdStr();
            for (MultipartFile multipartFile : file) {
                String avatar = upload.getAbsolutePath() + "//" + TimeStamp + multipartFile.getOriginalFilename();
                //保存附件文件
                fileInfo.setFileId(FileID);
                fileInfo.setLeaveId(leaveID);
                fileInfo.setFileData("/assets/common/upload/" + TimeStamp + multipartFile.getOriginalFilename());
                fileInfo.setFileName(TimeStamp + multipartFile.getOriginalFilename());
                // 转存文件
                multipartFile.transferTo(new File(avatar));
                fileInfo.setCreateTime(new Date());
                fileInfoService.save(fileInfo);
            }
        }
        request.getSession().setAttribute("leaveId",leaveID);
        request.getSession().removeAttribute("type");
        return fileInfoService.getById(FileID);
    }

        /**
         * 修改状态
         * @param leaveId
         * @param status
         */
        public void editLeaveStatus (Long leaveId, String status){
            this.baseMapper.setStatus(leaveId, status);
        }

        /**
         * 修改请假信息
         * @param leaveRequest
         */
        public void editLeave (LeaveRequest leaveRequest){
            String dates = CountDataTime.getDatePoor(leaveRequest.getStartDate(), leaveRequest.getEndDate());
            System.out.println(dates);
            leaveRequest.setDates(dates);
            this.baseMapper.updateById(leaveRequest);
        }

        /*
         获取请假信息
         */
        public LeaveRequest getLeave (Long LeaveId){
            return this.baseMapper.selectById(LeaveId);
        }

        /**
         * 删除
         * @param id 编号
         */
        public void del (Long id){
            //根据id查询文件表的图片有多少个
            QueryWrapper<FileInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("LEAVE_ID",id.toString());
            List<FileInfo> fileInfos = fileInfoService.selectFileInfo(queryWrapper);
            //循环删除
            for (FileInfo fileInfo : fileInfos) {
                this.fileInfoService.delFile(fileInfo.getFileId(),fileInfo.getFileName());
            }
            this.baseMapper.deleteById(id);
        }
}
