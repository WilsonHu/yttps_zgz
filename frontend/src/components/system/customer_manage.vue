<template xmlns:v-on="http://www.w3.org/1999/xhtml" xmlns:v-bind="http://www.w3.org/1999/xhtml" >
    <div >
        <audio id="error"  src="./src/assets/audio/error.mp3"></audio>
        <audio id="success"  src="./src/assets/audio/success.mp3"></audio>
        <el-col :span="3" style="margin-bottom: 10px">
            <strong >今日预约人数：{{visitor}}</strong>
        </el-col>
        <el-col :span="3"  style="margin-bottom: 10px">
            <strong  >未访人数：{{status0}}</strong>
        </el-col>
        <el-col :span="3"  style="margin-bottom: 10px" >
            <strong >来访人数：{{status1}}</strong>
        </el-col>
        <el-col class="well well-lg" style="background-color: white;">
            <el-row>
                <el-col>
                    <el-form :model="filters" label-position="right" label-width="80px">
                        <el-col :span="5">
                            <el-form-item label="日期:">
                                <el-date-picker
                                        v-model="filters.chooseTime"
                                        align="right"
                                        type="date"
                                        format="yyyy-MM-dd"
                                        placeholder="选择日期" >
                                </el-date-picker>
                            </el-form-item>
                        </el-col>
                        <el-col :span="5">
                            <el-form-item label="来访状态:" >
                                <el-select v-model="filters.status" clearable placeholder="请选择">
                                    <el-option label="已来访" value="1"></el-option>
                                    <el-option label="未来访" value="0"></el-option>
                                </el-select>
                            </el-form-item>
                        </el-col>
                    </el-form>

                    <el-col :span="4" >
                        <el-button
		                        icon="el-icon-search"
		                        size="normal"
		                        type="primary"
		                        @click="search" >搜索
                        </el-button >
                    </el-col >
                    <el-col :span="4" :offset="6">
                        <el-button
                                   icon="el-icon-download"
                                   size="normal"
                                   type="primary"
                                   @click="handleAdd" >导入
                        </el-button >

                        <el-button style="float: right;"
                                   icon="el-icon-upload2"
                                   size="normal"
                                   type="primary"
                                   @click="exportRecord=true" >导出
                        </el-button >
                    </el-col>
                    <el-table
		                    v-loading="loadingUI"
		                    element-loading-text="获取数据中..."
		                    :data="tableData"
		                    border
		                    style="width: 100%;" >
                        <el-table-column
		                        width="75"
                                align="center"
		                        label="序号" >
                            <template scope="scope">
                                {{scope.$index+startRow}}
                            </template>
                        </el-table-column>
                        <el-table-column
                                width="200"
                                align="center"
                                prop="name"
                                label="姓名">
                        </el-table-column>
                        <el-table-column
                                align="center"
                                prop="idcard"
                                label="身份证">
                        </el-table-column>
                        <el-table-column
                                align="center"
                                prop="company"
                                label="公司名称">
                        </el-table-column>
                        <el-table-column
                                align="center"
                                prop="datetime"
                                width="300"
                                label="到访日期" >
                        </el-table-column >
                        <el-table-column
                                align="center"
                                prop="status"
                                width="200"
                                label="是否来访" >
                            <template scope="scope">
                                <div style="color: red" v-if="scope.row.status==0">未来访</div>
                                <div style="color: green" v-if="scope.row.status==1">已来访</div>
                            </template>
                        </el-table-column >

                      <el-table-column
                                align="center"
                                label="操作"
                                width="200" >
                            <template scope="scope" >
                                <el-button
		                                size="small"
		                                type="danger"
                                        icon="el-icon-delete"
		                                :disabled="scope.row.account=='admin'"
		                                @click="handleDelete(scope.$index, scope.row)" >删除
                                </el-button >
                            </template >
                        </el-table-column >

                    </el-table >
                    <div class="block" style="text-align: center; margin-top: 20px" >
                        <el-pagination
                                background
		                        @current-change="handleCurrentChange"
		                        :current-page="currentPage"
		                        :page-size="pageSize"
		                        layout="total, prev, pager, next, jumper"
		                        :total="totalRecords" >
                        </el-pagination >
                    </div >
                </el-col >
            </el-row >
        </el-col >
        <el-dialog  :visible.sync="addDialogVisible" width="500px">
            <el-upload
                    class="upload-demo"
                    ref="upload"
                    name="multipartFile"
                    :action="fileURL"
                    :limit="1"
                    :auto-upload="false"
                    :before-upload="beforeAvatarUpload"
                    :on-success="successUpload"
                    :on-error="errorUpload"
                    :on-exceed="exceedFile">
                <el-button slot="trigger" size="small" type="primary">选取文件</el-button>
            </el-upload>
            <div  class="dialog-footer" style="margin-top: 30px" >
                <el-button @click="handleRemove" icon="el-icon-close" type="danger">取 消</el-button >
                <el-button type="success" @click="submitUpload" icon="el-icon-check">确 定</el-button >
            </div >
        </el-dialog >
        <el-dialog title="提示" :visible.sync="deleteConfirmVisible"  width="30%">
            <span >确认要删除[ <b >{{selectedItem.name}}</b > ]吗？</span >
            <div  class="dialog-footer" style="margin-top: 30px" >
	            <el-button   @click="deleteConfirmVisible = false" icon="el-icon-close" type="danger">取 消</el-button >
	            <el-button type="primary" @click="onConfirmDelete" icon="el-icon-check">确 定</el-button >
            </div >
        </el-dialog >
        <el-dialog title="请选择导出范围" :visible.sync="exportRecord"  width="25%">
            <span  class="dialog-footer" >
	            <el-button type="success" @click="exExecle('员工')" >员 工</el-button >
	            <el-button type="danger" @click="exExecle('访客')" >访 客</el-button >
                <el-button type="primary" @click="exExecle('')" >全 部</el-button >
	        </span >
        </el-dialog >
    </div >
</template >

<script >
    var _this;

    export default {
        name: "part_manage",
        components: {},
        data () {
            _this = this;
            return {
                fileURL:FileURL,
                name:'',
                idCard:'',
                face_image:'',
                photo:'',
               // isError: false,
               // errorMsg: '',
                totalRecords: 0,
                selectedItem: {},
                deleteConfirmVisible: false,
                tableData: [],
                //分页
                pageSize: EveryPageNum,//每一页的num
                currentPage: 1,
                startRow: 1,

                //增加对话框
                addDialogVisible: false,
                form: {
                    createTime:"",
                    customerName: "",
                },
                filters: {
                    chooseTime:new Date(),
                    status:""
                },
                loadingUI: false,
                status0: 0,
                status1: 0,
                visitor: 0,
                exportRecord:false
            }
        },
        methods: {
            exExecle(data) {
                $.ajax({
                    url: HOST + "excel/exportRecord",
                    type: "POST",
                    dataType: "json",
                    data: {"chooseTime":_this.filters.chooseTime,"identity":data},
                    success: function (data) {
                        if (data.code == 200) {
                            console.log(data.data);
                            var a = document.createElement("a");
                            a.setAttribute("href",data.data);
                            a.setAttribute("target","_blank");
                            a.click();
                            //exportRecord(data.data);
                         }
                    },
                    error: function (data) {
                        showMessage(_this, '服务器访问出错', 0);
                    }
                });
                _this.exportRecord=false;
            },
            audioError(){
                document.getElementById('error').play();
            },
            audioSuccess(){
                document.getElementById('success').play();
            },
            openSuccess() {
                _this.audioSuccess();
                _this.search();
                _this.$notify({
                    dangerouslyUseHTMLString: true,
                    message:
                    '<span>姓名：</span><strong>'+_this.name+'</strong></br>' +
                    '<span>身份证：</span><strong>'+_this.idCard+'</strong></br>' +
                    '<span>身份证照：</span><img src='+_this.photo+'></br>',
                    type: 'success',
                    duration:3000,
                    position: 'top-right',
                    offset: 50
                });
            },
            openError() {
                _this.audioError();
                _this.$notify({
                    dangerouslyUseHTMLString: true,
                    message:
                    '<span>姓名：</span><strong>'+_this.name+'</strong></br>' +
                    '<span>身份证：</span><strong>'+_this.idCard+'</strong></br>' +
                    '<img src='+_this.photo+'></br>'+
                    '<img style="margin-top: 5px;width: 260px;height: 260px" src='+_this.face_image+'>' ,
                    type: 'error',
                    duration: 0,
                    position: 'top-left',
                    offset: 50
                });
            },
            submitUpload() {
                this.$refs.upload.submit();
            },//开始上传文件
            beforeAvatarUpload(file){

                //console.log('文件类型：'+file.type);
                //console.log('文件大小：'+file.size+'B');

                const isXls = file.type === 'application/vnd.ms-excel';
                const isXlsx = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';

                if(!isXls && !isXlsx){
                    _this.$message.error('只能上传格式是 xls 或 xlxs 的Excel文件');
                }
                return  (isXls || isXlsx);

            },//文件上传前执行
            successUpload(response, file, fileList){
                var data=JSON.parse(response.data);
                if(data.code==200){
                    _this.$message.success(data.message);
                    _this.addDialogVisible = false;
                    _this.onSelectUsers();
                }else{
                    _this.$message.error(data.message);
                }
                _this.$refs.upload.clearFiles();
            },//文件上传成功方法
            errorUpload(err, file, fileList){
                _this.$message.error('上传失败，请检查服务器是否运行正常！');
            },//文件上传失败
            exceedFile(){
                _this.$message.error('每次只能上传一个文件！');
            },//选择文件数量超过
            handleRemove() {
                _this.addDialogVisible = false;
                this.$refs.upload.clearFiles();
            },//取消按钮
            handleCurrentChange(val) {
                this.currentPage = val;
                this.onSelectUsers();
            },
            search() {
                _this.onSelectUsers();
            },//搜索
            onSelectUsers() {
                _this.onVisitorCount();
                _this.tableData = new Array();
                _this.loadingUI = true;
                _this.filters.page = _this.currentPage;
                _this.filters.size = _this.pageSize;
                $.ajax({
                    url: HOST + "/visitor/info/list",
                    type: 'POST',
                    dataType: 'json',
                    data: _this.filters,
                    success: function (data) {
                        if (data.code == 200) {
                            _this.totalRecords = data.data.total;
                            _this.tableData = data.data.list;
                        }
                        _this.loadingUI = false;
                        _this.startRow = data.data.startRow;
                    },
                    error: function (data) {
                        showMessage(_this, '服务器访问出错', 0);
                        _this.loadingUI = false;
                    }
                })
            },//查询
            handleAdd() {
               // this.isError = false;
               // this.errorMsg = '';
                this.addDialogVisible = true;
            },//显示窗口
            handleDelete(index, item) {
                this.selectedItem = copyObject(item);
                if (this.selectedItem) {
                    _this.deleteConfirmVisible = true;
                }
            },
            onConfirmDelete: function () {
                _this.deleteConfirmVisible = false;
                $.ajax({
                    url: HOST + "visitor/info/delete",
                    type: 'POST',
                    dataType: 'json',
                    data: {"id":this.selectedItem.id},
                    success: function (data) {
                        if (data.data == true) {
                            _this.onSelectUsers();
                            showMessage(_this, '删除成功', 1);
                        } else {
                            showMessage(_this, '删除失败', 0);
                        }
                    },
                    error: function (data) {
                        showMessage(_this, '服务器访问出错', 0);
                    }
                })
            },
            onVisitorCount: function () {
                _this.deleteConfirmVisible = false;
                $.ajax({
                    url: HOST + "visitor/info/count",
                    type: 'POST',
                    dataType: 'json',
                    data: {"chooseTime":_this.filters.chooseTime},
                    success: function (data) {

                        var  data=JSON.parse(data.data)
                        _this.status0=data.status0;
                        _this.status1=data.status1;
                        _this.visitor=data.visitor;
                    },
                    error: function (data) {
                        showMessage(_this, '服务器访问出错', 0);
                    }
                })
            }
            },
        computed: {},
        filters: { },
        created: function () {
            this.userinfo = JSON.parse(sessionStorage.getItem('user'));
            if (isNull(this.userinfo)) {
                this.$router.push({path: '/login'});
                return;
            }
        },
        mounted: function () {
            this.onSelectUsers();
            this.onVisitorCount();
        },
    }

    var mqttReconnectInterval = null;
    var hostname = MqttServer,
        port = ServerPort,
        clientId = `client`,
        timeout = 30,
        keepAlive = 100,
        cleanSession = false,
        ssl = false;
    //userName = 'admin',
    //password = 'password';
    var client = new Paho.MQTT.Client(hostname, port, clientId);
    //建立客户端实例
    var options = {
        invocationContext: {
            host: hostname,
            port: port,
            path: client.path,
            clientId: clientId
        },
        timeout: timeout,
        keepAliveInterval: keepAlive,
        cleanSession: cleanSession,
        useSSL: ssl,
        //userName: userName,
        //password: password,
        onSuccess: onConnect,
        onFailure: function (e) {
            console.log(`connect failure: ${e}`);
        },
    };
    $(document).ready(function () {
        client.connect(options);//连接服务器并注册连接成功处理事件
        client.onConnectionLost = onConnectionLost;//注册连接断开处理事件
        client.onMessageArrived = onMessageArrived;//注册消息接收处理事件
    });

    function onConnect() {
        console.log("connect successfully");
        if (mqttReconnectInterval != null) {
            clearInterval(mqttReconnectInterval);
            mqttReconnectInterval = null;
        }
        for (let item of ServerTOPIC)//订阅主题
        {
            console.log(`subscribed server topic: ${item}`);
            client.subscribe(item);
        }
    }

    function onConnectionLost(responseObject) {
        if (responseObject.errorCode !== 0) {
           console.log("连接已断开");
           console.log("onConnectionLost:" + responseObject.errorMessage);
            mqttReconnectInterval = setInterval(() => {
                client.connect(options);
                client.onConnectionLost = onConnectionLost;//注册连接断开处理事件
                client.onMessageArrived = onMessageArrived;//注册消息接收处理事件
            }, 2000);
        }
    }

    function exportRecord(data) {
        var $form = $("<form>"); //定义一个form表单
        $form.hide().attr({target:'',method:'post','action': data});
        var $input = $("<input>");
        $input.attr({"type":"hidden","name":'req'}).val(req);
        $form.append($input).appendTo($("body")).submit().remove();
    }

    function onMessageArrived(message) {
       // console.log("收到消息:" + message.payloadString);
      //  console.log("主题：" + message.destinationName);
        var data = null;
        try {
            data = jQuery.parseJSON(message.payloadString);
           // console.log("解析出来的：data：" + JSON.stringify(data));
        } catch (e) {
            console.log(e);
        }
        if (data != null) {
            _this.name=data.name;
            _this.idCard=data.id_num;
            _this.photo="data:image/jpeg;base64,"+data.photo;
            switch (message.destinationName) {
                case ServerTOPIC[0]: //visitor/error
                    _this.face_image="data:image/jpeg;base64,"+data.face_image;
                    _this.openError();
                    break;
                case ServerTOPIC[1]://visitor/success
                    _this.openSuccess();
                    break;
                default:
                    //console.log("未知主题消息...")
                    break;
            }
        }
    }
</script >

<style>
    .upload-demo input{
        display: none;
    }

</style>
