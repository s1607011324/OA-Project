layui.use(['layer', 'form', 'admin', 'laydate', 'layedit', 'ax'], function () {
    var $ = layui.jquery;
    var $ax = layui.ax;
    var form = layui.form;
    var admin = layui.admin;
    var laydate = layui.laydate;
    var layer = layui.layer;
    var flags = Feng.getUrlParam("flag");
    //备注富文本编辑器
    var layedit = layui.layedit;
    var Note_index = layedit.build('note', {
        height: "160px",
        hideTool:[
            'strong' //加粗
            ,'italic' //斜体
            ,'underline' //下划线
            ,'del' //删除线
            ,'|' //分割线
            ,'left' //左对齐
            ,'center' //居中对齐
            ,'right' //右对齐
            ,'link' //超链接
            ,'unlink' //清除链接
            ,'face' //表情
            ,'image'] //插入图片
    }); //建立编辑器



    //获取请假信息
    var ajax = new $ax(Feng.ctxPath + "/leave/leave_get/" + Feng.getUrlParam("leaveId"));
    var result = ajax.start();
    //表单中input赋值
    form.val('leaveForm', result.data);

    /**
     * 公共时间方法
     */
    function dates() {
        $("#startDate").val(/\d{4}-\d{1,2}-\d{1,2}/g.exec(result.data.startDate));
        $("#endDate").val(/\d{4}-\d{1,2}-\d{1,2}/g.exec(result.data.endDate));
    }
    var FilesInfoDlg = {
        data: {
            id: $("#leaveId").val(),
        }
    };
    FilesInfoDlg.FileUploads = function () {
        admin.putTempData('formOk', false);
        top.layui.admin.open({
            type: 2,
            title: '查看附件',
            area: ['1150px','650px'],
            maxmin: true,
            content: Feng.ctxPath + '/file/getFileInfo?flag='+flags+'&&leaveId='+FilesInfoDlg.data.id,
        });
    };

    $("#fileUploads").click(function () {
        FilesInfoDlg.FileUploads();
    });

    if (flags==="1"){
        // 让当前iframe弹层高度适应
        var index = parent.layer.getFrameIndex(window.name); //先得到当前iframe层的索引
        layer.iframeAuto(index);
        Note_index = layedit.build('note',{
            height: "160px",
            tool:[
                'strong' //加粗
                ,'italic' //斜体
                ,'underline' //下划线
                ,'del' //删除线
                ,'|' //分割线
                ,'left' //左对齐
                ,'center' //居中对齐
                ,'right' //右对齐
                ,'link' //超链接
                ,'unlink' //清除链接
                ,'face' //表情
                ,'image'],//插入图片
            uploadImage:{url:Feng.ctxPath+'/file/uploadImg',type:'post'}
        });
        // 渲染时间选择框
        laydate.render({
            elem: '#startDate',
            type:'date',
            min: new Date().toDateString(), //最小日期
            start: new Date().toDateString(),  //开始日期
            done: function (value, date, endDate) {
                var startDate = new Date(value).getTime();
                var NowDate = new Date().getTime();
                if (startDate<NowDate){
                    Feng.error('开始时间不能小于当前时间');
                    $('#startDate').val("");
                }
                var endTime = new Date($('#endDate').val()).getTime();
                if (endTime <= startDate) {
                    Feng.error('结束时间不能小于开始时间');
                    $('#startDate').val("");
                }
            }
        });

        // 渲染时间选择框
        laydate.render({
            elem: '#endDate',
            type:'date',
            min: new Date().toDateString(), //最小日期
            start: new Date().toDateString(),  //开始日期
            done: function (value, date, endDate) {
                var startDate = new Date($('#startDate').val()).getTime();
                var endTime = new Date(value).getTime();
                var NowDate = new Date().getTime();
                if (endTime<NowDate){
                    Feng.error('结束时间不能小于当前时间');
                    $('#endDate').val("");
                }
                if (endTime <= startDate) {
                    Feng.error('结束时间不能小于开始时间');
                    $('#endDate').val("");
                }
            }
        });
        /*内容可编辑。此行必须在setContent函数前修改属性，否则属性设置不成功*/
        $("#LAY_layedit_1").contents().find("body[contenteditable]").prop("contenteditable", true);//jquery获取iframe里的内容
        layedit.setContent(Note_index, result.data.note);
        dates();
    }else {
        /*内容不可编辑。此行必须在setContent函数前修改属性，否则属性设置不成功*/
        $("#LAY_layedit_1").contents().find("body[contenteditable]").prop("contenteditable", false);//jquery获取iframe里的内容
        layedit.setContent(Note_index, result.data.note);
        dates();
        return
    }
    // 表单提交事件
    form.on('submit(btnSubmit)', function (data) {
        //获取领导审批意见
        var note= layedit.getContent(Note_index);
        if (note !== undefined && note !== "") {
            var ajax = new $ax(Feng.ctxPath + "/leave/edit", function (data) {
                    Feng.success("修改成功！");
                    //传给上个页面，刷新table用
                    admin.putTempData('formOk', true);
                    //关掉对话框
                    admin.closeThisDialog();
                }
                ,function (data) {
                    Feng.error("修改失败！")
                }
            );
            ajax.set(data.field);
            ajax.set({'note': note});
            ajax.start();
        } else {
            Feng.error("请输入备注信息！");
            return false;
        }
    });
});