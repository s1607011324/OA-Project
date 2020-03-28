layui.use(['layer', 'admin', 'table', 'ax', 'upload'], function (key, value) {
    var $ = layui.jquery;
    var $ax = layui.ax;
    var admin = layui.admin;
    var table = layui.table;
    var upload = layui.upload;
    var getFile = {
        tableId: "getFileTable",    //表格id
        flag: Feng.getUrlParam("flag"),
        leaveId: Feng.getUrlParam("leaveId")
    };


    /**
     * 初始化表格数据
     * @param data
     * @returns {*[][]}
     */
    getFile.init = function (data) {
        console.log(data);
        return [[
            /* {field: 'id', hide: false,sort: true, title: '编号'},*/
            {field: 'fileData', sort: true, title: '文件', width: 180, templet: '#fileDataTpi'},
            {field: 'fileName', sort: true, title: '文件名'},
            {field: 'createTime', sort: true, title: '上传时间'},
            //{field: 'leaveId', sort: true, title: '修改时间'},
            {align: 'center', toolbar: '#tableBar', title: '操作', minWidth: 200}
        ]];
    };

    /**
     * 批量文件上传方法
     * @constructor
     */
    getFile.FileUploads = function () {
        admin.putTempData('formOk', false);
        top.layui.admin.open({
            type: 2,
            title: '文件上传',
            area: ['700px', '320px'],
            content: Feng.ctxPath + '/file/fileUploads?flag=' + getFile.leaveId,
            end: function () {
                admin.getTempData('formOk') && table.reload(getFile.tableId);
            }
        });
    };

    //修改
    getFile.edit = function (data) {

    };

    //删除
    getFile.del = function (data) {
        Feng.confirm("是否删除 此操作不可撤销?", function () {
            var ajax = new $ax(Feng.ctxPath + "/file/delInfo/" + data.fileId, function (data) {
                Feng.success("删除成功!");
                //刷新table
                table.reload(getFile.tableId);
            }, function (data) {
                Feng.error("删除失败!");
            });
            ajax.start();
        });
    };

    /**
     * 下载事件
     * @param data
     */
    getFile.downloads = function (data) {
        window.open(Feng.ctxPath + "/file/download?fileName=" + data.fileName, '_blank')
    };

    // 渲染表格
    var tableResult = table.render({
        elem: '#' + getFile.tableId,
        url: Feng.ctxPath + '/file/getFileInfos/' + Feng.getUrlParam("leaveId"),
        page: true,
        height: "full-60",
        cellMinWidth: 100,
        cols: getFile.init(),
        done: function (res, curr, count) { //表格数据加载完后的事件
            console.log(res.data.fileId);
            console.log(curr);
            console.log(count);
            if (getFile.flag==='0'){
                $("#fileUploads").hide();
                $("a[lay-event='del']").hide();
            }

            //图片弹出层
            layer.photos({//点击图片弹出
                photos: '.layer-photos-demo',
                shift: 5 //0-6的 选择，指定弹出图片动画类型，默认随机
            });
        }
    });


    // 按钮点击事件
    table.on('tool(' + getFile.tableId + ')', function (obj) {
        var data = obj.data;
        localStorage.setItem("fileId", data.fileId);
        var layEvent = obj.event;
        // 修改按钮点击事件
        if (layEvent === 'edit') {
            getFile.edit(data);
        } else if (layEvent === 'del') {
            getFile.del(data);
        } else if (layEvent === 'download') {
            getFile.downloads(data);
        }
    });

    $("#fileUploads").click(function () {
        getFile.FileUploads();
    });

    /*//假设这是iframe页
    var index = parent.layer.getFrameIndex(window.name); //先得到当前iframe层的索引
    layer.iframeAuto(index);*/
});