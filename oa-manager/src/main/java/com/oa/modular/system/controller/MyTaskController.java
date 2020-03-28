package com.oa.modular.system.controller;


import cn.hutool.core.bean.BeanUtil;
import cn.stylefeng.roses.core.base.controller.BaseController;
import cn.stylefeng.roses.core.reqres.response.ResponseData;
import com.oa.core.common.constant.factory.ConstantFactory;
import com.oa.core.log.LogObjectHolder;
import com.oa.modular.system.entity.LeaveRequest;
import com.oa.modular.system.service.ActivitiService;
import com.oa.modular.system.service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.OutputStream;
import java.util.Map;

@Controller
@RequestMapping("/task")
public class MyTaskController extends BaseController {

    private static String PREFIX = "/modular/system/task";

    @Autowired
    private LeaveRequestService leaveRequestService;

    @Autowired
    private ActivitiService activitiService;

    @RequestMapping("")
    public String index() {
        return PREFIX + "/Task.html";
    }

    /**
     * 查询代办任务
     *
     * @return
     */
    @RequestMapping("/commission")
    @ResponseBody
    public Object commission(HttpServletRequest request) {
        //每页多少条数据
        int limit = Integer.valueOf(request.getParameter("limit"));

        //第几页
        int page = Integer.valueOf(request.getParameter("page"));


        return activitiService.getCommissions(page, limit);
    }

    /**
     * 获取实时流程图
     *
     * @param processInstanceId
     * @param outputStream
     */
    @RequestMapping("/flowImg/{processInstanceId}")
    public void getImg(@PathVariable("processInstanceId") String processInstanceId, OutputStream outputStream) {
        activitiService.getFlowImgByInstanceId(processInstanceId, outputStream);
    }

    /**
     * 跳转到执行任务页面
     *
     * @return
     */
    @RequestMapping(value = "/task_execute")
    public Object task_execute() {
        return PREFIX + "/task_execute.html";
    }


    /**
     * 回显请假信息
     *
     * @param taskId
     * @return
     */
    @RequestMapping("/getTask_execute/{taskId}/{processInstanceId}")
    @ResponseBody
    public Object getTask_execute(@PathVariable("taskId") String taskId, @PathVariable("processInstanceId") String processInstanceId) {
        LeaveRequest leaveRequest = activitiService.TaskExecute(processInstanceId, taskId);
        Map<String, Object> leaveMap = BeanUtil.beanToMap(leaveRequest);
        leaveMap.put("taskId", taskId);
        leaveMap.put("proposer", ConstantFactory.me().getUserNameById(leaveRequest.getUid()));
        return ResponseData.success(leaveMap);
    }

    /**
     * 执行完成任务
     *
     * @param leaveRequest
     * @param taskId
     * @return
     */
    @RequestMapping("/execute/{taskId}")
    @ResponseBody
    public ResponseData execute(LeaveRequest leaveRequest, @PathVariable String taskId) throws Exception {
        leaveRequest.setNote(leaveRequest.getNote().replaceAll("& lt;", "<").replaceAll("& gt;", ">").replaceAll("&nbsp;"," "));
        leaveRequest.setApproval_opinion(leaveRequest.getApproval_opinion().replaceAll("& lt;", "<").replaceAll("& gt;", ">").replaceAll("&nbsp;"," "));
        activitiService.doCompleteTask(taskId, leaveRequest);
        return SUCCESS_TIP;
    }
}
