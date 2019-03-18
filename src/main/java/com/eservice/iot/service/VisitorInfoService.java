package com.eservice.iot.service;
import com.eservice.iot.model.visitor_info.VisitorInfo;
import com.eservice.iot.core.Service;

import java.util.List;

/**
* Class Description: xxx
* @author Wilson Hu
* @date 2019/03/13.
*/
public interface VisitorInfoService extends Service<VisitorInfo> {
    List<VisitorInfo> search (String date,Integer status);
}
