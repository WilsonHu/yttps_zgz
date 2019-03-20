package com.eservice.iot.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eservice.iot.model.*;
import com.eservice.iot.model.visitor_info.VisitorInfo;
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
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;


import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;


/**
 * @author HT
 */
@Component
public class VisitorService {

    private final static Logger logger = LoggerFactory.getLogger(VisitorService.class);

    @Value("${park_base_url}")
    private String PARK_BASE_URL;

    /**
     * 长期有效的访客
     */
    public static final String LONG_TIME_VALID = "0";

    /**
     * 临时访客
     */
    public static final String TMP_VALID = "1";

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Token
     */
    private String token;
    /**
     * 访客列表
     */
    private List<Visitor> visitorList = new ArrayList<>();

    @Autowired
    private TokenService tokenService;

    @Autowired
    private TagService tagService;
    /**
     * cron: 秒、分、时、日、月、年
     * 早上六点至傍晚八点，每10秒钟获取一次当天访客信息
     */
    @Scheduled(fixedRate = 1000 * 10)
    public void fetchVisitorListScheduled() {
        if (token == null) {
            token = tokenService.getToken();
        }
        if(token != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.ACCEPT, "application/json");
            headers.add("Authorization", token);
            HttpEntity entity = new HttpEntity(headers);
            try {
                ResponseEntity<String> responseEntity = restTemplate.exchange(PARK_BASE_URL + "/visitors", HttpMethod.GET, entity, String.class);
                if (responseEntity.getStatusCodeValue() == ResponseCode.OK) {
                    String body = responseEntity.getBody();
                    if (body != null) {
                        processVisitorResponse(body);
                    } else {
                        fetchVisitorListScheduled();
                    }
                }
            } catch (HttpClientErrorException errorException) {
                if (errorException.getStatusCode().value() == ResponseCode.TOKEN_INVALID) {
                    token = tokenService.getToken();
                    fetchVisitorListScheduled();
                }
            }
        } else {
            logger.error("Token is null, fetch visitor list error!");
        }
    }

    private void processVisitorResponse(String body) {
        ResponseModel responseModel = JSONObject.parseObject(body, ResponseModel.class);
        if (responseModel != null && responseModel.getResult() != null) {
            List<Visitor> tmpList = JSONArray.parseArray(responseModel.getResult(), Visitor.class);
            if (tmpList != null ) {
                if (tmpList.size() != visitorList.size()) {
                    logger.info("The number of visitor：{} ==> {}", visitorList.size(), tmpList.size());
                    visitorList = tmpList;
                }
            }
        }
    }

    public boolean isExistToPark(Verify verify){
        boolean isTrue = false;
        for (Visitor visitor : visitorList) {
            if(verify.getIdInfo().getId_num().equalsIgnoreCase(visitor.getPerson_information().getIdentity_number())){
                isTrue=true;
            }
        }
        return isTrue;
    }

    public boolean createVisitor(String  image, VisitorInfo visitorInfo) {
        HashMap<String, Object> postParameters = new HashMap<>();
        ArrayList<Visitor> visitors = new ArrayList<>();
        Visitor visitor = new Visitor();
        visitor.setFace_image_content(image);
        PersonInformation personInformation = new PersonInformation();
        personInformation.setName(visitorInfo.getName());
        personInformation.setIdentity_number(visitorInfo.getIdcard());
        personInformation.setVisit_time_type("1");
        personInformation.setVisitee_name("某某");
        visitor.setPerson_information(personInformation);
        visitor.setTag_id_list(tagService.getDepartmentId(new String[]{"访客",visitorInfo.getCompany()}));
        visitors.add(visitor);
        postParameters.put("visitor_list", visitors);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        headers.add(HttpHeaders.AUTHORIZATION, tokenService.getToken());
        HttpEntity httpEntity = new HttpEntity<>(JSON.toJSONString(postParameters), headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(PARK_BASE_URL + "/visitors", httpEntity, String.class);
        if (responseEntity.getStatusCodeValue() == ResponseCode.OK) {
            return true;
        } else {
            return false;
        }
    }
}
