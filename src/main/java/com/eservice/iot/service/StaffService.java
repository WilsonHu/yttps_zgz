package com.eservice.iot.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eservice.iot.model.ResponseModel;
import com.eservice.iot.model.Staff;
import com.eservice.iot.model.VisitRecord;
import com.eservice.iot.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


/**
 * @author HT
 */
@Component
public class StaffService {

    private final static Logger logger = LoggerFactory.getLogger(StaffService.class);

    @Value("${park_base_url}")
    private String PARK_BASE_URL;

    @Autowired
    private RestTemplate restTemplate;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Token
     */
    private String token;
    /**
     * 员工列表
     */
    private ArrayList<Staff> staffList = new ArrayList<>();

    /**
     * 当天已签到员工列表
     */
    private ArrayList<VisitRecord> staffSignInList = new ArrayList<>();

    /**
     * 当天已签到VIP员工列表
     */
    private ArrayList<VisitRecord> vipSignInList = new ArrayList<>();

    @Autowired
    private TokenService tokenService;

    @Autowired
    private TagService tagService;

    private ThreadPoolTaskExecutor mExecutor;

    /**
     * 查询开始时间,单位为秒
     */
    private Long queryStartTime = 0L;

//    @Autowired
//    private MqttMessageHelper mqttMessageHelper;

    /**
     * 需要考勤的设备ID列表
     */
    private ArrayList<String> ATTENDANCE_DEVICE_LIST = new ArrayList<>();


    public StaffService() {
        //准备初始数据，此时获取到考勤列表后不去通知钉钉，初始化开始查询时间
        queryStartTime = Util.getDateStartTime().getTime() / 1000;
        ATTENDANCE_DEVICE_LIST.add("192.168.8.145");
    }

    /**
     * 每秒查询一次考勤信息
     */
    @Scheduled(fixedRate = 1000)
//    public void fetchSignInScheduled() {
//        ///当员工列表数为0，或者已全部签核完成,以及当前处于程序初始化状态情况下，可以跳过不再去获取考勤数据
//        boolean skip = staffList.size() <= 0 || tagService == null || !tagService.isTagInitialFinished();
//        if (skip) {
//            return;
//        }
//        if (token == null && tokenService != null) {
//            token = tokenService.getToken();
//        }
//        if (token != null) {
//            querySignInStaff(queryStartTime);
//        }
//    }

    /**
     * 每分钟获取一次员工信息
     */
    @Scheduled(fixedRate = 1000 * 60)
    public void fetchStaffScheduled() {
        if (token == null && tokenService != null) {
            token = tokenService.getToken();
        }
        if (token != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.ACCEPT, "application/json");
            headers.add("Authorization", token);
            HttpEntity entity = new HttpEntity(headers);
            try {
                ResponseEntity<String> responseEntity = restTemplate.exchange(PARK_BASE_URL + "/staffs", HttpMethod.GET, entity, String.class);
                if (responseEntity.getStatusCodeValue() == ResponseCode.OK) {
                    String body = responseEntity.getBody();
                    if (body != null) {
                        processStaffResponse(body);
                    } else {
                        fetchStaffScheduled();
                    }
                }
            } catch (HttpClientErrorException exception) {
                if (exception.getStatusCode().value() == ResponseCode.TOKEN_INVALID) {
                    token = tokenService.getToken();
                    if (token != null) {
                        fetchStaffScheduled();
                    }
                }
            }
        }
    }

    /**
     * 凌晨1点清除签到记录
     */
//    @Scheduled(cron = "0 0 1 * * ?")
//    public void resetStaffDataScheduled() {
//        logger.info("每天凌晨一点清除前一天签到记录：{}", formatter.format(new Date()));
//        if (staffSignInList != null & staffSignInList.size() > 0) {
//            staffSignInList.clear();
//        }
//
//        if (vipSignInList != null & vipSignInList.size() > 0) {
//            vipSignInList.clear();
//        }
//        //通过MQTT将员工签到信息发送至web端
//        logger.warn("Send message to client to clear sign in data!");
//        //mqttMessageHelper.sendToClient("staff/sign_in/reset", "{}");
//    }

    private void processStaffResponse(String body) {
        ResponseModel responseModel = JSONObject.parseObject(body, ResponseModel.class);
        if (responseModel != null && responseModel.getResult() != null) {
            ArrayList<Staff> tmpList = (ArrayList<Staff>) JSONArray.parseArray(responseModel.getResult(), Staff.class);
            boolean changed = false;
            if (tmpList != null && tmpList.size() != 0) {
                if (tmpList.size() != staffList.size()) {
                    changed = true;
                } else {
                    if (!tmpList.equals(staffList)) {
                        changed = true;
                    }
                }
                if (changed) {
                    logger.info("The number of staff：{} ==> {}", staffList.size(), tmpList.size());
                }
                staffList = tmpList;
            }
        }
    }

    private void processStaffSignInResponse(ArrayList<VisitRecord> tempList, boolean initial) {
        if (tempList != null && tempList.size() > 0) {
            ArrayList<VisitRecord> sendSignInList = new ArrayList<>();
            ArrayList<VisitRecord> sendVipList = new ArrayList<>();
            for (VisitRecord visitRecord : tempList) {
                List<String> tagList = visitRecord.getPerson().getTag_id_list();
                ///签到相关
                boolean needSignIn = false;
                if (tagList != null) {
                    for (int i = 0; i < tagList.size() && !needSignIn; i++) {
                        ///在考勤标签列表中
                        if (tagService.getSignInTagIdList().contains(tagList.get(i))) {
                            needSignIn = true;
                        }
                    }
                }
                if (needSignIn) {
                    boolean exit = false;
                    for (int i = 0; i < staffSignInList.size() && !exit; i++) {
                        if (staffSignInList.get(i).getPerson().getPerson_id().equals(visitRecord.getPerson().getPerson_id())) {
                            exit = true;
                        }
                    }
                    if (!exit) {
                        staffSignInList.add(visitRecord);
//                            if (!initial) {
//                                sendSignInList.add(visitRecord);
//                            }
                    }
                    if (!initial) {
                        sendSignInList.add(visitRecord);
                    }
                }

                ///VIP相关
                boolean isVIP = false;
                if (tagList != null) {
                    for (int i = 0; i < tagList.size() && !isVIP; i++) {
                        ///在VIP标签列表中
                        if (tagService.getVIPTagIdList().contains(tagList.get(i))) {
                            isVIP = true;
                        }
                    }
                }
                if (isVIP) {
                    boolean exit = false;
                    for (int i = 0; i < vipSignInList.size() && !exit; i++) {
                        if (vipSignInList.get(i).getPerson().getPerson_id().equals(visitRecord.getPerson().getPerson_id())) {
                            exit = true;
                        }
                    }
                    if (!exit) {
                        vipSignInList.add(visitRecord);
                    }
                    if (!initial) {
                        sendVipList.add(visitRecord);
                    }
                }
                //建立线程池发送钉钉
                if (mExecutor == null) {
                    initExecutor();
                }
                if (sendSignInList.size() > 0) {
                    mExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            //通过MQTT将员工签到信息发送至web端
                            logger.warn("Send sign in list to web, size ==> {}", sendSignInList.size() );
                            //mqttMessageHelper.sendToClient("staff/sign_in", JSON.toJSONString(sendSignInList));
                        }
                    });
                }
                if(sendVipList.size() > 0) {
                    mExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            //通过MQTT将VIP签到信息发送至web端的VIP页面
                            logger.warn("Send VIP list to web, size ==> {}", sendVipList.size() );
                            //mqttMessageHelper.sendToClient("staff/vip/sign_in", JSON.toJSONString(sendVipList));
                        }
                    });
                }
            }
        }
    }

    private void querySignInStaff(Long startTime) {
        if (token == null) {
            token = tokenService.getToken();
        }
        HashMap<String, Object> postParameters = new HashMap<>();
//        ///考勤记录查询开始时间
        postParameters.put("start_timestamp", startTime);
//        ///考勤记录查询结束时间
        Long queryEndTime = System.currentTimeMillis() / 1000;
        postParameters.put("end_timestamp", queryEndTime);
        //只获取员工数据
        ArrayList<String> identity = new ArrayList<>();
        identity.add("STAFF");
        postParameters.put("identity_list", identity);
        //只获取指定考勤设备的过人记录
        postParameters.put("device_id_list", ATTENDANCE_DEVICE_LIST);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        headers.add(HttpHeaders.AUTHORIZATION, token);
        HttpEntity httpEntity = new HttpEntity<>(JSON.toJSONString(postParameters), headers);
        try {
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(PARK_BASE_URL + "/visit_record/query", httpEntity, String.class);
            if (responseEntity.getStatusCodeValue() == ResponseCode.OK) {
                String body = responseEntity.getBody();
                if (body != null) {
                    ResponseModel responseModel = JSONObject.parseObject(body, ResponseModel.class);
                    if (responseModel != null && responseModel.getResult() != null) {
                        ArrayList<VisitRecord> tempList = (ArrayList<VisitRecord>) JSONArray.parseArray(responseModel.getResult(), VisitRecord.class);
                        if(tempList != null && tempList.size() > 0) {
                            processStaffSignInResponse(tempList, startTime.equals(Util.getDateStartTime().getTime() / 1000));
                            //query成功后用上一次查询的结束时间作为下一次开始时间，减去1秒形成闭区间,默认设备跟园区服务器的时间时间差别在1秒内
                            queryStartTime = queryEndTime - 1;
                        }
                    }
                }
            }
        } catch (HttpClientErrorException exception) {
            if (exception.getStatusCode().value() == ResponseCode.TOKEN_INVALID) {
                //token失效,重新获取token后再进行数据请求
                token = tokenService.getToken();
                querySignInStaff(startTime);
            }
        }
    }

    public boolean deleteStaff(String id) {
        boolean success = false;
        if (token == null && tokenService != null) {
            token = tokenService.getToken();
        }
        if (token != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.ACCEPT, "application/json");
            headers.add("Authorization", token);
            HttpEntity entity = new HttpEntity(headers);
            try {
                ResponseEntity<String> responseEntity = restTemplate.exchange(PARK_BASE_URL + "/staffs/" + id, HttpMethod.DELETE, entity, String.class);
                if (responseEntity.getStatusCodeValue() == ResponseCode.OK) {
                    String body = responseEntity.getBody();
                    if (body != null) {
                        ResponseModel responseModel = JSONObject.parseObject(body, ResponseModel.class);
                        if(responseModel != null && responseModel.getRtn() == 0) {
                            success = true;
                        }
                    }
                }
            } catch (HttpClientErrorException exception) {
                if (exception.getStatusCode().value() == ResponseCode.TOKEN_INVALID) {
                    token = tokenService.getToken();
                    if (token != null) {
                        deleteStaff(id);
                    }
                }
            }
        }
        return success;
    }

    private void initExecutor() {
        mExecutor = new ThreadPoolTaskExecutor();
        mExecutor.setCorePoolSize(2);
        mExecutor.setMaxPoolSize(5);
        mExecutor.setThreadNamePrefix("YTTPS-");
        mExecutor.initialize();
    }

    public ArrayList<Staff> getStaffList() {
        return staffList;
    }

    public ArrayList<VisitRecord> getStaffSignInList() {
        return staffSignInList;
    }

    public ArrayList<VisitRecord> getVipSignInList() {
        return vipSignInList;
    }
}
