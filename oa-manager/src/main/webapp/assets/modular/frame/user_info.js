layui.use(['form', 'upload', 'element', 'ax', 'laydate'], function () {
    var $ = layui.jquery;
    var form = layui.form;
    var upload = layui.upload;
    var element = layui.element;
    var $ax = layui.ax;
    var laydate = layui.laydate;

    //渲染时间选择框
    laydate.render({
        elem: '#birthday'
    });

    //获取用户详情
    var ajax = new $ax(Feng.ctxPath + "/system/currentUserInfo",function (data) {
        console.log(data);
    });
    var result = ajax.start();

    //用这个方法必须用在class有layui-form的元素上
    form.val('userInfoForm', result.data);

    //表单提交事件
    form.on('submit(userInfoSubmit)', function (data) {
        var ajax = new $ax(Feng.ctxPath + "/mgr/edit", function (data) {
            Feng.success("修改成功!");
        }, function (data) {
            Feng.error("修改失败!" + data.responseJSON.message + "!");
        });
        ajax.set(data.field);
        ajax.start();
    });

    upload.render({
        elem: '#imgHead',
        url: Feng.ctxPath + '/system/uploadAvatar', // 上传接口
        done: function (res) {
            console.log(res);
            $("#imgHead").html("<img src=\""+Feng.ctxPath+eval(res).data.fileData+"\" alt=\"头像\"/>")
            localStorage.setItem("filePath",Feng.ctxPath+eval(res).data.fileData);

            Feng.success("上传成功!");
            // 上传完毕回调
        },
        error: function (res) {
            console.log("上传失败！"+res)
            // 请求异常回调
        }
    });
});