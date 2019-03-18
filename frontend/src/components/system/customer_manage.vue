<template xmlns:v-on="http://www.w3.org/1999/xhtml" xmlns:v-bind="http://www.w3.org/1999/xhtml" >
    <div >
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
                                        placeholder="选择日期"
                                        :picker-options="pickerOptions1">
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

                    <el-col :span="1" style="margin-left: 25px">
                        <el-button
		                        icon="el-icon-search"
		                        size="normal"
		                        type="primary"
		                        @click="search" >搜索
                        </el-button >
                    </el-col >

                    <el-button style="float: right;"
                               icon="el-icon-plus"
                               size="normal"
                               type="primary"
                               @click="handleAdd" >导入
                    </el-button >

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
                                label="预约日期" >
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

<!--                        <el-table-column
                                align="center"
                                label="操作"
                                width="200" >
                            <template scope="scope" >
                                <el-button
		                                size="small"
                                        icon="el-icon-edit"
                                        type="primary"
		                                @click="handleEdit(scope.$index, scope.row)" >编辑
                                </el-button >
                                <el-button
		                                size="small"
		                                type="danger"
                                        icon="el-icon-delete"
		                                :disabled="scope.row.account=='admin'"
		                                @click="handleDelete(scope.$index, scope.row)" >删除
                                </el-button >
                            </template >
                        </el-table-column >-->

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

      <el-dialog title="导入Excel文件" :visible.sync="addDialogVisible" width="40%">
            <el-upload
                    class="upload-demo"
                    ref="upload"
                    action="http://localhost:9090/visitor/info/add"
                    name="multipartFile"
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

<!--
        <el-dialog title="编辑公司" :visible.sync="modifyDialogVisible" width="35%">
            <el-form :model="modifyForm" >
                <el-col :span="24" >
                    <el-form-item label="公司名称：" :label-width="formLabelWidth">
                        <el-input v-model="modifyForm.customerName" @change="onChange" :disabled="modifyForm.account == 'admin'" ></el-input >
                    </el-form-item>
                </el-col>
            </el-form >
            <el-alert v-if="isError" style="margin-top: 10px;padding: 5px;"
                      :title="errorMsg"
                      type="error"
                      :closable="false"
                      show-icon >
            </el-alert >
            <div class="dialog-footer" style="margin-top: 30px" >
                <el-button @click="" icon="el-icon-close" type="danger">取 消</el-button >
                <el-button type="primary" @click="onEidt" icon="el-icon-check">确 定</el-button >
            </div >
        </el-dialog >

        <el-dialog title="提示" :visible.sync="deleteConfirmVisible"  width="30%">
            <span >确认要删除[ <b >{{selectedItem.customerName}}</b > ]吗？</span >
            <span slot="footer" class="dialog-footer" >
	    <el-button @click="deleteConfirmVisible = false" icon="el-icon-close" >取 消</el-button >
	    <el-button type="primary" @click="onConfirmDelete" icon="el-icon-check">确 定</el-button >
	  </span >
        </el-dialog >
-->

    </div >
</template >

<style>
    .upload-demo input{
        display: none;
    }
</style>

<script >
    var _this;

    export default {
        name: "part_manage",
        components: {},
        data () {
            _this = this;
            return {
                name:'',
                idCard:'',
                face_image:'',
                photo:'',
                pickerOptions1: {
                    disabledDate(time) {
                        return time.getTime() > Date.now();
                    }
                },
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
                formLabelWidth: '100px',

                filters: {
                    chooseTime:"",
                    status:""
                },
                loadingUI: false,
            }
        },
        methods: {
            openSuccess() {
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
                _this.$notify({
                    dangerouslyUseHTMLString: true,
                    message:
                    '<span>姓名：</span><strong>'+_this.name+'</strong></br>' +
                    '<span>身份证：</span><strong>'+_this.idCard+'</strong></br>' +
                    '<span>身份证照：</span><img src='+_this.photo+'></br>'+
                    '<span>现场照片：</span><img style="margin-top: 5px;width: 180px;height: 180px" src='+_this.face_image+'>' ,
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

                console.log('文件类型：'+file.type);
                console.log('文件大小：'+file.size+'B');

                const isXls = file.type === 'application/vnd.ms-excel';
                const isXlsx = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';

                if(!isXls && !isXlsx){
                    _this.$message.error('只能上传格式是 xls 或 xlxs 的Excel文件');
                }
                return  (isXls || isXlsx);

            },//文件上传前执行
            successUpload(response, file, fileList){
                if(response.data=='true'){
                    _this.$message.success('文件上传成功！');
                    _this.addDialogVisible = false;
                    _this.onSelectUsers();
                }else{
                    _this.$message.error(response.data);
                }
                _this.$refs.upload.clearFiles();
            },//文件上传成功方法
            errorUpload(err, file, fileList){
                _this.$message.error('文件上传失败，请检查服务器是否运行正常！');
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
                            _this.startRow = data.data.startRow;
                        }
                        _this.loadingUI = false;
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

    function onMessageArrived(message) {
        console.log("收到消息:" + message.payloadString);
        console.log("主题：" + message.destinationName);
        var data = null;
        try {
            data = jQuery.parseJSON(message.payloadString);
            console.log("解析出来的：data：" + JSON.stringify(data));
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
                    console.log("未知主题消息...")
                    break;
            }
        }
    }
</script >
<style >

</style >
