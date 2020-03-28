layui.use(['layer', 'form', 'table','laydate', 'admin', 'ax'], function () {
    var $ = layui.$;
    var layer = layui.layer;
    var form = layui.form;
    var table = layui.table;
    var $ax = layui.ax;
    var laydate = layui.laydate;
    var admin = layui.admin;

    /**
     * 行政管理--请假申请
     */
    var Leave = {
        tableId: "leaveTable",    //表格id
        condition: {
            startDate:"",
            endDate:"",
            status:$("#status").val()
        }
    };

    /**
     * 初始化表格的列
     */
    Leave.initColumn = function () {
        var colors = '';
        if(Leave.condition.status==='0'){
            colors = 'color:#EE113D;';
        }
        if(Leave.condition.status==='1'){
            colors = 'color:#00CC7A;';
        }
        if(Leave.condition.status==='2'){
            colors = 'color:#5555FF;';
        }

        return [[
            {type: 'checkbox'},
            /* {field: 'id', hide: false,sort: true, title: '编号'},*/
            {field: 'title', sort: true, title: '标题'},
            {field: 'proposer', sort: true, title: '申请人'},
            {field: 'startDate', sort: true, title: '开始时间'},
            {field: 'endDate', sort:     true, title: '结束时间'},
            {field: 'dates', sort: true, title: '请假天数'},
            {field: 'statusName', sort: true, title: '状态',style:''+colors},
            {align: 'center', toolbar: '#tableBar', title: '操作', minWidth: 200}
        ]];
    };
    /**
     * 审批
     *
     * @param data 点击按钮时候的行数据
     */
    Leave.LeaveApproval = function (data) {
        console.log(data);
        Feng.confirm("是否申报 此操作不可撤销?", function () {
            var ajax = new $ax(Feng.ctxPath + "/leave/leaveApproval/"+data.id, function (data) {
                Feng.success("申报成功!");
                //刷新table
                table.reload(Leave.tableId);

            }, function (data) {
                Feng.error("申报失败!");
            });
            ajax.start();
        });
    };

    //修改
    Leave.edit = function(data){
        admin.putTempData('formOk', false);
        top.layui.admin.open({
            type: 2,
            title: '修改请假信息',
            area: ['800px', '520px'],
            content: Feng.ctxPath + '/leave/leaveEdit?leaveId='+data.id+"&&flag=1",
            end: function () {
                admin.getTempData('formOk') && table.reload(Leave.tableId);
            }
        });
    };

    //删除
    Leave.del = function(data){
        Feng.confirm("是否删除 此操作不可撤销?", function () {
            var ajax = new $ax(Feng.ctxPath + "/leave/del/"+data.id, function (data) {
                Feng.success("删除成功!");
                //刷新table
                table.reload(Leave.tableId);
            }, function (data) {
                Feng.error("删除失败!");
            });
            ajax.start();
        });
    };

    /**
     * 查看请假信息
     * @param data
     */
    Leave.leaveSelect = function (data) {
        admin.putTempData('formOk', false);
        top.layui.admin.open({
            type: 2,
            title: '查看请假信息',
            area: ['800px', '430px'],
            content: Feng.ctxPath + '/leave/leaveInfos?leaveId='+data.id+"&&flag=0",
            end: function () {
                admin.getTempData('formOk') && table.reload(Leave.tableId);
            }
        });
    };

    /**
     * 点击查询按钮
     */
    Leave.search = function () {
        var queryData = {};
        queryData['startDate'] = $("#startDate").val();
        queryData['status'] = $("#status").val();
        queryData['endDate'] = $("#endDate").val();
        table.reload(Leave.tableId, {where: queryData});
    };

    /**
     * 弹出添加请假对话框
     */
    Leave.openAddLeave = function () {
        admin.putTempData('formOk', false);
            top.layui.admin.open({
            type: 2,
            title: '请假申请',
            area: ['800px', '570px'],
            maxmin: true,
            content: Feng.ctxPath + '/leave/leave_add',
            end: function () {
                admin.getTempData('formOk') && table.reload(Leave.tableId);
            }
        });
    };


    // 渲染表格
    var tableResult = table.render({
        elem: '#' + Leave.tableId,
        url: Feng.ctxPath + '/leave/list/'+Leave.condition.status,
        toolbar: '#toolbarDemo',//开启头部工具栏，并为其绑定左侧模板
        defaultToolbar: ['filter', 'exports', 'print', { //自定义头部工具栏右侧图标。如无需自定义，去除该参数即可
            title: '提示'
            ,layEvent: 'LAYTABLE_TIPS'
            ,icon: 'layui-icon-tips'
        }],
        page: true,
        height: "full-158",
        cellMinWidth: 100,
        cols: Leave.initColumn()
    });

    //日期时间选择器
    laydate.render({
        elem: '#startDate',
        type: 'date'
    });

    laydate.render({
        elem: '#endDate',
        type: 'date'
    });

    // 添加按钮点击事件
    $('#btnAdd').click(function () {
        Leave.openAddLeave();
    });

    // 搜索按钮点击事件
    $('#btnSearch').click(function () {
        Leave.search();
    });

    // 按钮点击事件
    table.on('tool(' + Leave.tableId + ')', function (obj) {
        var data = obj.data;
        var layEvent = obj.event;
        // 修改按钮点击事件
        if (layEvent === 'edit') {
            Leave.edit(data);
        } else if (layEvent === 'del') {
            Leave.del(data);
        } else if (layEvent === 'leaveApproval') {
            Leave.LeaveApproval(data);
        }else if (layEvent === 'leaveSelect') {
            Leave.leaveSelect(data);
        }
    });
});

