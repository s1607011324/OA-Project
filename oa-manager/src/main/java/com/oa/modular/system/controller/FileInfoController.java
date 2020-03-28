package com.oa.modular.system.controller;

import cn.stylefeng.roses.core.base.controller.BaseController;
import cn.stylefeng.roses.core.reqres.response.ResponseData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.core.common.page.LayuiPageFactory;
import com.oa.core.util.FileUtil;
import com.oa.modular.system.entity.FileInfo;
import com.oa.modular.system.service.FileInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.terracotta.modules.ehcache.ToolkitInstanceFactoryImpl.LOGGER;

@Controller
@RequestMapping("/file")
public class FileInfoController extends BaseController {

    private static String PREFIX = "/modular/system/upload";

    @Autowired
    private FileInfoService fileInfoService;

    /**
     * 跳转到查看附件页面
     *
     * @return
     */
    @RequestMapping("/getFileInfo")
    public Object getFileInfo() {
        return PREFIX + "/getFile.html";
    }

    /**
     * 跳转到上传附件的页面
     *
     * @return
     */
    @RequestMapping("/fileUploads")
    public Object FileUploads() {
        return PREFIX + "/fileUpload.html";
    }

    /**
     * 获取附件信息
     *
     * @param leaveId
     * @return
     */
    @ResponseBody
    @RequestMapping("/getFileInfos/{leaveId}")
    public Object getFileInfo(@PathVariable String leaveId) {
        Page<Map<String, Object>> fileInfos = fileInfoService.getFiles(leaveId);
        if (fileInfos == null) {
            return ResponseData.success(-1, "请求失败", null);
        }
        return LayuiPageFactory.createPageInfo(fileInfos);
    }

    @ResponseBody
    @RequestMapping("/uploadFileAdd/{leaveId}")
    public Object FileAdd(@PathVariable Long leaveId, @RequestParam(value = "file", required = false) MultipartFile[] file) throws IOException {
        File upload = fileInfoService.uploadFile();
        if (upload != null) {
            FileInfo fileInfo = new FileInfo();
            String TimeStamp = IdWorker.getIdStr();
            for (MultipartFile multipartFile : file) {
                String avatar = upload.getAbsolutePath() + "//" + TimeStamp + multipartFile.getOriginalFilename();
                //保存附件文件
                fileInfo.setFileId(IdWorker.getIdStr());
                fileInfo.setLeaveId(leaveId);
                fileInfo.setFileData("/assets/common/upload/" + TimeStamp + multipartFile.getOriginalFilename());
                fileInfo.setFileName(TimeStamp + multipartFile.getOriginalFilename());
                // 转存文件
                multipartFile.transferTo(new File(avatar));
                fileInfo.setCreateTime(new Date());
                fileInfoService.save(fileInfo);
            }
        }
        return ResponseData.success(0, "上传成功", null);
    }


    @ResponseBody
    @RequestMapping("/download")
    public void download(@RequestParam("fileName") String fileName, HttpServletResponse resp) throws IOException {
        FileInputStream fis = null;
        try {
            //获取当前项目的绝对路径
            File upload = fileInfoService.uploadFile();
            fis = new FileInputStream(upload.getAbsoluteFile()+"//"+fileName);
            // 设置信息给客户端不解析
            String type = new MimetypesFileTypeMap().getContentType(fileName);
            resp.setContentType(type+";charset=UTF-8");
            resp.setCharacterEncoding("UTF-8");
            resp.setHeader("Content-Disposition",
                    "attachment;filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
            byte[] b = new byte[100];
            int len;
            while ((len = fis.read(b)) > 0) {
                resp.getOutputStream().write(b, 0, len);
            }
        } catch (Exception e) {
            LOGGER.error("文件[ {} ]下载错误", fileName);
        } finally {
            resp.getOutputStream().flush();
            resp.getOutputStream().close();
            fis.close();
        }
    }

    /**
     * 富文本编辑器的图片上传
     *
     * @param file
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/uploadImg")
    public Object uploadImg(HttpServletRequest request,@RequestParam(value = "file", required = false) MultipartFile file) throws Exception {
        File upload = fileInfoService.uploadFile();
        String FileData = "";
        String FileName = "";
        int code = -1;
        String msgs = "上传失败！";
        ImgS imgS = null;
        if (upload != null) {
            String TimeStamp = IdWorker.getIdStr();
            String avatar = upload.getAbsolutePath() + "//" + TimeStamp + file.getOriginalFilename();
            //保存附件文件
            imgS = new ImgS();
            FileData = request.getContextPath()+"/assets/common/upload/" + TimeStamp + file.getOriginalFilename();
            FileName = TimeStamp + file.getOriginalFilename();
            imgS.setSrc(FileData);
            imgS.setTitle(FileName);
            imgS.setHeight("20%");
            imgS.setWidth("20%");
            // 转存文件
            file.transferTo(new File(avatar));
            code = 0;
            msgs = "上传成功！";
        }
        //code:0表示成功，其它失败
        //msg:提示信息 //一般上传失败后返回
        //src:图片路径 必填
        //title:图片名称
        return ResponseData.success(code, msgs, imgS);//JSONObject.parseObject("{\"code\":\""+code+"\",\"msg\":\""+msgs+"\"，\"data\":{\"src\":\""+FileData+"\",\"title\":\""+FileName+"\"}}");
    }

    /**
     * 删除文件
     *
     * @param FileId
     * @return
     */
    @RequestMapping("/delInfo/{FileId}")
    @ResponseBody
    public ResponseData del(@PathVariable String FileId) {
        fileInfoService.delFileInfo(FileId);
        return SUCCESS_TIP;
    }
}

class ImgS {
    private String src;
    private String title;
    private String height;
    private String width;

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }
}
