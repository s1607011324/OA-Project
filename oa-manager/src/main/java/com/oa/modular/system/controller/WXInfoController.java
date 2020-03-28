package com.oa.modular.system.controller;

import cn.stylefeng.roses.core.base.controller.BaseController;
import com.oa.core.util.MessageUtil;
import com.oa.modular.system.entity.TextMessage;
import com.oa.modular.system.entity.WXInfo;
import com.oa.modular.system.service.UserService;
import com.oa.modular.system.service.WxInfoService;
import com.oa.modular.system.shiro.service.UserAuthService;
import com.thoughtworks.xstream.XStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.oa.core.util.WeiXinUtil.*;

@Controller
//@RequestMapping("/weixin")
public class WXInfoController extends BaseController {

    @Resource
    private WxInfoService wxInfoService;

    @Resource
    private UserService userService;

    @Autowired
    private UserAuthService userAuthService;


    // 生成带参数的二维码，扫描关注微信公众号，自动登录网站
    @RequestMapping(value = "/weixin/mpLogin")
    public Object wechatMpLogin(Model model, ModelMap modelMap) throws Exception {
        String access_token = getAccessToken();
        String url = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=" + access_token;
        String scene_str = "lrfun.com." + new Date().getTime();
        String params = "{\"expire_seconds\":600, \"action_name\":\"QR_STR_SCENE\", \"action_info\":{\"scene\":{\"scene_str\":\"" + scene_str + "\"}}}";
        Map<String, Object> resultMap = httpClientPost(url, params);
        if (resultMap.get("ticket") != null) {
            String qrcodeUrl = "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=" + resultMap.get("ticket");
            modelMap.put("qrUrl", qrcodeUrl);
            System.out.println(qrcodeUrl);
        }

        model.addAttribute("scene_str", scene_str);
        return "/modular/frame/wechatMpLogin.html";
    }

    // 检测登录
    @RequestMapping(value = "/weixin/checkLogin")
    @ResponseBody
    public Map<String, Object> wechatMpCheckLogin(HttpServletRequest request, Model model, @RequestParam String scene_str) {
        // 根据scene_str查询数据库，获取对应记录
        // SELECT * FROM wechat_user_info WHERE event_key='scene_str';
        WXInfo wxInfo = wxInfoService.selectList(scene_str);
        Map<String, Object> returnMap = new HashMap<String, Object>();
        if (wxInfo != null) {
            request.getSession().setAttribute("scene_str", scene_str);
            if (wxInfo.getBinduserid() != null) {
                returnMap.put("result", "true");
                model.addAttribute("users", wxInfo);
                returnMap.put("reg", "false");
            } else {
                returnMap.put("reg", "true");
            }
        } else {
            returnMap.put("result", "false");
        }
        return returnMap;
    }


/*    @RequestMapping("/weixin/bind")
    public Object wxBind(String str){
        *//*List<WXInfo> wxInfoList = wxInfoService.selectList(str);
        for (WXInfo wxInfo : wxInfoList) {
            User user = this.userService.getById(wxInfo.getBinduserid());
            if (user.getAvatar().equalsIgnoreCase(ShiroKit.getSession().getAttribute("username").toString())&&user.getPassword().equalsIgnoreCase(ShiroKit.md5(ShiroKit.getSession().getAttribute("password").toString(),user.getSalt()))){

            }
        }
        return *//*
    }*/

    @RequestMapping("/weixin/WXReg")
    public Object wxReg() {
        return "/modular/frame/WXReg.html";
    }

    /**
     * 微信公众号签名认证接口
     *
     * @throws
     * @Title: test
     * @Description: TODO
     * @param: @param signature
     * @param: @param timestamp
     * @param: @param nonce
     * @param: @param echostr
     * @param: @return
     * @return: String
     */
    @RequestMapping(value = "/weixin/wx", method = RequestMethod.GET)
    public String test(String signature, String timestamp, String nonce, String echostr) throws IOException {

        // 通过检验signature对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败
        if (signature != null && checkSignature(signature, timestamp, nonce)) {
            System.out.println(echostr);
            return echostr;
        }
        return null;
    }

    // 回调函数
    @RequestMapping(value = "/weixin/wx", method = RequestMethod.POST)
    public void callback(HttpServletRequest httpServletRequest, HttpServletResponse response) throws Exception {
        String allow = (String) httpServletRequest.getSession().getAttribute("allow");
        httpServletRequest.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, String> callbackMap = xmlToMap(httpServletRequest); //获取回调信息
        //下面是返回的xml
        // <xml><ToUserName><![CDATA[gh_f6b4da984c87]]></ToUserName> //微信公众号的微信号
        //<FromUserName><![CDATA[oJxRO1Y2NgWJ9gMDyE3LwAYUNdAs]]></FromUserName> //openid用于获取用户信息，做登录使用
        //<CreateTime>1531130986</CreateTime> //回调时间
        //<MsgType><![CDATA[event]]></MsgType>
        //<Event><![CDATA[SCAN]]></Event>
        //<EventKey><![CDATA[lrfun.com.UxJkWC1531967386903]]></EventKey> //上面自定义的参数（scene_str）
        //<Ticket><![CDATA[gQF57zwAAAAAAAAAAS5odHRwOi8vd2VpeGluLnFxLmNvbS9xLzAyY2ljbjB3RGtkZWwxbExLY3hyMVMAAgTvM0NbAwSAOgkA]]></Ticket> //换取二维码的ticket
        //</xml>
        if (callbackMap != null) {
            System.out.println(callbackMap.get("MsgType"));
            if (callbackMap.get("MsgType").equalsIgnoreCase("text")) {
                String toUserName = callbackMap.get("ToUserName");
                String fromUserName = callbackMap.get("FromUserName");
                String msgType = callbackMap.get("MsgType");
                String content = callbackMap.get("Content");
                try {
                    String message = null;
                    if (MessageUtil.MESSAGE_TEXT.equals(msgType)) {
                        TextMessage text = MessageUtil.WXSent(httpServletRequest,toUserName,fromUserName,content);
                        message = MessageUtil.textMessageToxml(text);
                    }
                    out.print(message);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    out.close();

                }
            }
            if (callbackMap.get("Event") != null && callbackMap.get("FromUserName") != null) {
                // 通过openid获取用户信息
                Map<String, Object> wechatUserInfoMap = getUserInfoByOpenid(callbackMap.get("FromUserName"));
                String eventType = callbackMap.get("Event");//事件类型
                String EventKey = callbackMap.get("EventKey");
                System.out.println(eventType);
                System.out.println(EventKey);
                if (eventType.equals("SCAN")) {
                    WXInfo w = new WXInfo();
                    w.setOpenid(wechatUserInfoMap.get("openid").toString());
                    w.setEventkey(EventKey);
                    wxInfoService.update(w);
                }
                if (eventType.equals("subscribe")) {
                    if (wechatUserInfoMap.size() != 0) {
                        WXInfo w = new WXInfo();
                        System.out.println(wechatUserInfoMap.get("openid").toString());
                        w.setOpenid(wechatUserInfoMap.get("openid").toString());
                        w.setNickname(wechatUserInfoMap.get("nickname").toString());
                        w.setSex((Double) wechatUserInfoMap.get("sex"));
                        w.setHeadimgurl(wechatUserInfoMap.get("headimgurl").toString());
                        w.setCountry(wechatUserInfoMap.get("country").toString());
                        w.setProvince(wechatUserInfoMap.get("province").toString());
                        w.setCity(wechatUserInfoMap.get("city").toString());
                        w.setEventkey(EventKey.substring(EventKey.indexOf('_') + 1, EventKey.length()));
                        wxInfoService.add(w);
                    }
                }
                System.out.println(wechatUserInfoMap.get("openid").toString());
                if (eventType.equals("unsubscribe")) {
                    wxInfoService.delete(wechatUserInfoMap.get("openid").toString());
                }
                // 将数据写入到数据库中，前面自定义的参数（scene_str）也需记录到数据库中，后面用于检测匹配登录
                // INSERT INTO wechat_user_info......(数据库操作)
            }
        }
        out.println("");
        //return new ModelAndView("/test/login.html","text","no");
    }
}
