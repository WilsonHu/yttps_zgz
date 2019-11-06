package com.eservice.iot.web;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import com.alibaba.fastjson.JSONObject;
import com.eservice.iot.core.Result;
import com.eservice.iot.core.ResultGenerator;
import com.eservice.iot.model.Verify;
import com.eservice.iot.model.visitor_info.VisitorInfo;
import com.eservice.iot.service.MqttMessageHelper;
import com.eservice.iot.service.VisitorService;
import com.eservice.iot.util.ExcelData;
import com.eservice.iot.service.VisitorInfoService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


import javax.annotation.Resource;

import javax.imageio.ImageIO;
import javax.validation.constraints.NotNull;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    @Resource
    MqttMessageHelper mqttMessageHelper;
    @Resource
    VisitorService visitorService;

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    @PostMapping("/count")
    public Result count() {
        int status0=0;
        int status1=0;
        List<VisitorInfo> list = visitorInfoService.search(formatter.format( new Date()),null);
        for (VisitorInfo visitorInfo : list) {
                if(visitorInfo.getStatus()==0){
                    status0++;
                }
                if(visitorInfo.getStatus()==1){
                    status1++;
                }
        }
        return ResultGenerator.genSuccessResult("{\"status0\":"+status0+",\"status1\":"+status1+",\"visitor\":"+(status0+status1)+"}");
    }


    @PostMapping("/verify")
    public Result verify(@RequestBody  String data){
        Verify verify = JSONObject.parseObject(data,Verify.class);//获取认证核验机的数据
        List<VisitorInfo> visitorInfoDay =  visitorInfoService.search(formatter.format(new Date()),0);
      //  if(!visitorService.isExistToPark(verify)) {//是否已经存在园区访客中
            boolean isDay=false;//是否是今天的访客列表中的人
            for (VisitorInfo visitorInfo : visitorInfoDay) {
                if (verify.getIdInfo().getId_num().equalsIgnoreCase(visitorInfo.getIdcard())) {
                    if(visitorInfo.getStatus()==0) {
                       if( visitorService.createVisitor(verify.getVerifyResult().getFace_image(), visitorInfo)) {
                           visitorInfo.setStatus(1);//修改来访状态
                           visitorInfoService.update(visitorInfo);
                       }
                    }
                    isDay=true;
                    break;
                }
            }
            if(isDay){
                mqttMessageHelper.sendToClient("visitor/success",JSONObject.toJSONString(verify.getIdInfo()));
            }else {
                String img = verify.getVerifyResult().getFace_image();
                byte[] buff=Base64.decode(img);

                File file =null;
                FileOutputStream fout=null;
                try {
                     file = File.createTempFile("base64", ".jpg");
                    fout=new FileOutputStream(file);
                    fout.write(buff);
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    if(fout!=null) {
                        try {
                            fout.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                try {
                    Thumbnails.of(file).scale(0.4,0.4).toFile(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                    FileInputStream fin = null;
                try {
                    fin = new FileInputStream(file);
                    byte[] buffInput = new byte[fin.available()];
                    fin.read(buffInput);
                    img = Base64.encode(buffInput);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fin != null) {
                        try {
                            fin.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    file.delete();
                }
                String jsonId = JSONObject.toJSONString(verify.getIdInfo());
                StringBuffer json=new StringBuffer(jsonId.substring(0,jsonId.lastIndexOf("}")));
                json.append(",\"face_image\":\""+img+"\"}");
                mqttMessageHelper.sendToClient("visitor/error",json.toString());
            }
      /*  }else{
            mqttMessageHelper.sendToClient("visitor/success",JSONObject.toJSONString(verify.getIdInfo()));
        }*/
        return ResultGenerator.genSuccessResult();
    }

    @PostMapping("/add")
    public Result add(MultipartFile multipartFile) throws Exception{
        StringBuffer errorIndex = new StringBuffer();//存储数据不完整的序号。
        List<String> visitorInfoJsonList = getVisitorJsonList(multipartFile,errorIndex);
        if(visitorInfoJsonList!=null){
            int repCount=0;
            for (String visitorInfoJson:visitorInfoJsonList) {
                VisitorInfo visitorInfo = JSONObject.parseObject(visitorInfoJson,VisitorInfo.class);
                if(!isExistToLoacl(visitorInfo)){//根据身份证判断该访客是否已经添加到数据了，true 已添加，false，未添加
                    visitorInfoService.save(visitorInfo);
                }else{
                    repCount++;
                }
            }
            if(repCount==0){
                return ResultGenerator.genSuccessResult("{\"message\":\"文件上传成功！\",\"code\":200}");
            }else {
                return ResultGenerator.genSuccessResult("{\"message\":\"你有"+repCount+"数据，在相同访客时间已经添加！\",\"code\":200}");
            }
        }else {
            return ResultGenerator.genSuccessResult("{\"message\":\"第"+errorIndex+"行的数据不完整！\",\"code\":500}");
        }
    }

    @PostMapping("/delete")
    public Result delete(@RequestParam Integer id) {
        return ResultGenerator.genSuccessResult(visitorInfoService.deleteById(id));
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
    public Result list(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "0") Integer size, @RequestParam(defaultValue = "") String chooseTime,@RequestParam(defaultValue = "") Integer status) {
        PageHelper.startPage(page, size);
        String dateTime=null;
        if(null!=chooseTime&&!"".equals(chooseTime)){
            dateTime=formatter.format(new Date(chooseTime));
        }
        List<VisitorInfo> list = visitorInfoService.search(dateTime,status);
        PageInfo pageInfo = new PageInfo(list);
        return ResultGenerator.genSuccessResult(pageInfo);
    }

    public List<String> getVisitorJsonList(MultipartFile multipartFile,StringBuffer errorIndex) throws Exception{
       List<String[]> excel = ExcelData.getExcelData(multipartFile);
       String[] data = {"id","name","idcard","company","dateTime"};
       List<String> visitorJsonList = new ArrayList<>();
       //因为跳过了第一行，
       int rowIndex=2;
        for (String[] rowData: excel) {
            boolean isTrue=(rowData[2].length()==18)?true:false;        //身份证是否正确是否正确
            StringBuffer visitorJson = new StringBuffer("{");       //json格式以{开始
            for (int i=1;isTrue&&i<rowData.length;i++) {
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
                errorIndex.append(rowIndex+",");
            }
            rowIndex++;
        }
        if(errorIndex.length()==0){
            return visitorJsonList;
        }else {
            return null;
        }
    }

    public boolean isExistToLoacl(VisitorInfo visitor){
         List<VisitorInfo> visitorInfoAll=visitorInfoService.findAll();
        for (VisitorInfo visitorInfo : visitorInfoAll ) {
            if(visitorInfo.getIdcard().equals(visitor.getIdcard())&&visitorInfo.getDatetime().equals(visitor.getDatetime())){
                return true;
            }
        }
        return false;
    }

}
