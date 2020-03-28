/**
 * Copyright 2018-2020 stylefeng & fengshuonan (https://gitee.com/stylefeng)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.oa.modular.system.controller;

import com.oa.core.common.node.MenuNode;
import com.oa.core.log.LogManager;
import com.oa.core.log.factory.LogTaskFactory;
import com.oa.modular.system.shiro.ShiroKit;
import com.oa.modular.system.shiro.ShiroUser;
import com.oa.modular.system.shiro.service.UserAuthService;
import com.oa.modular.system.entity.User;
import com.oa.modular.system.entity.WXInfo;
import com.oa.modular.system.service.FileInfoService;
import com.oa.modular.system.service.UserService;
import cn.stylefeng.roses.core.base.controller.BaseController;
import com.oa.modular.system.service.WxInfoService;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static cn.stylefeng.roses.core.util.HttpContext.getIp;

/**
 * 登录控制器
 *
 * @author fengshuonan
 * @Date 2017年1月10日 下午8:25:24
 */
@Controller
public class LoginController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserAuthService userAuthService;

    @Resource
    private WxInfoService wxInfoService;

    /**
     * 跳转到主页
     *
     * @author fengshuonan
     * @Date 2018/12/23 5:41 PM
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(Model model, HttpServletRequest request) {
        ShiroUser user = null;
        List<Long> roleList = null;
        String scene_str = (String) request.getSession().getAttribute("scene_str");
        if (scene_str != null && ShiroKit.isGuest()) {
            ShiroKit.getSession().setAttribute("sessionFlag", true);
            WXInfo wxInfo = wxInfoService.selectList(scene_str);
            User users = this.userService.getById(wxInfo.getBinduserid());
            user = userAuthService.shiroUser(users);
            roleList = user.getRoleList();
        } else if (ShiroKit.isAuthenticated() || ShiroKit.getUser() != null) {
            //获取当前用户角色列表
            user = ShiroKit.getUserNotNull();
            roleList = user.getRoleList();
            if (roleList == null || roleList.size() == 0) {
                ShiroKit.getSubject().logout();
                model.addAttribute("tips", "该用户没有角色，无法登陆");
                return "/login.html";
            }
        } else {
            return "/login.html";
        }
        List<MenuNode> menus = userService.getUserMenuNodes(roleList);
        model.addAttribute("user", user);
        model.addAttribute("menus", menus);
        request.getSession().setAttribute("type", "2");
        return "/index.html";
    }


    /**
     * 跳转到登录页面
     *
     * @author fengshuonan
     * @Date 2018/12/23 5:41 PM
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login() {
        if (ShiroKit.isAuthenticated() || ShiroKit.getUser() != null) {
            return REDIRECT + "/";
        } else {
            return "/login.html";
        }
    }

    /**
     * 点击登录执行的动作
     *
     * @author fengshuonan
     * @Date 2018/12/23 5:42 PM
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String loginVali() {

        String username = super.getPara("username").trim();
        String password = super.getPara("password").trim();
        String remember = super.getPara("remember");

        Subject currentUser = ShiroKit.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(username, password.toCharArray());
        System.out.println(token.getPassword());

        //如果开启了记住我功能
        if ("on".equals(remember)) {
            token.setRememberMe(true);
        } else {
            token.setRememberMe(false);
        }

        //执行shiro登录操作
        currentUser.login(token);

        //登录成功，记录登录日志
        ShiroUser shiroUser = ShiroKit.getUserNotNull();
        LogManager.me().executeLog(LogTaskFactory.loginLog(shiroUser.getId(), getIp()));

        ShiroKit.getSession().setAttribute("sessionFlag", true);
        //ShiroKit.getSession().setAttribute("username",username);
        //ShiroKit.getSession().setAttribute("password",password);
        return REDIRECT + "/";
    }

    /**
     * 退出登录
     *
     * @author fengshuonan
     * @Date 2018/12/23 5:42 PM
     */
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logOut(HttpServletRequest request) {
        LogManager.me().executeLog(LogTaskFactory.exitLog(ShiroKit.getUserNotNull().getId(), getIp()));
        request.getSession().invalidate();
        ShiroKit.getSubject().logout();
        deleteAllCookie();
        return REDIRECT + "/login";
    }
}
