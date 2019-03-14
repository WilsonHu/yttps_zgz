package com.eservice.iot.service.impl;

import com.eservice.iot.dao.VisitorInfoMapper;
import com.eservice.iot.model.visitor_info.VisitorInfo;
import com.eservice.iot.service.VisitorInfoService;
import com.eservice.iot.core.AbstractService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;


/**
* Class Description: xxx
* @author Wilson Hu
* @date 2019/03/13.
*/
@Service
@Transactional
public class VisitorInfoServiceImpl extends AbstractService<VisitorInfo> implements VisitorInfoService {
    @Resource
    private VisitorInfoMapper visitorInfoMapper;

    public List<VisitorInfo> search (String date){
        return visitorInfoMapper.search(date);
    }
}
