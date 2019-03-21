package com.eservice.iot.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eservice.iot.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * @author HT
 */
@Component
public class AccessService {

    private final static Logger logger = LoggerFactory.getLogger(AccessService.class);

    @Value("${park_base_url}")
    private String PARK_BASE_URL;

    @Value("${in_device_id}")
    private String IN_DEVICE_ID;

    @Value("${out_device_id}")
    private String OUT_DEVICE_ID;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Token
     */
    private String token;

    @Autowired
    private TokenService tokenService;


    public Map<String,Date> queryUserIn(Long startTime, Long endTime, String type) {

        if (token == null) {
            token = tokenService.getToken();
        }

        //存储所有进入的通行记录
        List<AccessRecord> accessPassIn = new ArrayList<>();

        if (token != null) {
            HashMap<String, Object> postParameters = new HashMap<>();
            ///考勤记录查询开始时间
            postParameters.put("start_timestamp", startTime);
            ///考勤记录查询结束时间
            Long queryEndTime = endTime;
            postParameters.put("end_timestamp", queryEndTime);

            ArrayList<String> identity = new ArrayList<>();
            if(type.equalsIgnoreCase("员工")){
                identity.add(Constant.STAFF);
            }else if(type.equalsIgnoreCase("访客")){
                identity.add(Constant.VISITOR);
            }
            postParameters.put("identity_list", identity);

             List<String> deviceList=new ArrayList<>();
            for(String device : IN_DEVICE_ID.split(",")){
                deviceList.add(device);
            }
            postParameters.put("device_id_list", deviceList);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
            headers.add(HttpHeaders.AUTHORIZATION, tokenService.getToken());
            HttpEntity httpEntity = new HttpEntity<>(JSON.toJSONString(postParameters), headers);

            ResponseEntity<String> responseEntity = restTemplate.postForEntity(PARK_BASE_URL + "/access/record", httpEntity, String.class);
            if (responseEntity.getStatusCodeValue() == ResponseCode.OK) {
                String body = responseEntity.getBody();
                if (body != null) {
                    ResponseModel responseModel = JSONObject.parseObject(body, ResponseModel.class);
                    if (responseModel != null && responseModel.getResult() != null) {
                        accessPassIn = JSONArray.parseArray(responseModel.getResult(), AccessRecord.class);
                        if (accessPassIn != null) {
                            //移除不通过的数据
                            for (int i=0;i<accessPassIn.size();i++) {
                                AccessRecord accessRecord = accessPassIn.get(i);
                                if (!accessRecord.getPass_result().equals("PASS")) {
                                    accessPassIn.remove(accessRecord);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            logger.error("Token is null, query accessPass error!");
        }

        if(accessPassIn.size()>0){
            //对数据，按姓名进行排序，确保姓名相同的人在一块
        Collections.sort(accessPassIn, new Comparator<AccessRecord>() {
            @Override
            public int compare(AccessRecord o1, AccessRecord o2) {
                return o1.getPerson().getPerson_information().getName().compareTo(o2.getPerson().getPerson_information().getName());
            }
        });
            int size=accessPassIn.size();
            //存储每个通行过得姓名对应得最早一次进入时间
            Map<String, Date> outTime = new HashMap();
            for (int i=0; i<size;){
                //默认第一个姓名对应得时间是最早时间
                String nameMin=accessPassIn.get(i).getPerson().getPerson_information().getName();
                int dateMin = accessPassIn.get(i).getTimestamp();
                int j=i+1;
                while (j<size){
                    //从第二个人开始比较
                    String name=accessPassIn.get(j).getPerson().getPerson_information().getName();
                    int date =  accessPassIn.get(j).getTimestamp();
                    //在姓名相同的一块数据中，找到这个人的最早进入时间
                    if(name.equalsIgnoreCase(nameMin)){
                        if(date<dateMin){
                            dateMin=date;
                        }
                    }else{
                        //当查询到下一个姓名相同的数据块中，对前一次的数找到的姓名对应得最早进入时间进行保存
                        outTime.put(nameMin,new Date(dateMin*1000L));
                        //在从这个姓名开始，重新寻找
                        break;
                    }
                    j++;
                }
                i=j;
                if(i==size){
                    outTime.put(nameMin,new Date(dateMin*1000L));
                }
            }
            return  outTime;
        }else{
            logger.error("accessPassIn is size:0, add empDate error!");
            return null;
        }
    }

    public Map<String,Date>  queryUserOut(Long startTime, Long endTime, String type) {

        if (token == null) {
            token = tokenService.getToken();
        }

        //存储所有出去的通行记录
        List<AccessRecord> accessPassOut = new ArrayList<>();

        if (token != null) {
            HashMap<String, Object> postParameters = new HashMap<>();
            ///记录查询开始时间
            postParameters.put("start_timestamp", startTime);
            ///记录查询结束时间
            Long queryEndTime = endTime;
            postParameters.put("end_timestamp", queryEndTime);

            ArrayList<String> identity = new ArrayList<>();
            if(type.equalsIgnoreCase("员工")){
                identity.add(Constant.STAFF);
            }else if(type.equalsIgnoreCase("访客")){
                identity.add(Constant.VISITOR);
            }
            postParameters.put("identity_list", identity);

            List<String> deviceList=new ArrayList<>();
            for(String device : OUT_DEVICE_ID.split(",")){
                deviceList.add(device);
            }
            postParameters.put("device_id_list", deviceList);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
            headers.add(HttpHeaders.AUTHORIZATION, tokenService.getToken());
            HttpEntity httpEntity = new HttpEntity<>(JSON.toJSONString(postParameters), headers);

            ResponseEntity<String> responseEntity = restTemplate.postForEntity(PARK_BASE_URL + "/access/record", httpEntity, String.class);
            if (responseEntity.getStatusCodeValue() == ResponseCode.OK) {
                String body = responseEntity.getBody();
                if (body != null) {
                    ResponseModel responseModel = JSONObject.parseObject(body, ResponseModel.class);
                    if (responseModel != null && responseModel.getResult() != null) {
                        accessPassOut = JSONArray.parseArray(responseModel.getResult(), AccessRecord.class);
                        if (accessPassOut != null) {
                            //移除不通过的数据
                            for (int i=0;i<accessPassOut.size();i++) {
                                AccessRecord accessRecord = accessPassOut.get(i);
                                if (!accessRecord.getPass_result().equals("PASS")) {
                                    accessPassOut.remove(accessRecord);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            logger.error("Token is null, query accessPass error!");
        }


        if(accessPassOut.size()>0){
            //对数据，按姓名进行排序，确保姓名相同的人在一块
            Collections.sort(accessPassOut, new Comparator<AccessRecord>() {
                @Override
                public int compare(AccessRecord o1, AccessRecord o2) {
                    return o1.getPerson().getPerson_information().getName().compareTo(o2.getPerson().getPerson_information().getName());
                }
            });
            int size=accessPassOut.size();
            //存储每个人最晚一次出去时间
            Map<String, Date> outTime = new HashMap();
            for (int i=0; i<size;){
                //默认第一个姓名对应得时间是最晚时间
                String nameMax=accessPassOut.get(i).getPerson().getPerson_information().getName();
                int dateMax = accessPassOut.get(i).getTimestamp();
                int j=i+1;
                while (j<size){
                    //从第二个人开始比较
                    String name=accessPassOut.get(j).getPerson().getPerson_information().getName();
                    int date =  accessPassOut.get(j).getTimestamp();
                    //在姓名相同的一块数据中，找到这个人的最晚出去时间
                    if(name.equalsIgnoreCase(nameMax)){
                        if(date>dateMax){
                            dateMax=date;
                        }
                    }else{
                        //当查询到下一个姓名相同的数据块中，对前一次的数找到的姓名对应得最晚出去时间进行保存
                        outTime.put(nameMax,new Date(dateMax*1000L));
                        //在从这个姓名开始，重新寻找
                        break;
                    }
                    j++;
                }
                i=j;
                if(i==size){
                    outTime.put(nameMax,new Date(dateMax*1000L));
                }
            }
            return  outTime;
        }else{
            logger.error("accessPassOut is size:0, add empDate error!");
            return null;
        }
    }

}
