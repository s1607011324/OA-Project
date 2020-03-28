package com.oa.core.util;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.oa.modular.system.entity.TextMessage;
import com.oa.modular.system.entity.WXSystem;
import com.oa.modular.system.service.SystemService;
import com.thoughtworks.xstream.XStream;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Time;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageUtil {
    public static final String MESSAGE_TEXT = "text";//文本消息
    public static final String MESSAGE_IMAGE = "image";//图片消息
    public static final String MESSAGE_VOICE = "voice";//语音消息
    public static final String MESSAGE_VIDEO = "video";//视频
    public static final String MESSAGE_SHORTVIDEO = "shortvideo";//小视屏
    public static final String MESSAGE_LOCATION = "location";//地理位置
    public static final String MESSAGE_LINK = "link";//链接
    public static final String MESSAGE_EVENT = "event";//事件推送
    public static final String MESSAGE_SUBSCRIBE = "subscribe";//被关注
    public static final String MESSAGE_UNSUBSCRIBE = "unsubscribe";//取消关注
    public static final String MESSAGE_CLICK = "CLICK";//点击
    public static final String MESSAGE_VIEW = "VIEW";//

    private final static Logger log = LoggerFactory.getLogger(MessageUtil.class);

    @Resource
    private static SystemService systemService = (SystemService) SpringContextUtil.getBean("systemService");

    public static TextMessage WXSent(HttpServletRequest request, String toUserName, String fromUserName, String content) {
        String cmd = "cmd /c ";
        TextMessage text = new TextMessage();
        text.setFromUserName(toUserName);
        text.setToUserName(fromUserName);
        text.setCreateTime(new Date().getTime() + "");
        text.setMsgType("text");
        text.setContent("欢迎关注此公众号，仅供测试和学习！");
        String server = null; //服务名称变量
        String time = "0"; //时间变量
        if (content.contains("显示远程控制命令") || content.equalsIgnoreCase("help")) {
            text.setContent("请输入开机密码！（回复格式：开机密码+您的密码）");
            return text;
            //shutdown -a
            //sc config i8042prt start= disabled
            //sc config i8042prt start= auto
        }
        WXSystem sys = systemService.selectSys(new QueryWrapper<>());
        long allow = new Date().getTime()-sys.getAllow();
        System.out.println(allow);
        if (content.contains("开机密码") || allow<=600000) {
            int pwdIndex = content.lastIndexOf("码");
            if (pwdIndex!=-1) {
                WXSystem system = systemService.selectSys(content.substring(pwdIndex + 1));
                if (system!=null){
                    if (system.getPwd() != null && content.equalsIgnoreCase("开机密码" + system.getPwd())) {
                        String sbs = "使用以下命令，可以轻松控制电脑:\n" +
                                "\t\t\t打开xxx服务：启动|开启+服务名称\n" +
                                "\t\t\t关闭xxx服务：停止|关闭+服务名称\n" +
                                "\t\t\t禁用键盘按键：禁用键盘\n" +
                                "\t\t\t恢复键盘使用：恢复键盘\n" +
                                "\t\t\t取消任务计划：取消任务\n" +
                                "\t\t\t定时重启：重启+时间（单位 秒）\n" +
                                "\t\t\t定时关机：关机+时间（单位 秒）\n" +
                                "\t\t\t电脑锁屏：锁屏\n" +
                                "\t\t\t电脑重启：重启\n" +
                                "\t\t\t电脑关机：关机\n" +
                                "\t\t\t重置密码：重置+#旧密码+#新密码\n";
                        text.setContent(sbs);
                        WXSystem wxSystem = new WXSystem();
                        wxSystem.setId(system.getId());
                        wxSystem.setAllow(new Date().getTime());
                        systemService.update(wxSystem);
                        return text;
                    }
                }
            }
            if (content.contains("启动") || content.contains("开启")) {
                int result1 = content.lastIndexOf("动");
                int result2 = content.lastIndexOf("启");
                if (result1 != -1 && result2 == -1) {
                    server = content.substring(result1 + 1);
                }
                if (result2 != -1 && result1 == -1) {
                    server = content.substring(result2 + 1);
                }
                if (!"".equalsIgnoreCase(server) && server != null) {
                    MessageUtil.callCmd(" net start " + server);
                    String ipAddr = MessageUtil.callCmd("cmd /c ipconfig");
                    String ip = ipAddr.substring(ipAddr.indexOf("L") - 1, ipAddr.lastIndexOf("无"));
                    text.setContent("启动" + server + "服务成功！\n主机内网IP：" + ip.substring(ip.lastIndexOf("址") + 27, ip.lastIndexOf("子")));
                    return text;
                }
            }
            if (content.contains("停止") || content.contains("关闭")) {
                int result1 = content.lastIndexOf("止");
                int result2 = content.lastIndexOf("闭");
                if (result1 != -1 && result2 == -1) {
                    server = content.substring(result1 + 1);
                }
                if (result2 != -1 && result1 == -1) {
                    server = content.substring(result2 + 1);
                }
                if (!"".equalsIgnoreCase(server) && server != null) {
                    MessageUtil.callCmd("cmd /c net stop " + server);
                    text.setContent("停止" + server + "服务成功！");
                    return text;
                }
            }
            if (content.contains("重启")) {
                int result1 = content.lastIndexOf("启");
                if (result1 != -1) {
                    time = content.substring(result1 + 1);
                }
                if (!"".equalsIgnoreCase(time) && !"0".equalsIgnoreCase(time)) {
                    MessageUtil.callCmd("shutdown /r /t " + time);
                    text.setContent("定时重启成功！\n电脑将在" + time + "秒后重启！");
                    return text;
                }
                if ("".equalsIgnoreCase(time)) {
                    MessageUtil.callCmd("shutdown /r /t 0");
                    text.setContent("重启成功！");
                    return text;
                }
            }
            if (content.contains("关机")) {
                int result1 = content.lastIndexOf("机");
                if (result1 != -1) {
                    time = content.substring(result1 + 1);
                }
                if (!"".equalsIgnoreCase(time) && !"0".equalsIgnoreCase(time)) {
                    MessageUtil.callCmd("shutdown /s /t " + time);
                    text.setContent("定时关机成功！\n电脑将在" + time + "秒后关机！");
                    return text;
                }
                MessageUtil.callCmd("shutdown /s /t 0");
                text.setContent("关机成功！");
                return text;
            }
            if (content.equalsIgnoreCase("禁用键盘")) {
                MessageUtil.callCmd("sc config i8042prt start= disabled");
                text.setContent("禁用键盘成功！重启生效！请手动重启电脑！");
                return text;
            }
            if (content.equalsIgnoreCase("恢复键盘")) {
                MessageUtil.callCmd("sc config i8042prt start= auto");
                text.setContent("恢复键盘成功！重启生效！请手动重启电脑！");
                return text;
            }
            if (content.equalsIgnoreCase("取消任务")) {
                MessageUtil.callCmd("shutdown /a");
                text.setContent("取消任务计划成功！");
                return text;
            }
            if (content.equalsIgnoreCase("锁屏")) {
                MessageUtil.callCmd("rundll32.exe user32.dll,LockWorkStation");
                text.setContent("电脑锁屏成功！");
                return text;
            }
            //rundll32.exe user32.dll,LockWorkStation

            if (content.contains("重置")) {
                int result1 = content.indexOf("#");
                int result2 = content.lastIndexOf("#");
                if (result1 != -1 && result2 != -1) {
                    WXSystem system = systemService.selectSys(content.substring(result1 + 1, result2));
                    WXSystem systems = new WXSystem();
                    systems.setId(system.getId());
                    systems.setPwd(content.substring(result2 + 1));
                    systems.setAllow(new Date().getTime());
                    systemService.update(systems);
                    text.setContent("重置密码成功！请牢记您的密码");
                }
            }
        }
        return text;
    }
                /*if (content.contains("签名")) {
            if (content.contains(":") || content.contains("：")) {
                if (content.contains(":")) {
                    result = content.indexOf(":");
                } else if (content.contains("：")) {
                    result = content.indexOf("：");
                }
                userName = content.substring(result + 1);
                text.setContent("签名[" + userName + "]正在生成请稍等");
            } else {
                text.setContent("请输入正确格式签名,(签名:xxx)\n举例:签名:张三");
            }
        } else
        } else {
            text.setContent("请按格式输入名字,(签名:xxx)\n举例:签名:张三");
        }*/

                 /*String path_bat = "D:\\BDGX\\Y2\\oa-manager\\启动SSH.bat";
                            File batFile = new File(path_bat);
                            System.out.println("batFileExist:" + batFile.exists());
                            if (batFile.exists()) {

                            }*/

    private static String callCmd(String locationCmd) {
        StringBuilder sb = new StringBuilder();
        try {
            Process child = Runtime.getRuntime().exec(locationCmd);
            InputStream in = child.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, "GBK"));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line + "\n");
                //System.out.println(line.substring(line.indexOf("L")));
            }
            in.close();
            try {
                child.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("sb:" + sb.toString());
            System.out.println("callCmd execute finished");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * xml转换为map集合
     *
     * @param request
     * @return
     * @throws Exception
     */
    public static Map<String, String> xmlToMap(HttpServletRequest request) throws Exception {
        Map<String, String> map = new HashMap<>();
        SAXReader reader = new SAXReader();

        InputStream ins = request.getInputStream();
        Document doc = reader.read(ins);
        Element root = doc.getRootElement();

        List<Element> list = root.elements();
        for (Element element : list) {
            map.put(element.getName(), element.getText());
        }
        ins.close();
        return map;
    }

    /**
     * 将文本对象转换为xml
     *
     * @param textMessage
     * @return
     */
    public static String textMessageToxml(TextMessage textMessage) {
        XStream xStream = new XStream();
        xStream.alias("xml", textMessage.getClass());
        return xStream.toXML(textMessage);
    }

    /*    *//**
     * 将图片对象转换为xml
     *
     * @param imageMessage
     * @return
     *//*
    public static String imageMessageToxml(ImageMessage imageMessage) {
        XStream xStream = new XStream();
        xStream.alias("xml", imageMessage.getClass());
        return xStream.toXML(imageMessage);
    }

    *//**
     * 图片消息组装
     * @param toUserName
     * @param fromUserName
     * @return
     *//*
    public static String initNewsMessage (String toUserName, String fromUserName)throws Exception {
        String message = null;
        Image image = new Image();
        AccessToken token = new AccessToken();
        if (WeiXinUtil.accessToken == null){
            log.info("===================accessToken为空进行获取");
            WeiXinUtil.accessToken = WeiXinUtil.getAccessToken();
        }
        log.info("票据:"+WeiXinUtil.accessToken.getToken());
        log.info("有效时间:"+WeiXinUtil.accessToken.getExpiresIn());

//            SeleniumController.signatureGenerationTwo(userName);//根据名字生成签名
//            image.setMediaId(WeiXinUtil.upload(SeleniumController.pageUrl,WeiXinUtil.accessToken.getToken(),"image"));
        image.setMedia_id(WeiXinUtil.upload("img/签名格式.png",WeiXinUtil.accessToken.getToken(),"image"));
//            image.setMediaId(WeiXinUtil.upload("img/0.jpg",WeiXinUtil.accessToken.getToken(),"image"));

        ImageMessage imageMessage = new ImageMessage();
        imageMessage.setFromUserName(toUserName);
        imageMessage.setToUserName(fromUserName);
        imageMessage.setMsgType(MESSAGE_IMAGE);
        imageMessage.setCreateTime(new Date().getTime()+"");
        imageMessage.setImage(image);
        message = imageMessageToxml(imageMessage);
        return message;
    }

    *//**
     * 客服图片消息组装
     * @param touser
     *//*
    public static String cstomImageMessage(String touser, String userName) throws Exception {
        if (WeiXinUtil.accessToken == null){
            log.info("===================accessToken为空进行获取");
            WeiXinUtil.accessToken = WeiXinUtil.getAccessToken();
        }
        log.info("票据:"+WeiXinUtil.accessToken.getToken());
        log.info("有效时间:"+WeiXinUtil.accessToken.getExpiresIn());
        CustomImageMessage customImageMessage = new CustomImageMessage();
        customImageMessage.setTouser(touser);
        customImageMessage.setMsgtype(MESSAGE_IMAGE);
        Image image = new Image();
        SeleniumController.signatureGenerationTwo(userName);//根据名字生成签名
        image.setMedia_id(WeiXinUtil.upload(SeleniumController.pageUrl,WeiXinUtil.accessToken.getToken(),"image"));
        customImageMessage.setImage(image);
        Gson gson=new Gson();
        String json = gson.toJson(customImageMessage);
        log.info(json);
        return json;
    }

    *//**
     * 发送图片客服消息
     * @param touser
     * @param userName
     * @throws Exception
     *//*
    public static void cstomImageMessageShow(String touser, String userName)throws Exception {
        String url="https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=ACCESS_TOKEN";
        if (WeiXinUtil.accessToken == null){
            log.info("===================accessToken为空进行获取");
            WeiXinUtil.accessToken = WeiXinUtil.getAccessToken();
        }
        log.info(""+ JSON.toJSON(WeiXinUtil.accessToken));
        url = url.replaceAll("ACCESS_TOKEN", WeiXinUtil.accessToken.getToken());
        String result =  WeiXinUtil.httpsRequest(url,"POST",cstomImageMessage(touser,userName));
        log.info(result);
    }*/
}


