layui.use(['layer', 'form', 'table','laydate', 'admin', 'ax'], function () {
    var $ = layui.$;
    var layer = layui.layer;
    var form = layui.form;
    var table = layui.table;
    var $ax = layui.ax;
    var laydate = layui.laydate;
    var admin = layui.admin;

    /**
     * 代办任务
     */
    var Task = {
        tableId: "taskTable",    //表格id
        /*condition: {
            startDate:"",
            endDate:"",
            status:$("#status").val()
        }*/
    };

    /**
     * 初始化表格的列
     */
    Task.initColumn = function () {
        return [[
            {type: 'checkbox'},
            /* {field: 'id', hide: false,sort: true, title: '编号'},*/
            {field: 'name', sort: true, title: '任务名称'},
            {field: 'initiator', sort: true, title: '申请人'},
            {field: 'title', sort: true, title: '任务标题'},
            {field: 'createTime', sort: true, title: '创建时间'},
            {align: 'center', toolbar: '#tableBar', title: '操作', minWidth: 200}
        ]];
    };

    /**
     * 查看流程跟踪
     * @param data
     * @constructor
     */
    Task.process_trace = function (data) {
        admin.putTempData('formOk', false);
        top.layui.admin.open({
            type: 2,
            title: '流程跟踪',
            area: ['900px', '500px'],
            content: Feng.ctxPath + '/task/flowImg/'+data.processInstanceId,
        });
    };

    /**
     * 执行待办任务
     */
    Task.execute_the_task = function (data) {
        admin.putTempData('formOk', false);
            top.layui.admin.open({
            type: 2,
            title: '执行任务',
            area: ['900px', '730px'],
            maxmin: true,
            content: Feng.ctxPath + '/task/task_execute?taskId='+data.id+"&&processInstanceId="+data.processInstanceId,
            end: function () {
                admin.getTempData('formOk') && table.reload(Task.tableId);
            }
        });
    };
    // 渲染表格
    var tableResult = table.render({
        elem: '#' + Task.tableId,
        url: Feng.ctxPath + '/task/commission',
        toolbar: '#toolbarDemo',//开启头部工具栏，并为其绑定左侧模板
        defaultToolbar: ['filter', 'exports', 'print', { //自定义头部工具栏右侧图标。如无需自定义，去除该参数即可
            title: '提示'
            ,layEvent: 'LAYTABLE_TIPS'
            ,icon: 'layui-icon-tips'
        }],
        page: true,
        height: "full-158",
        cellMinWidth: 100,
        cols: Task.initColumn()
    });

 /*   //日期时间选择器
    laydate.render({
        elem: '#startDate',
        type: 'date'
    });

    laydate.render({
        elem: '#endDate',
        type: 'date'
    });*/
/*
    // 添加按钮点击事件
    $('#btnAdd').click(function () {
        Leave.openAddLeave();
    });

    // 搜索按钮点击事件
    $('#btnSearch').click(function () {
        Leave.search();
    });*/

    // 按钮点击事件
    table.on('tool(' + Task.tableId + ')', function (obj) {
        var data = obj.data;
        var layEvent = obj.event;
        if (layEvent === 'process_trace') {
            Task.process_trace(data);
        } else if (layEvent === 'execute_the_task') {
            Task.execute_the_task(data);
        }
    });

   /* // 修改按钮点击事件
    $('#btnSearch').click(function () {
    });*/



});

