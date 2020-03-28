/**
 * 请假申请详情对话框
 */
layui.use(['layer', 'form', 'admin', 'laydate', 'ax','layedit'], function () {
    var $ = layui.jquery;
    var $ax = layui.ax;
    var form = layui.form;
    var admin = layui.admin;
    var laydate = layui.laydate;

    var layer = layui.layer;
    var layedit = layui.layedit;

    //假设这是iframe页
    var indexs = parent.layer.getFrameIndex(window.name); //先得到当前iframe层的索引
    layer.iframeAuto(indexs);

    var index = layedit.build('note',{
        height:"160px",
        uploadImage:{url:Feng.ctxPath+'/file/uploadImg',type:'post'}
    }); //建立编辑器

    var LeaveInfoDlg = {
        data: {
            uid: $("#uid").val(),
            userName: ""
        }
    };
    LeaveInfoDlg.FileUploads = function () {
        admin.putTempData('formOk', false);
        top.layui.admin.open({
            type: 2,
            title: '文件上传',
            area: ['700px', '320px'],
            content: Feng.ctxPath + '/file/fileUploads',
        });
    };

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

    // 表单提交事件
    form.on('submit(btnSubmit)', function (data) {
        var note = layedit.getContent(index);
        var ajax = new $ax(Feng.ctxPath + "/leave/add/-2", function (data) {
            Feng.success("申请成功！");

            //传给上个页面，刷新table用
            admin.putTempData('formOk', true);

            //关掉对话框
            admin.closeThisDialog();
        }, function (data) {
            Feng.error("申请失败！" + data.responseJSON.message)
        });
        ajax.set(data.field);
        ajax.set({"note": note});
        ajax.start();
    });

    $("#fileUploads").click(function () {
       LeaveInfoDlg.FileUploads();
    })
});