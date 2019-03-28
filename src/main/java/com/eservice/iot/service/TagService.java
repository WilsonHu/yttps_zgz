package com.eservice.iot.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eservice.iot.model.Constant;
import com.eservice.iot.model.ResponseModel;
import com.eservice.iot.model.Tag;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * @author HT
 */
@Component
public class TagService {

    private final static Logger logger = LoggerFactory.getLogger(TagService.class);

    @Value("${park_base_url}")
    private String PARK_BASE_URL;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TokenService tokenService;

    private ThreadPoolTaskExecutor mExecutor;


    /**
     * Token
     */
    private String token;
    /**
     * 访客tag
     */
    private List<Tag> allTagList = new ArrayList<>();
    /**
     * 访客tag
     */
    private List<Tag> visitorTagList = new ArrayList<>();

    /**
     * 员工tag
     */
    private List<Tag> staffTagList = new ArrayList<>();

    /**
     * 每10秒更新一次TAG
     */
    @Scheduled(fixedRate = 1000 * 10)
    public void fetchTags() {
        if (tokenService != null) {
            if (token == null) {
                token = tokenService.getToken();
            }
            if (token != null) {
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.ACCEPT, "application/json");
                headers.add("Authorization", token);
                HttpEntity entity = new HttpEntity(headers);
                try {
                    ResponseEntity<String> responseEntity = restTemplate.exchange(PARK_BASE_URL + "/tags?size=0", HttpMethod.GET, entity, String.class);
                    if (responseEntity.getStatusCodeValue() == ResponseCode.OK) {
                        String body = responseEntity.getBody();
                        if (body != null) {
                            processTagResponse(body);
                        } else {
                            fetchTags();
                        }
                    }
                } catch (HttpClientErrorException errorException) {
                    if (errorException.getStatusCode().value() == ResponseCode.TOKEN_INVALID) {
                        //token失效,重新获取token后再进行数据请求
                        token = tokenService.getToken();
                        if (token != null) {
                            fetchTags();
                        }
                    }
                }
            }

        } else {
            ///等待tokenService初始化完成，TAG标签被其他很多service依赖，所以需要其先初始化完毕后
            if (mExecutor == null) {
                mExecutor = new ThreadPoolTaskExecutor();
                mExecutor.setCorePoolSize(1);
                mExecutor.setMaxPoolSize(2);
                mExecutor.setThreadNamePrefix("YTTPS-");
                mExecutor.initialize();
            }
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                        fetchTags();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void processTagResponse(String body) {
        ResponseModel responseModel = JSONObject.parseObject(body, ResponseModel.class);
        if (responseModel != null && responseModel.getResult() != null) {
            List<Tag> tmpList =JSONArray.parseArray(responseModel.getResult(), Tag.class);
            if (tmpList != null &&tmpList.size()>0) {
                ArrayList<Tag> visitorTagList = new ArrayList<>();
                ArrayList<Tag> staffTagList = new ArrayList<>();
                for (Tag tag : tmpList) {
                    for (String str : tag.getVisible_identity()) {
                        if (Constant.VISITOR.equals(str)) {
                            visitorTagList.add(tag);
                        }
                        if (Constant.STAFF.equals(str)) {
                            staffTagList.add(tag);
                        }
                    }
                }
                if(this.allTagList.size()!=tmpList.size()){
                    logger.info("The number of allTagList：{} ==> {}", this.allTagList.size(), tmpList.size());
                    this.allTagList = tmpList;
                    logger.info("The number of visitorTagList：{} ==> {}", this.visitorTagList.size(), visitorTagList.size());
                    this.visitorTagList = visitorTagList;
                    logger.info("The number of staffTagList：{} ==> {}", this.staffTagList.size(), staffTagList.size());
                    this.staffTagList = staffTagList;
                }
            }
        }
    }

    public boolean createTag(String name, String identity) {
        HashMap<String, Object> postParameters = new HashMap<>();
        ArrayList<Tag> tagList = new ArrayList<>();
        Tag tag = new Tag();
        tag.setTag_name(name);
        ArrayList<String> identityList = new ArrayList<>();
        identityList.add(identity);
        tag.setVisible_identity(identityList);
        postParameters.put("tag_list", tagList);
        tagList.add(tag);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        headers.add(HttpHeaders.AUTHORIZATION, tokenService.getToken());
        HttpEntity httpEntity = new HttpEntity<>(JSON.toJSONString(postParameters), headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(PARK_BASE_URL + "/tags", httpEntity, String.class);
        if (responseEntity.getStatusCodeValue() == ResponseCode.OK) {
            return true;
        } else {
            return false;
        }
    }

    public List<Tag> getAllTagList() {
        return allTagList;
    }

    public List<Tag> getVisitorTagList() {
        return visitorTagList;
    }

    public List<Tag> getStaffTagList() {
        return staffTagList;
    }

    /**
     * 根据机构名称获取 机构id
     *
     * @param stringList 机构名称集合
     * @return 机构id集合
     */
    public ArrayList<String> getDepartmentId(String[] stringList) {
        ArrayList<String> idList = new ArrayList<>();
        for (String str : stringList) {
            isExist(str);//判断标签是否存在，不存在则先新增
            for (Tag tag : visitorTagList) {
                if (str.equals(tag.getTag_name())) {
                    idList.add(tag.getTag_id());
                }
            }
        }
        return idList;
    }

    public void isExist(String tagName) {
        boolean isExist = false;
        for (Tag tag : visitorTagList) {
            if (tagName.equals(tag.getTag_name())) {
                isExist = true;
            }
        }
        if (!isExist) {
            createTag(tagName, Constant.VISITOR);
            fetchTags();
        }
    }
}
