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
package com.oa.core.interceptor;

import com.oa.modular.system.shiro.ShiroKit;
import com.oa.modular.system.shiro.ShiroUser;
import com.oa.core.util.DefaultImages;
import com.oa.core.util.SpringContextUtil;
import com.oa.core.util.WeiXinUtil;
import com.oa.modular.system.entity.FileInfo;
import com.oa.modular.system.entity.User;
import com.oa.modular.system.entity.WXInfo;
import com.oa.modular.system.service.FileInfoService;
import com.oa.modular.system.service.UserService;
import com.oa.modular.system.service.WxInfoService;
import com.oa.modular.system.shiro.service.impl.UserAuthServiceServiceImpl;
import org.apache.shiro.authc.AuthenticationException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * 自动渲染当前用户信息登录属性 的过滤器
 *
 * @author fengshuonan
 * @Date 2018/10/30 4:30 PM
 */
public class AttributeSetInteceptor extends HandlerInterceptorAdapter {

    @Resource
    private FileInfoService fileInfoService = (FileInfoService) SpringContextUtil.getBean("fileInfoService");

    @Resource
    private WxInfoService wxInfoServices = (WxInfoService) SpringContextUtil.getBean("wxInfoService");

    @Resource
    private UserAuthServiceServiceImpl userAuthServiceService = (UserAuthServiceServiceImpl) SpringContextUtil.getBean("userAuthServiceServiceImpl");

    @Resource
    private UserService userService = (UserService) SpringContextUtil.getBean("userService");


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        //没有视图的直接跳过过滤器
        if (modelAndView == null || modelAndView.getViewName() == null) {
            return;
        }

        //视图结尾不是html的直接跳过
        if (!modelAndView.getViewName().endsWith("html")) {
            return;
        }
        String scene_str = (String) request.getSession().getAttribute("scene_str");
        ShiroUser user = null;
        if (scene_str != null) {
            WXInfo wxInfo = wxInfoServices.selectList(scene_str);
            User users = this.userService.getById(wxInfo.getBinduserid());
            user = userAuthServiceService.shiroUser(users);
        }else {
             user = ShiroKit.getUser();
        }
        //微信登录
        if (user == null && scene_str==null) {
            if (WeiXinUtil.weixin(request)){
                return;
            }
            throw new AuthenticationException("当前没有登录账号！");
        } else {
            User u = this.userService.getById(user.getId());
            FileInfo fileInfo = fileInfoService.getById(u.getAvatar());
            if (fileInfo != null) {
                modelAndView.addObject("filePath", fileInfo.getFileData());
            } else {
                modelAndView.addObject("filePath", DefaultImages.defaultAvatarUrl());
            }
            modelAndView.addObject("name", user.getName());
            modelAndView.addObject("email", user.getEmail());
            //request.getSession().removeAttribute("scene_str");
        }
    }
}
