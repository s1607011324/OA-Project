/**
 * Copyright (c) 2015-2017, Chill Zhuang 庄骞 (smallchill@163.com).
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
package com.oa.modular.system.shiro;

import cn.stylefeng.roses.core.util.HttpContext;
import com.oa.core.common.constant.Const;
import com.oa.core.common.constant.factory.ConstantFactory;
import com.oa.core.common.exception.BizExceptionEnum;
import com.oa.core.util.SpringContextUtil;
import com.oa.modular.system.entity.User;
import cn.stylefeng.roses.core.util.ToolUtil;
import cn.stylefeng.roses.kernel.model.exception.ServiceException;
import com.oa.modular.system.entity.WXInfo;
import com.oa.modular.system.service.UserService;
import com.oa.modular.system.service.WxInfoService;
import com.oa.modular.system.shiro.service.UserAuthService;
import com.oa.modular.system.shiro.service.impl.UserAuthServiceServiceImpl;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Objects;

/**
 * shiro工具类
 *
 * @author dafei, Chill Zhuang
 */
public class ShiroKit {


    private static final String NAMES_DELIMETER = ",";

    /**
     * 加盐参数
     */
    public final static String hashAlgorithmName = "MD5";

    /**
     * 循环次数
     */
    public final static int hashIterations = 1024;

    @Resource
    private static UserService userService = (UserService) SpringContextUtil.getBean("userService");

    @Resource
    private static WxInfoService wxInfoService = (WxInfoService) SpringContextUtil.getBean("wxInfoService");

    @Resource
    private static UserAuthServiceServiceImpl userAuthServiceService = (UserAuthServiceServiceImpl) SpringContextUtil.getBean("userAuthServiceServiceImpl");

    /**
     * shiro密码加密工具类
     *
     * @param credentials 密码
     * @param saltSource  密码盐
     * @return
     */
    public static String md5(String credentials, String saltSource) {
        ByteSource salt = new Md5Hash(saltSource);
        return new SimpleHash(hashAlgorithmName, credentials, salt, hashIterations).toString();
    }

    /**
     * 获取随机盐值
     *
     * @param length
     * @return
     */
    public static String getRandomSalt(int length) {
        return ToolUtil.getRandomString(length);
    }

    /**
     * 获取当前 Subject
     *
     * @return Subject
     */
    public static Subject getSubject() {
        return SecurityUtils.getSubject();
    }

    /**
     * 获取封装的 ShiroUser
     *
     * @return ShiroUser
     */
    public static ShiroUser getUser() {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        String scene_str = null;
        try {
            scene_str = (String) request.getSession().getAttribute("scene_str");
        } catch (Exception e) {
            return (ShiroUser) getSubject().getPrincipals().getPrimaryPrincipal();
        }
        if (isGuest() && scene_str == null) {
            return null;
        } else if (scene_str != null) {
            WXInfo wxInfo = wxInfoService.selectList(scene_str);
            User users = userService.getById(wxInfo.getBinduserid());
            return userAuthServiceService.shiroUser(users);
        } else {
            return (ShiroUser) getSubject().getPrincipals().getPrimaryPrincipal();
        }
    }

    /**
     * 获取ShiroUser，不为空的
     *
     * @return ShiroUser
     */
    public static ShiroUser getUserNotNull() {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        String scene_str = (String) request.getSession().getAttribute("scene_str");
        if (isGuest() && scene_str == null) {
            throw new ServiceException(BizExceptionEnum.NOT_LOGIN);
        } else if (scene_str != null) {
            WXInfo wxInfo = wxInfoService.selectList(scene_str);
            User users = userService.getById(wxInfo.getBinduserid());
            return userAuthServiceService.shiroUser(users);
        } else {
            return (ShiroUser) getSubject().getPrincipals().getPrimaryPrincipal();
        }
    }

    /**
     * 从shiro获取session
     */
    public static Session getSession() {
        return getSubject().getSession();
    }

    /**
     * 获取shiro指定的sessionKey
     */
    @SuppressWarnings("unchecked")
    public static <T> T getSessionAttr(String key) {
        Session session = getSession();
        return session != null ? (T) session.getAttribute(key) : null;
    }

    /**
     * 设置shiro指定的sessionKey
     */
    public static void setSessionAttr(String key, Object value) {
        Session session = getSession();
        session.setAttribute(key, value);
    }

    /**
     * 移除shiro指定的sessionKey
     */
    public static void removeSessionAttr(String key) {
        Session session = getSession();
        if (session != null)
            session.removeAttribute(key);
    }

    /**
     * 验证当前用户是否属于该角色？,使用时与lacksRole 搭配使用
     *
     * @param roleName 角色名
     * @return 属于该角色：true，否则false
     */
    public static boolean hasRole(String roleName) {
        return getSubject() != null && roleName != null
                && roleName.length() > 0 && getSubject().hasRole(roleName);
    }

    /**
     * 与hasRole标签逻辑相反，当用户不属于该角色时验证通过。
     *
     * @param roleName 角色名
     * @return 不属于该角色：true，否则false
     */
    public static boolean lacksRole(String roleName) {
        return !hasRole(roleName);
    }

    /**
     * 验证当前用户是否属于以下任意一个角色。
     *
     * @param roleNames 角色列表
     * @return 属于:true,否则false
     */
    public static boolean hasAnyRoles(String roleNames) {
        boolean hasAnyRole = false;
        Subject subject = getSubject();
        if (subject != null && roleNames != null && roleNames.length() > 0) {
            for (String role : roleNames.split(NAMES_DELIMETER)) {
                if (subject.hasRole(role.trim())) {
                    hasAnyRole = true;
                    break;
                }
            }
        }
        return hasAnyRole;
    }

    /**
     * 验证当前用户是否属于以下所有角色。
     *
     * @param roleNames 角色列表
     * @return 属于:true,否则false
     */
    public static boolean hasAllRoles(String roleNames) {
        boolean hasAllRole = true;
        Subject subject = getSubject();
        if (subject != null && roleNames != null && roleNames.length() > 0) {
            for (String role : roleNames.split(NAMES_DELIMETER)) {
                if (!subject.hasRole(role.trim())) {
                    hasAllRole = false;
                    break;
                }
            }
        }
        return hasAllRole;
    }

    /**
     * 验证当前用户是否拥有指定权限,使用时与lacksPermission 搭配使用
     *
     * @param permission 权限名
     * @return 拥有权限：true，否则false
     */
    public static boolean hasPermission(String permission) {
        HttpServletRequest httpServletRequest = HttpContext.getRequest();
        String scene_str = (String) httpServletRequest.getSession().getAttribute("scene_str");
        if (scene_str!=null) return true;
        return getSubject() != null && permission != null
                && permission.length() > 0
                && getSubject().isPermitted(permission);
    }

    /**
     * 与hasPermission标签逻辑相反，当前用户没有制定权限时，验证通过。
     *
     * @param permission 权限名
     * @return 拥有权限：true，否则false
     */
    public static boolean lacksPermission(String permission) {
        return !hasPermission(permission);
    }

    /**
     * 已认证通过的用户。不包含已记住的用户，这是与user标签的区别所在。与notAuthenticated搭配使用
     *
     * @return 通过身份验证：true，否则false
     */
    public static boolean isAuthenticated() {
        return getSubject() != null && getSubject().isAuthenticated();
    }

    /**
     * 未认证通过用户，与authenticated标签相对应。与guest标签的区别是，该标签包含已记住用户。。
     *
     * @return 没有通过身份验证：true，否则false
     */
    public static boolean notAuthenticated() {
        return !isAuthenticated();
    }

    /**
     * 认证通过或已记住的用户。与guset搭配使用。
     *
     * @return 用户：true，否则 false
     */
    public static boolean isUser() {
        return getSubject() != null && getSubject().getPrincipal() != null;
    }

    /**
     * 验证当前用户是否为“访客”，即未认证（包含未记住）的用户。用user搭配使用
     *
     * @return 访客：true，否则false
     */
    public static boolean isGuest() {
        return !isUser();
    }

    /**
     * 输出当前用户信息，通常为登录帐号信息。
     *
     * @return 当前用户信息
     */
    public static String principal() {
        if (getSubject() != null) {
            Object principal = getSubject().getPrincipal();
            return principal.toString();
        }
        return "";
    }

    /**
     * 获取当前用户的部门数据范围的集合
     */
    public static List<Long> getDeptDataScope() {
        Long deptId = getUser().getDeptId();
        List<Long> subDeptIds = ConstantFactory.me().getSubDeptId(deptId);
        subDeptIds.add(deptId);
        return subDeptIds;
    }

    /**
     * 判断当前用户是否是超级管理员
     */
    public static boolean isAdmin() {
        List<Long> roleList = ShiroKit.getUser().getRoleList();
        for (Long integer : roleList) {
            String singleRoleTip = ConstantFactory.me().getSingleRoleTip(integer);
            if (singleRoleTip.equals(Const.ADMIN_NAME)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 通过用户表的信息创建一个shiroUser对象
     */
    public static ShiroUser createShiroUser(User user) {
        ShiroUser shiroUser = new ShiroUser();

        if (user == null) {
            return shiroUser;
        }

        shiroUser.setId(user.getUserId());
        shiroUser.setAccount(user.getAccount());
        shiroUser.setDeptId(user.getDeptId());
        shiroUser.setDeptName(ConstantFactory.me().getDeptName(user.getDeptId()));
        shiroUser.setName(user.getName());
        shiroUser.setEmail(user.getEmail());
        shiroUser.setAvatar(user.getAvatar());

        return shiroUser;
    }

}
