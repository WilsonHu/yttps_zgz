package com.eservice.iot.dao;

import com.eservice.iot.core.Mapper;
import com.eservice.iot.model.visitor_info.VisitorInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface VisitorInfoMapper extends Mapper<VisitorInfo> {
    List<VisitorInfo> search (Map map);
}