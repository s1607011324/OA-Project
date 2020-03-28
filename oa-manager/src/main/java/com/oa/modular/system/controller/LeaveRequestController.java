package com.oa.modular.system.controller;


import cn.hutool.core.bean.BeanUtil;
import cn.stylefeng.roses.core.base.controller.BaseController;
import cn.stylefeng.roses.core.reqres.response.ResponseData;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.core.common.constant.factory.ConstantFactory;
import com.oa.core.common.page.LayuiPageFactory;
import com.oa.modular.system.shiro.ShiroKit;
import com.oa.modular.system.shiro.ShiroUser;
import com.oa.modular.system.entity.*;
import com.oa.modular.system.service.*;
import com.oa.modular.system.warpper.LeaveRequestWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/leave")
public class LeaveRequestController extends BaseController {

    private static String PREFIX = "/modular/system/leave";

    @Autowired
    private LeaveRequestService leaveRequestService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private ActivitiService activitiService;

    /**
     * 跳转到请假主页
     * @param id
     * @return
     */
    @RequestMapping("/home/{id}")
    public String index(@PathVariable("id") String id){
        if (id.equals("1")){
            System.out.println("跳转已审批主页");
            return PREFIX + "/leave-1.html";
        }
        if (id.equals("2")){
            System.out.println("跳转已结束主页");
            return PREFIX + "/leave-2.html";
        }
        System.out.println("跳转未审批主页");
        return PREFIX + "/leave.html";
    }
    /**
     *
     * 获取请假列表信息
     * @return
     */
    @RequestMapping(value = "/list/{status}")
    @ResponseBody
    public Object list(String startDate, String endDate, @PathVariable String status) {
        //获取当前用户角色列表
        ShiroUser user = ShiroKit.getUserNotNull();
        Page<Map<String, Object>> leaves = leaveRequestService.selectLeave(user.getId().toString(),status,startDate,endDate);
        Page<Map<String, Object>> wrap = new LeaveRequestWrapper(leaves).wrap();
        return LayuiPageFactory.createPageInfo(wrap);
    }

    /**
     * 跳转到添加页
     * @param model
     * @return
     */
    @RequestMapping(value = "/leave_add")
    public Object LeaveAdd(Model model){
        //获取当前用户角色列表
        ShiroUser user = ShiroKit.getUserNotNull();
        model.addAttribute("u",user);
        return PREFIX +"/leave_add.html";
    }

    /**
     * 添加请假信息
     * @param leaveRequest
     * @return
     */
    @RequestMapping(value = "/add/{flag}")
    @ResponseBody
    public ResponseData add(HttpServletRequest request, @PathVariable String flag, @RequestParam(value = "file", required = false) MultipartFile[] file, LeaveRequest leaveRequest) throws Exception {
        if ("-1".equals(flag)){
            FileInfo fileInfo = (FileInfo) this.leaveRequestService.uploadsFiles(request,file);
            return ResponseData.success(0,"上传成功",fileInfo);
        }else if ("-2".equals(flag)) {
            this.leaveRequestService.addLeave(request,flag,leaveRequest);
        }
        return ResponseData.error("系统错误！");
    }

    /**
     * 请假申报
     */
    @RequestMapping("/leaveApproval/{id}")
    @ResponseBody
    public ResponseData LeaveApproval(@PathVariable("id") Long leaveId,String status){
        //修改状态
        this.leaveRequestService.editLeaveStatus(leaveId, StatusEnum.started.getValue().toString());
        //储存变量
        LeaveRequest leaveRequest = this.leaveRequestService.getLeave(leaveId);
        Map<String,Object> va = new HashMap<>();
        va.put("immediateLeader", this.roleService.selectDeptNameRolePName());
        va.put("personnel",VariableEnum.person.getPersonnel());
        va.put(VariableEnum.model.toString(),leaveRequest);
        va.put(VariableEnum.initiator.toString(), ConstantFactory.me().getUserNameById(leaveRequest.getUid()));
        //启动流程
        this.activitiService.start("LeaveRequest.bpmn20.xml",LeaveRequest.class.getName(),va);
        return SUCCESS_TIP;
    }

    /**
     * 跳转到请假信息
     *
     * @return
     */
    @RequestMapping("/leaveInfos")
    public Object Leave_info() {
        return PREFIX +"/leaveSelect.html";
    }
    /**
     * 跳转到修改的请假信息
     *
     * @return
     */
    @RequestMapping("/leaveEdit")
    public Object Leave_edit() {
        return PREFIX +"/leaveEdit.html";
    }

    /**
     * 查询请假信息
     * @param leaveId
     * @param model
     * @return
     */
    @RequestMapping("/leave_get/{leaveId}")
    @ResponseBody
    public Object LeaveSelect(@PathVariable("leaveId") Long leaveId,Model model){
        LeaveRequest leaveRequest = this.leaveRequestService.getLeave(leaveId);
        Map<String, Object> leaveMap = BeanUtil.beanToMap(leaveRequest);
        leaveMap.put("proposer", ConstantFactory.me().getUserNameById(leaveRequest.getUid()));
        return ResponseData.success(leaveMap);
    }

    /**
     * 删除请假信息
     * @param id
     * @return
     */
    @RequestMapping("/del/{id}")
    @ResponseBody
    public ResponseData LeaveDel(@PathVariable("id") Long id){
        leaveRequestService.del(id);
        return SUCCESS_TIP;
    }

    /**
     * 修改请假信息
     * @param leaveRequest
     * @return
     */
    @RequestMapping("/edit")
    @ResponseBody
    public Object edit(LeaveRequest leaveRequest) {
        leaveRequest.setNote(leaveRequest.getNote().replaceAll("& lt;", "<").replaceAll("& gt;", ">").replaceAll("&nbsp;"," "));
        leaveRequestService.editLeave(leaveRequest);
        return SUCCESS_TIP;
    }


}
