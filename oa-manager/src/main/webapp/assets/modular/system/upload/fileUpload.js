layui.use(['layer', 'form', 'table','laydate', 'admin', 'ax','upload',], function () {
    var $ = layui.$;
    var layer = layui.layer;
    var form = layui.form;
    var table = layui.table;
    var $ax = layui.ax;
    var laydate = layui.laydate;
    var admin = layui.admin;
    var upload = layui.upload;


    var flag=Feng.getUrlParam("flag");
    if (flag!==null){
        flags(Feng.ctxPath+'/file/uploadFileAdd/'+flag);
    }else {
        flags(Feng.ctxPath+'/leave/add/-1');
    }

    function flags(urlP) {
        console.log(urlP);
        //多文件列表示例
        var demoListView = $('#demoList'),
            uploadListIns = upload.render({
            elem: '.testList'
            ,url: urlP
            ,acceptMime: 'image/*'
            ,ext: 'jpg|png|gif|bmp|jpeg'
            ,multiple: true
            ,number:10
            ,auto: false
            ,bindAction: '#testListAction'
            ,choose: function(obj){
                //调用公共
                uploads(obj);
                $("#test10").hide();
            }
            ,done: function(res, index, upload){
                done(res,index,upload);
                //传给上个页面，刷新table用
                admin.putTempData('formOk', true);
                //关掉对话框
                admin.closeThisDialog();
            }
            ,error: function(index, upload){
                error(index,upload)
            }
        });

        //公共上传方法
        function uploads(obj) {
            var files = this.files = obj.pushFile(); //将每次选择的文件追加到文件队列
            //读取本地文件
            obj.preview(function(index, file, result){
                var tr = $(['<tr id="upload-'+ index +'">'
                    ,'<td>'+ file.name +'</td>'
                    ,'<td>'+ (file.size/1014).toFixed(1) +'kb</td>'
                    ,'<td>等待上传</td>'
                    ,'<td>'
                    ,'<button class="layui-btn layui-btn-xs demo-reload layui-hide">重传</button>'
                    ,'<button class="layui-btn layui-btn-xs layui-btn-danger demo-delete">删除</button>'
                    ,'</td>'
                    ,'</tr>'].join(''));

                //单个重传
                tr.find('.demo-reload').on('click', function(){
                    obj.upload(index, file);
                });


                //删除
                tr.find('.demo-delete').on('click', function(){
                    delete files[index]; //删除对应的文件
                    tr.remove();
                    if ($("#demoList").html()===''){
                        $("#test10").show();
                    }
                    uploadListIns.config.elem.next()[0].value = ''; //清空 input file 值，以免删除后出现同名文件不可选
                });


                demoListView.append(tr);
            });
        }

        function done(res, index, upload) {
            if(res.code === 0){ //上传成功
                var tr = demoListView.find('tr#upload-'+ index)
                    ,tds = tr.children();
                tds.eq(2).html('<span style="color: #5FB878;">上传成功</span>');
                tds.eq(3).html(''); //清空操作
                Feng.success("上传成功！");

                return delete this.files[index]; //删除文件队列已经上传成功的文件
            }
            this.error(index, upload);
        }

        function error(index, upload) {
            var tr = demoListView.find('tr#upload-'+ index)
                ,tds = tr.children();
            tds.eq(2).html('<span style="color: #FF5722;">上传失败</span>');
            tds.eq(3).find('.demo-reload').removeClass('layui-hide'); //显示重传
        }
    }

});

