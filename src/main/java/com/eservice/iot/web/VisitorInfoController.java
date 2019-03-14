package com.eservice.iot.web;
import com.alibaba.fastjson.JSONObject;
import com.eservice.iot.core.Result;
import com.eservice.iot.core.ResultGenerator;
import com.eservice.iot.model.visitor_info.VisitorInfo;
import com.eservice.iot.util.ExcelData;
import com.eservice.iot.service.VisitorInfoService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Condition;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


/**
* Class Description: xxx
* @author Wilson Hu
* @date 2019/03/13.
*/
@RestController
@RequestMapping("/visitor/info")
public class VisitorInfoController {
    @Resource
    private VisitorInfoService visitorInfoService;
    
    @PostMapping("/add")
    public Result add(MultipartFile multipartFile) throws Exception{
        StringBuffer errorIndex = new StringBuffer();//存储数据不完整的序号。
        List<String> visitorInfoJsonList = getVisitorJsonList(multipartFile,errorIndex);
        if(visitorInfoJsonList!=null){
            for (String visitorInfoJson:visitorInfoJsonList) {
                VisitorInfo visitorInfo = JSONObject.parseObject(visitorInfoJson,VisitorInfo.class);
                visitorInfo.setDatetime(new Date());
                visitorInfoService.save(visitorInfo);
            }
            return ResultGenerator.genSuccessResult();
        }else {
            return ResultGenerator.genSuccessResult("序号为："+errorIndex+"的数据不完整！");
        }
    }

    @PostMapping("/delete")
    public Result delete(@RequestParam Integer id) {
        visitorInfoService.deleteById(id);
        return ResultGenerator.genSuccessResult();
    }

    @PostMapping("/update")
    public Result update(@RequestBody @NotNull VisitorInfo visitorInfo) {
        visitorInfoService.update(visitorInfo);
        return ResultGenerator.genSuccessResult();
    }

    @PostMapping("/detail")
    public Result detail(@RequestParam @NotNull Integer id) {
        VisitorInfo visitorInfo = visitorInfoService.findById(id);
        return ResultGenerator.genSuccessResult(visitorInfo);
    }

    @PostMapping("/list")
    public Result list(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "0") Integer size, String chooseTime) {
        PageHelper.startPage(page, size);
        List<VisitorInfo> list = visitorInfoService.search(chooseTime);
        PageInfo pageInfo = new PageInfo(list);
        return ResultGenerator.genSuccessResult(pageInfo);
    }


    public List<String> getVisitorJsonList(MultipartFile multipartFile,StringBuffer errorIndex) throws Exception{
       List<String[]> excel = ExcelData.getExcelData(multipartFile);
       String[] data = {"id","name","idcard","company"};
       List<String> visitorJsonList = new ArrayList<>();
       int rowIndex=1;
        for (String[] rowData: excel) {
            boolean isTrue=(rowData[2].length()==18)?true:false;        //身份证是否正确是否正确
            StringBuffer visitorJson = new StringBuffer("{");       //json格式以{开始
            for (int i=0;isTrue&&i<rowData.length;i++) {
                if("".equals(rowData[i])){
                    isTrue=false;
                    break;
                }
                visitorJson.append("\""+data[i]+"\":\""+rowData[i]+"\",");
            }
            if(isTrue) {
                visitorJson=new StringBuffer(visitorJson.substring(0, visitorJson.lastIndexOf(","))+"}");//去除最后一个，json格式以}结束
                visitorJsonList.add(visitorJson.toString());
            }else {
                errorIndex.append(rowIndex+"");
            }
            rowIndex++;
        }
        if(errorIndex.length()==0){
            return visitorJsonList;
        }else {
            return null;
        }
    }
}
