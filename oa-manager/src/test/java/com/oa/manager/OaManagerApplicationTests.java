package com.oa.manager;

import com.oa.core.common.node.MenuNode;
import com.oa.modular.system.shiro.ShiroUser;
import com.oa.modular.system.shiro.service.UserAuthService;
import com.oa.modular.system.entity.User;
import com.oa.modular.system.entity.WXInfo;
import com.oa.modular.system.service.FileInfoService;
import com.oa.modular.system.service.UserService;
import com.oa.modular.system.service.WxInfoService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


//@RunWith(SpringRunner.class)
//@SpringBootTest
class OaManagerApplicationTests {

	//@Test
	public void test1(){
		Date d = new Date(2020,1,22);
		Date d1 = new Date(2020,2,10);

		getDatePoor(d1,d);

	}

	//@Test
	public void getDatePoor(Date endDate, Date nowDate) {
		long nd = 1000 * 24 * 60 * 60;
		long nh = 1000 * 60 * 60;
		long nm = 1000 * 60;
		// long ns = 1000;
		// 获得两个时间的毫秒时间差异
		long diff = endDate.getTime() - nowDate.getTime();
		// 计算差多少天
		long day = diff / nd;
		// 计算差多少小时
		long hour = diff % nd / nh;
		// 计算差多少分钟
		long min = diff % nd % nh / nm;
		// 计算差多少秒//输出结果
		// long sec = diff % nd % nh % nm / ns;

		System.out.println(day + "天" + hour + "小时" + min + "分钟");

	}
	@Autowired
	private RepositoryService repositoryService;

	@Autowired
	private RuntimeService runtimeService;

	@Autowired
	private TaskService taskService;

	@Autowired
	private FileInfoService fileInfoService;

	//@Test
	public void contextLoads() {

        //发布流程定义
		repositoryService.createDeployment().addClasspathResource("Test.bpmn20.xml").deploy();

		//启动流程实例
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("com.oa.modular.system.entity.LeaveRequest");

		//查询任务->User Task1
		Task task =  taskService.createTaskQuery().singleResult();
		System.out.println(task.getName());
		List<Task> taskList = taskService.createTaskQuery().taskAssignee("部门经理").list();
		TaskQuery query = taskService.createTaskQuery().taskCandidateOrAssigned("admin");

       List<Task> list = query.listPage((1 - 1) * 20, 20);
		for (Task t: list) {
			System.out.println(t.getName());
		}

		//完成任务
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("flow", "同意");
		taskService.complete(task.getId(),variables);

		//查询任务->User Task2
		task =  taskService.createTaskQuery().singleResult();
		System.out.println(task.getName());

		//完成任务
		taskService.complete(task.getId());

		//查询任务->User Task3
		task =  taskService.createTaskQuery().singleResult();
		System.out.println(task.getName());

	}

	//@Test
	public void test2(){
		/*List<FileInfo> list = fileInfoService.getFiles("1215972255109033985");
		for (FileInfo fileInfo : list) {
			System.out.println(fileInfo);
		}*/
	}

	@Resource
	private WxInfoService wxInfoService;

	@Resource
	private UserService userService;

	@Autowired
	private UserAuthService userAuthService;

	//@Test
	public void test3(){
		/*//List<WXInfo> wxInfos = wxInfoService.selectList("lrfun.com.1584676276727");
		List<WXInfo> wxInfos = wxInfoService.selectHeadImgUrl("http://thirdwx.qlogo.cn/mmopen/ajNVdqHZLLBhWrYf9icRyAYzZHoH81ryrAfSCicFITOxMgLwDglZLkF2nscKEBkbe02cmaBqSN1kaxld4DfYRje0QZTcDM5vKl92Qlqkno2yU/132");
		for (WXInfo wxInfo : wxInfos) {
			System.out.println(wxInfo);
		}*/
// algorithmName 算法名称
        /*String algorithmName="md5";//sha1,sha-256,sha-512。。。，下面的第一个参数
        String encryptStr = new SimpleHash("md5", "199951", "0ywvc", 1024).toBase64();
        String sha256 = new SimpleHash("sha-256", "4a44a60c737439adcceee7db2b5f0083", "0ywvc", 1024).toBase64();
        String sha512= new SimpleHash("sha-512", "4a44a60c737439adcceee7db2b5f0083", "0ywvc", 1024).toBase64();

        System.out.println(encryptStr);
        System.out.println(sha256);
        System.out.println(sha512);*/
		//System.out.println(ShiroKit.md5("199951","0ywvc"));
       /* String encodeToString = Hex.encodeToString("199951".getBytes());
        String decodeToString = new String(Hex.decode(encodeToString));

        System.out.println("加密："+encodeToString);
        System.out.println("解密："+decodeToString);*/
		WXInfo wxInfo = wxInfoService.selectList("lrfun.com.1584690280375");
		User user = this.userService.getById(wxInfo.getBinduserid());
		ShiroUser shiroUser = userAuthService.shiroUser(user);
		List<Long> roleList = shiroUser.getRoleList();
		List<MenuNode> menus = userService.getUserMenuNodes(roleList);
	}

}
