package com.oa.modular.system.service;

import cn.stylefeng.roses.core.util.ToolUtil;
import cn.stylefeng.roses.kernel.model.exception.RequestEmptyException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.core.common.page.LayuiPageFactory;
import com.oa.modular.system.shiro.ShiroKit;
import com.oa.modular.system.shiro.ShiroUser;
import com.oa.modular.system.entity.FileInfo;
import com.oa.modular.system.entity.User;
import com.oa.modular.system.mapper.FileInfoMapper;
import cn.stylefeng.roses.kernel.model.exception.ServiceException;
import cn.stylefeng.roses.kernel.model.exception.enums.CoreExceptionEnum;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 文件信息表
 * 服务实现类
 * </p>
 *
 * @author stylefeng
 * @since 2018-12-07
 */
@Service
public class FileInfoService extends ServiceImpl<FileInfoMapper, FileInfo> {

    @Autowired
    private UserService userService;

    @Autowired
    private FileInfoService fileInfoService;

    /**
     * 上传头像
     *
     * @author fengshuonan
     * @Date 2018/11/10 4:10 PM
     */
    @Transactional(rollbackFor = Exception.class)
    public FileInfo uploadAvatar(MultipartFile file,String avatar) {
        ShiroUser currentUser = ShiroKit.getUser();
        User user = userService.getById(currentUser.getId());
        if (!file.isEmpty()) {
            try {
                /*//获取跟目录
                File path = new File(ResourceUtils.getURL("classpath:").getPath());
                if (!path.exists()) path = new File("");

                //如果上传目录为/assets/common/upload/，则可以如下获取：
                File upload = new File(path.getAbsolutePath(), "assets/common/upload/");
                if (!upload.exists()) upload.mkdirs();*/
                File upload = uploadFile();
                if (upload!=null){
                    String TimeStamp = IdWorker.getIdStr();
                    avatar = upload.getAbsolutePath()+"//"+TimeStamp+file.getOriginalFilename();
                    //用来保存信息
                    FileInfo fileInfo = new FileInfo();
                    fileInfo.setFileId(user.getAvatar());
                    fileInfo.setFileData("/assets/common/upload/" + TimeStamp + file.getOriginalFilename());
                    fileInfo.setFileName(TimeStamp+file.getOriginalFilename());
                    if (user.getAvatar()!=null){
                        // 查询回显
                        FileInfo fileInfos = fileInfoService.getById(user.getAvatar());
                        // 存在就删除
                        File fileIsExits=new File(upload.getAbsolutePath()+"//"+fileInfos.getFileName());
                        if(fileIsExits.exists()) {
                            Boolean result = fileIsExits.delete();
                        }// 转存文件
                        file.transferTo(new File(avatar));
                        fileInfo.setUpdateTime(new Date());
                        //修改文件信息
                        this.updateById(fileInfo);
                    }else {
                        // 转存文件
                        file.transferTo(new File(avatar));
                        //保存文件信息
                        fileInfo.setFileId(IdWorker.getIdStr());
                        fileInfo.setCreateTime(new Date());
                        this.save(fileInfo);
                    }

                    //更新用户的头像
                    user.setAvatar(fileInfo.getFileId());
                    userService.updateById(user);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (ToolUtil.isEmpty(avatar)) {
            throw new RequestEmptyException("请求头像为空");
        }
        if (currentUser == null) {
            throw new ServiceException(CoreExceptionEnum.NO_CURRENT_USER);
        }
        return fileInfoService.getById(user.getAvatar());
    }

    public File uploadFile() throws FileNotFoundException {
        //获取跟目录
        File path = new File(ResourceUtils.getURL("classpath:").getPath());
        if (!path.exists()){
            path = new File("");
        }

        //如果上传目录为/assets/common/upload/，则可以如下获取：
        File upload = new File(path.getAbsolutePath(), "assets/common/upload/");
        if (!upload.exists()){
            upload.mkdirs();
        }
        return upload;
    }


    /**
     * 查询文件信息
     * @param id
     * @return
     */
    public Page<Map<String, Object>> getFiles(String id){
        Page page = LayuiPageFactory.defaultPage();
        return this.baseMapper.selectFiles(page,id);
    }

    /**
     * 根据条件查询
     * @param queryWrapper 条件
     * @return
     */
    public List<FileInfo> selectFileInfo(QueryWrapper<FileInfo> queryWrapper) {
        return this.baseMapper.selectList(queryWrapper);
    }

    /**
     * 根据编号删除文件
     * @param id
     */
    public void delFile(String id,String path){
        //删除
        try {
            boolean result = this.fileInfoService.deleteFilePath(path);
            if (result){
                System.out.println("文件删除成功！");
            }
            this.baseMapper.deleteById(id);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.err.println("删除文件错误！");
        }
    }

    /**
     * 添加文件
     * @param fileInfo
     */
    public void add(FileInfo fileInfo){
        this.save(fileInfo);
    }

    /**
     *  删除传入的路径的文件
     * @param path 文件位置
     */
    private boolean deleteFilePath(String path) throws FileNotFoundException {
        //获取当前项目的绝对路径
        File upload = uploadFile();
        // 存在就删除
        File fileIsExits = new File(upload.getAbsolutePath() + "//" + path);
        boolean result = false;
        System.out.println(fileIsExits.exists());
        if(fileIsExits.exists()) {
            result = fileIsExits.delete();
        }
        return result;
    }

    /**
     * 删除文件信息及文件
     * @param id
     */
    public void delFileInfo(String id){
        //根据id查询文件表的图片有多少个
        FileInfo fileInfo = this.fileInfoService.getById(id);
        this.fileInfoService.delFile(fileInfo.getFileId(),fileInfo.getFileName());
    }
}
