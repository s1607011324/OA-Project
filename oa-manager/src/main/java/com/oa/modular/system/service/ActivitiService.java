package com.oa.modular.system.service;

import com.oa.core.common.constant.factory.ConstantFactory;
import com.oa.core.common.page.LayuiPageInfo;
import com.oa.modular.system.shiro.ShiroKit;
import com.oa.modular.system.shiro.ShiroUser;
import com.oa.modular.system.diagram.CustomProcessDiagramGeneratorImpl;
import com.oa.modular.system.entity.LeaveRequest;
import com.oa.modular.system.entity.StatusEnum;
import com.oa.modular.system.entity.TaskBean;
import com.oa.modular.system.entity.VariableEnum;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.image.ProcessDiagramGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ActivitiService {
    private final static Logger logger = LoggerFactory.getLogger(ActivitiService.class);

    @Autowired
    private LeaveRequestService leaveRequestService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private HistoryService historyService;

    /**
     * start activiti process
     *
     * @return instance id
     */
    public Object start(String classpath, String key, Map<String, Object> variables) {
        //发布流程定义
        repositoryService.createDeployment().addClasspathResource(classpath).deploy();
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(key, variables);
        return instance.getId();
    }


    /**
     * get user task list
     *
     * @param uid
     * @return user task list
     */
    public List<Task> getTask(String uid) {
        return taskService.createTaskQuery().taskAssignee(uid).list();
    }


    /**
     * 根据流程实例Id,获取实时流程图片
     *
     * @param processInstanceId
     * @param outputStream
     * @return
     */
    public void getFlowImgByInstanceId(String processInstanceId, OutputStream outputStream) {
        try {
            if (StringUtils.isEmpty(processInstanceId)) {
                logger.error("processInstanceId is null");
                return;
            }
            // 获取历史流程实例
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            // 获取流程中已经执行的节点，按照执行先后顺序排序
            List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId)
                    .orderByHistoricActivityInstanceId().asc().list();
            // 高亮已经执行流程节点ID集合
            List<String> highLightedActivitiIds = new ArrayList<>();
            for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
                highLightedActivitiIds.add(historicActivityInstance.getActivityId());
            }

            List<HistoricProcessInstance> historicFinishedProcessInstances = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).finished()
                    .list();
            ProcessDiagramGenerator processDiagramGenerator = null;
            // 如果还没完成，流程图高亮颜色为绿色，如果已经完成为红色
            if (!CollectionUtils.isEmpty(historicFinishedProcessInstances)) {
                // 如果不为空，说明已经完成
                //processDiagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
            } else {
                processDiagramGenerator = new CustomProcessDiagramGeneratorImpl();
            }

            BpmnModel bpmnModel = repositoryService.getBpmnModel(historicProcessInstance.getProcessDefinitionId());
            // 高亮流程已发生流转的线id集合
            List<String> highLightedFlowIds = getHighLightedFlows(bpmnModel, historicActivityInstances);

            // 使用默认配置获得流程图表生成器，并生成追踪图片字符流
            InputStream imageStream = processDiagramGenerator.generateDiagram(bpmnModel, "png", highLightedActivitiIds, highLightedFlowIds, "宋体", "微软雅黑", "黑体", null, 2.0);

            // 输出图片内容
            byte[] b = new byte[1024];
            int len;
            while ((len = imageStream.read(b, 0, 1024)) != -1) {
                outputStream.write(b, 0, len);
            }
        } catch (Exception e) {
            logger.error("processInstanceId" + processInstanceId + "生成流程图失败，原因：" + e.getMessage(), e);
        }

    }

    /**
     * 获取已经流转的线
     *
     * @param bpmnModel
     * @param historicActivityInstances
     * @return
     */
    private List<String> getHighLightedFlows(BpmnModel bpmnModel, List<HistoricActivityInstance> historicActivityInstances) {
        // 高亮流程已发生流转的线id集合
        List<String> highLightedFlowIds = new ArrayList<>();
        // 全部活动节点
        List<FlowNode> historicActivityNodes = new ArrayList<>();
        // 已完成的历史活动节点
        List<HistoricActivityInstance> finishedActivityInstances = new ArrayList<>();

        for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
            FlowNode flowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstance.getActivityId(), true);
            historicActivityNodes.add(flowNode);
            if (historicActivityInstance.getEndTime() != null) {
                finishedActivityInstances.add(historicActivityInstance);
            }
        }

        FlowNode currentFlowNode = null;
        FlowNode targetFlowNode = null;
        // 遍历已完成的活动实例，从每个实例的outgoingFlows中找到已执行的
        for (HistoricActivityInstance currentActivityInstance : finishedActivityInstances) {
            // 获得当前活动对应的节点信息及outgoingFlows信息
            currentFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(currentActivityInstance.getActivityId(), true);
            List<SequenceFlow> sequenceFlows = currentFlowNode.getOutgoingFlows();

            /**
             * 遍历outgoingFlows并找到已已流转的 满足如下条件认为已已流转： 1.当前节点是并行网关或兼容网关，则通过outgoingFlows能够在历史活动中找到的全部节点均为已流转 2.当前节点是以上两种类型之外的，通过outgoingFlows查找到的时间最早的流转节点视为有效流转
             */
            if ("parallelGateway".equals(currentActivityInstance.getActivityType()) || "inclusiveGateway".equals(currentActivityInstance.getActivityType())) {
                // 遍历历史活动节点，找到匹配流程目标节点的
                for (SequenceFlow sequenceFlow : sequenceFlows) {
                    targetFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(sequenceFlow.getTargetRef(), true);
                    if (historicActivityNodes.contains(targetFlowNode)) {
                        highLightedFlowIds.add(targetFlowNode.getId());
                    }
                }
            } else {
                List<Map<String, Object>> tempMapList = new ArrayList<>();
                for (SequenceFlow sequenceFlow : sequenceFlows) {
                    for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
                        if (historicActivityInstance.getActivityId().equals(sequenceFlow.getTargetRef())) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("highLightedFlowId", sequenceFlow.getId());
                            map.put("highLightedFlowStartTime", historicActivityInstance.getStartTime().getTime());
                            tempMapList.add(map);
                        }
                    }
                }

                if (!CollectionUtils.isEmpty(tempMapList)) {
                    // 遍历匹配的集合，取得开始时间最早的一个
                    long earliestStamp = 0L;
                    String highLightedFlowId = null;
                    for (Map<String, Object> map : tempMapList) {
                        long highLightedFlowStartTime = Long.valueOf(map.get("highLightedFlowStartTime").toString());
                        if (earliestStamp == 0 || earliestStamp >= highLightedFlowStartTime) {
                            highLightedFlowId = map.get("highLightedFlowId").toString();
                            earliestStamp = highLightedFlowStartTime;
                        }
                    }

                    highLightedFlowIds.add(highLightedFlowId);
                }

            }

        }
        return highLightedFlowIds;
    }


    /**
     * 获取代办任务信息
     *
     * @param page
     * @param limit
     * @return
     */
    public LayuiPageInfo getCommissions(Integer page, Integer limit) {
        ShiroUser user = ShiroKit.getUser();
        List<TaskBean> tasks = new ArrayList<>();
        LayuiPageInfo result = null;
        if (user != null) {
            List uid = user.getRoleNames();
            String ass = "";
            for (Object o : uid) {
                if (o.toString().equals(VariableEnum.DS.getPersonnel())||o.toString().equals(VariableEnum.Sum.getPersonnel())||o.toString().equals(VariableEnum.FSum.getPersonnel())){
                    ass = o.toString();
                }else {
                    ass = user.getDeptName() + o.toString();
                }
                TaskQuery query = taskService.createTaskQuery().taskCandidateOrAssigned(ass);
                List<Task> list = query.listPage((page - 1) * limit, limit);
                if (list.size() == 0) {
                    query = taskService.createTaskQuery().taskCandidateOrAssigned(user.getName());
                    list = query.listPage((page - 1) * limit, limit);
                }
                Long rowCount = query.count();
                for (Task task : list) {
                    if ((user.getDeptName() + o.toString()).equals(task.getAssignee()) ||o.toString().equals(task.getAssignee())|| user.getName().equals(task.getAssignee())) {
                        TaskBean tb = new TaskBean();
                        tb.setId(task.getId());
                        tb.setName(task.getName());
                        tb.setDescription(task.getDescription());
                        tb.setProcessDefinitionId(task.getProcessDefinitionId());
                        tb.setProcessInstanceId(task.getProcessInstanceId());
                        tb.setCreateTime(task.getCreateTime());
                        LeaveRequest leaveRequest = (LeaveRequest) taskService.getVariable(task.getId(), VariableEnum.model.toString());
                        String initiatorName = taskService.getVariable(task.getId(), VariableEnum.initiator.toString()).toString();
                        tb.setInitiator(initiatorName);
                        tb.setTitle(leaveRequest.getTitle());
                        tasks.add(tb);
                    }
                }
                result = new LayuiPageInfo();
                result.setCount(rowCount);
                result.setData(tasks);
            }
        }
        return result;
    }

    /**
     * 查询待办任务的请假信息
     *
     * @param id
     * @return
     */
    public LeaveRequest TaskExecute(String processInstanceId, String id) {
        Task task = taskService.createTaskQuery().taskId(id).singleResult();
        LeaveRequest leaveRequest = null;
        if (task!=null){
            leaveRequest = (LeaveRequest) taskService.getVariable(task.getId(), VariableEnum.model.toString());
            List<Map<String, String>> list = getProcessComments(processInstanceId);
            /*StringBuilder sb = new StringBuilder();
            for (Map<String, String> map : list) {
                sb.append("\n\t\t").append("任务名称：").append(map.get("taskName")).append("\n\t\t").append("领导意见：").append(map.get("comment"));
            }*/
            leaveRequest.setTaskName(task.getName());
            leaveRequest.setApproval_opinions(list);
        }
        return leaveRequest;
    }


    /**
     * 获取附件
     * @param processInstanceId
     * @return
     */
    private List<Map<String, String>> getComments(String processInstanceId) {
        List<Attachment> attachments = taskService
                .getProcessInstanceAttachments(processInstanceId);
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        for (Attachment comment : attachments) {
            Map<String, String> map = new HashMap<String, String>();
            HistoricTaskInstance t = historyService
                    .createHistoricTaskInstanceQuery()
                    .taskId(comment.getTaskId()).singleResult();
            String commentName = t.getName() + "(" + comment.getName() + ")";
            map.put("taskName", commentName);
            map.put("comment", comment.getDescription());
            list.add(map);
        }
        return list;
    }

    /**
     * 获取审批意见
     * @param processInstanceId
     * @return
     */
    public List<Map<String, String>> getProcessComments(String processInstanceId) {
        List<Comment> attachments = taskService
                .getProcessInstanceComments(processInstanceId);
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        for (Comment comment : attachments) {
            Map<String, String> map = new HashMap<String, String>();
            HistoricTaskInstance t = historyService
                    .createHistoricTaskInstanceQuery()
                    .taskId(comment.getTaskId()).singleResult();
            String commentName = t.getName() + "(" + comment.getUserId() + ")";
            map.put("taskName", commentName);
            map.put("comment", comment.getFullMessage());
            list.add(map);
        }
        return list;
    }

    /**
     * 完成任务
     *
     * @return
     */

    public LeaveRequest doCompleteTask(String taskId,LeaveRequest leaveRequest) {
        //leaveRequest.setApproval_opinion(approval_opinion);
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

        //保存审批意见
        if (!StringUtils.isEmpty(leaveRequest.getApproval_opinion())) {
            // 由于流程用户上下文对象是线程独立的，所以要在需要的位置设置，要保证设置和获取操作在同一个线程中
            Authentication.setAuthenticatedUserId(ConstantFactory.me().getUserNameById(leaveRequest.getUid()));//批注人的名称  一定要写，不然查看的时候不知道人物信息
            // 添加批注信息
            taskService.addComment(taskId, task.getProcessInstanceId(), leaveRequest.getApproval_opinion());//comment为批注内容
           // Attachment attachment = taskService.
                    //createAttachment("comment", taskId, task.getProcessInstanceId(), ConstantFactory.me().getUserNameById(leaveRequest.getUid()), leaveRequest.getApproval_opinion(),"");
            //taskService.saveAttachment(attachment);
        }

/*        //修改业务数据
        if (modifyModel) {
            crudService.save(model);

            //重新存入变量
            taskService.setVariable(taskId, VariableEnum.model.toString(),leaveRequest);
        }*/


        //完成任务
        //taskService.claim(taskId,ShiroKit.getUserNotNull().getName());
        if (null != leaveRequest.getFlow() && !"".equals(leaveRequest.getFlow())) {
            Map<String, Object> variables = new HashMap<String, Object>();
            variables.put("flow", leaveRequest.getFlow());
            taskService.complete(task.getId(), variables);
        } else {
            taskService.complete(task.getId());
        }

        //流程结束，修改状态
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
        if (null == processInstance) {
            leaveRequest.setStatus(StatusEnum.finished.getValue().toString());
            leaveRequestService.updateById(leaveRequest);
        }
        return leaveRequest;
    }
}

