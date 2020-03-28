package com.oa.core.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.oa.modular.system.controller.WXInfoController;
import com.oa.modular.system.entity.TextMessage;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 签名认证工具类
 * @ClassName:  WeixinCheckoutUtil
 * @Description:TODO
 * @author: jp
 * @date:   2019年6月13日 下午4:56:12
 *
 * @Copyright: 2019 www.tydic.com Inc. All rights reserved.
 *
 */
public class WeiXinUtil {

    // 与接口配置信息中的Token要一致
    private static String token = "youjp";
    //测试公众号
    private static final String app_id = "wx66404b893ada5601";
    private static final String app_secret = "cbd8735827c8290eb521e89d813dcdde";

    private static final Gson gson = new Gson();

    public static Boolean weixin(HttpServletRequest request){
        HashMap<String,String> map = new HashMap();
        map.put("mpLogin","/weixin/mpLogin");
        map.put("wx","/weixin/wx");
        map.put("WXReg","/weixin/WXReg");
        map.put("checkLogin.html", "/weixin/checkLogin");
        map.put("login", "/weixin/login");
        map.put("/", "/");
        for (String s : map.keySet()) {
            //System.out.println(request.getRequestURI());
            //System.out.println(map.get(s));
            if (request.getRequestURI().equals(map.get(s))){
                return true;
            }
        }
        return false;
    }
    // xml转为map
    public static Map<String, String> xmlToMap(HttpServletRequest httpServletRequest) {
        Map<String, String> map = new HashMap<String, String>();
        try {
            InputStream inputStream = httpServletRequest.getInputStream();
            SAXReader reader = new SAXReader(); // 读取输入流
            org.dom4j.Document document = reader.read(inputStream);
            Element root = document.getRootElement(); // 得到xml根元素
            List<Element> elementList = root.elements(); // 得到根元素的所有子节点
            // 遍历所有子节点
            for (Element e : elementList)
                map.put(e.getName(), e.getText());
            // 释放资源
            inputStream.close();
            inputStream = null;
            return map;
        } catch (Exception e) {
            e.getMessage();
        }
        return null;
    }

    /***
     * httpClient-Get请求
     * @param url 请求地址
     * @return
     * @throws Exception
     */
    private static Map<String, Object> httpClientGet(String url) throws Exception {
        org.apache.commons.httpclient.HttpClient client = new org.apache.commons.httpclient.HttpClient();
        client.getParams().setContentCharset("UTF-8");
        GetMethod httpGet = new GetMethod(url);
        try {
            client.executeMethod(httpGet);
            String response = httpGet.getResponseBodyAsString();
            Map<String, Object> map = gson.fromJson(response, Map.class);
            return map;
        } catch (Exception e) {
            throw e;
        } finally {
            httpGet.releaseConnection();
        }
    }

    /***
     * httpClient-Post请求
     * @param url 请求地址
     * @param params post参数
     * @return
     * @throws Exception
     */
    public static Map<String, Object> httpClientPost(String url, String params) throws Exception {
        org.apache.commons.httpclient.HttpClient client = new HttpClient();
        client.getParams().setContentCharset("UTF-8");
        PostMethod httpPost = new PostMethod(url);
        try {
            RequestEntity requestEntity = new ByteArrayRequestEntity(params.getBytes("utf-8"));
            httpPost.setRequestEntity(requestEntity);
            client.executeMethod(httpPost);
            String response = httpPost.getResponseBodyAsString();
            Map<String, Object> map = gson.fromJson(response, Map.class);
            return map;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            httpPost.releaseConnection();
        }
    }

    // 获取access_tocken
    public static String getAccessToken() throws Exception {
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + app_id + "&secret=" + app_secret;
        Map<String, Object> accessTokenMap = httpClientGet(url);
        System.out.println(accessTokenMap);
        return accessTokenMap.get("access_token").toString();
    }

    // 通过openid获取用户信息
    public static Map<String, Object> getUserInfoByOpenid(String openid) throws Exception {
        String access_tocken = getAccessToken();
        String url = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=" + access_tocken + "&openid=" + openid;
        Map<String, Object> map = httpClientGet(url);
        return map;
    }


    /**
     * 验证签名
     *
     * @param signature
     * @param timestamp
     * @param nonce
     * @return
     */
    public static boolean checkSignature(String signature, String timestamp, String nonce) {
        String[] arr = new String[] { token, timestamp, nonce };
        // 将token、timestamp、nonce三个参数进行字典序排序
        // Arrays.sort(arr);
        sort(arr);
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            content.append(arr[i]);
        }
        MessageDigest md = null;
        String tmpStr = null;

        try {
            md = MessageDigest.getInstance("SHA-1");
            // 将三个参数字符串拼接成一个字符串进行sha1加密
            byte[] digest = md.digest(content.toString().getBytes());
            tmpStr = byteToStr(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        content = null;
        // 将sha1加密后的字符串可与signature对比，标识该请求来源于微信

        return tmpStr != null ? tmpStr.equals(signature.toUpperCase()) : false;
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param byteArray
     * @return
     */
    private static String byteToStr(byte[] byteArray) {
        String strDigest = "";
        for (int i = 0; i < byteArray.length; i++) {
            strDigest += byteToHexStr(byteArray[i]);
        }
        return strDigest;
    }

    /**
     * 将字节转换为十六进制字符串
     *
     * @param mByte
     * @return
     */
    private static String byteToHexStr(byte mByte) {
        char[] Digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        char[] tempArr = new char[2];
        tempArr[0] = Digit[(mByte >>> 4) & 0X0F];
        tempArr[1] = Digit[mByte & 0X0F];
        String s = new String(tempArr);
        return s;
    }
    public static void sort(String a[]) {
        for (int i = 0; i < a.length - 1; i++) {
            for (int j = i + 1; j < a.length; j++) {
                if (a[j].compareTo(a[i]) < 0) {
                    String temp = a[i];
                    a[i] = a[j];
                    a[j] = temp;
                }
            }
        }
    }

}