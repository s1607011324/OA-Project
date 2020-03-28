layui.use(['layer', 'form', 'admin', 'laydate', 'layedit', 'ax'], function () {
    var $ = layui.jquery;
    var $ax = layui.ax;
    var form = layui.form;
    var admin = layui.admin;
    var laydate = layui.laydate;
    var layer = layui.layer;

    // 让当前iframe弹层高度适应
    admin.iframeAuto();
    var layedit = layui.layedit;
    //领导意见富文本编辑器
    var index = layedit.build('approval_opinion', {
        height: "200px",
        uploadImage:{url:Feng.ctxPath+'/file/uploadImg',type:'post'}
    }); //建立编辑器
    //备注富文本编辑器
    var Note_index = layedit.build('note', {
        height: "80px",
        hideTool: [
            'strong' //加粗
            , 'italic' //斜体
            , 'underline' //下划线
            , 'del' //删除线
            , '|' //分割线
            , 'left' //左对齐
            , 'center' //居中对齐
            , 'right' //右对齐
            , 'link' //超链接
            , 'unlink' //清除链接
            , 'face' //表情
            , 'image'] //插入图片
    }); //建立编辑器
    //上个领导的意见
    var approval_opinions = layedit.build('approval_opinions', {
        height: "120px",
        hideTool: [
            'strong' //加粗
            , 'italic' //斜体
            , 'underline' //下划线
            , 'del' //删除线
            , '|' //分割线
            , 'left' //左对齐
            , 'center' //居中对齐
            , 'right' //右对齐
            , 'link' //超链接
            , 'unlink' //清除链接
            , 'face' //表情
            , 'image'] //插入图片
    }); //建立编辑器
    layedit.sync(index);

    //获取请假信息
    var ajax = new $ax(Feng.ctxPath + "/task/getTask_execute/" + Feng.getUrlParam("taskId") + "/" + Feng.getUrlParam("processInstanceId"));
    var result = ajax.start();

    /*内容不可编辑。此行必须在setContent函数前修改属性，否则属性设置不成功*/
    $("#LAY_layedit_2").contents().find("body[contenteditable]").prop("contenteditable", false);//jquery获取iframe里的内容
    layedit.setContent(Note_index, result.data.note);
    var apps = "领导们审批意见";
    for (var i = 0; i < result.data.approval_opinions.length; i++) {
        apps += "<br>-----------------------------------------------------------------------------<br>" +
            "任务名称：" + result.data.approval_opinions[i].taskName + "<br>领导意见：" + result.data.approval_opinions[i].comment;
    }
    layedit.setContent(approval_opinions, apps);
    var app = layedit.getContent(approval_opinions);
    if (app === "" || app === "领导们审批意见") {
        $("#opinions").hide();
    } else {
        $("#LAY_layedit_3").contents().find("body[contenteditable]").prop("contenteditable", false);//jquery获取iframe里的内容
    }
    //调整任务
    //获取领导审批
    var approval_opinion = "";
    if (result.data.TaskName === "调整申请") {
        $("input:radio[title='同意']").attr("title", "重新申请").val("重新申请");
        $("input:radio[title='不同意']").attr("title", "撤销申请").val("撤销申请");
        //领导审批意见富文本编辑器隐藏
        $("#xiaojia").hide();
        //给富文本编辑器从新赋值
        approval_opinion = layedit.setContent(index, "调整申请");
    } else if (result.data.TaskName === "销假") {
        //上级领导审批意见富文本编辑器隐藏
        $("#lingdao").hide();
        //领导审批意见富文本编辑器隐藏
        $("#xiaojia").hide();
        //给富文本编辑器从新赋值
        approval_opinion = layedit.setContent(index, "销假");
        //取消按钮隐藏
        $("button:button[ew-event='closeDialog']").hide();
        //确定按钮改成销假
        $("button:submit[lay-filter='btnSubmit']").html("销假");
    }

    //表单中input赋值
    form.val('taskForm', result.data);

    /**
     * 公共时间方法
     */
    $("#startDate").val(/\d{4}-\d{1,2}-\d{1,2}/g.exec(result.data.startDate));
    $("#endDate").val(/\d{4}-\d{1,2}-\d{1,2}/g.exec(result.data.endDate));

    var FilesInfo = {
        data: {
            id: $("#leaveId").val(),
        }
    };
    FilesInfo.FileUploads = function () {
        admin.putTempData('formOk', false);
        top.layui.admin.open({
            type: 2,
            title: '查看附件',
            area: ['1500px','750px'],
            maxmin: true,
            content: Feng.ctxPath + '/file/getFileInfo?flag=0&&leaveId='+FilesInfo.data.id,
        });
    };

    $("#fileUploads").click(function () {
        FilesInfo.FileUploads();
    });



    // 表单提交事件
    form.on('submit(btnSubmit)', function (data) {
        //获取领导审批意见
        approval_opinion = layedit.getContent(index);
        if (approval_opinion !== undefined && approval_opinion !== "") {
            var ajax = new $ax(Feng.ctxPath + "/task/execute/" + result.data.taskId, function (data) {
                    Feng.success("执行成功！");
                    //传给上个页面，刷新table用
                    admin.putTempData('formOk', true);
                    //关掉对话框
                    admin.closeThisDialog();
                }
                , function (data) {
                    Feng.error("执行失败！")
                }
            );
            ajax.set(data.field);
            ajax.set({'approval_opinion': approval_opinion});
            ajax.start();
        } else {
            Feng.error("请输入审批意见！");
            return false;
        }
    });
});