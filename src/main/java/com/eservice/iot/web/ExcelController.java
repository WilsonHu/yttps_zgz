package com.eservice.iot.web;

import com.eservice.iot.core.Result;
import com.eservice.iot.core.ResultGenerator;
import com.eservice.iot.model.AttendanceTime;
import com.eservice.iot.service.AccessService;
import org.apache.poi.hssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description: java类作用描述
 * @Author: ZT
 * @CreateDate: 2019/3/20 16:13
 */
@RestController
@RequestMapping("/excel")
public class ExcelController {
    private final static Logger logger = LoggerFactory.getLogger(ExcelController.class);

    @Value("${excel_path}")
    private String EXCEL_PATH;
    @Value("${broker-host}")
    private String BROKER_HOST;

    @Resource
    private AccessService accessService;

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @PostMapping("/exportRecord")
    public Result exportRecord(@RequestParam(defaultValue = "") String chooseTime, @RequestParam(defaultValue = "员工") String identity) {
        Date date = new Date();
        //将前端传过来的时间转化为yyyy-MM-dd HH:mm:ss
        if(chooseTime.equalsIgnoreCase(null)&&chooseTime.equalsIgnoreCase("")){
            date = new Date(chooseTime);
        }
        String time = format.format(date);

        Long startTime = null;
        Long endTime = null;

        try {
            startTime = sdf.parse(time + " 00:00:00").getTime() / 1000;
            endTime = sdf.parse(time + " 23:59:59").getTime() / 1000;
        }catch (Exception e){
            e.printStackTrace();
        }

/*        //获取本地ip地址
        try {
            InetAddress ip = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }*/

        //获取进入记录
        Map<String,Date> inTimes = accessService.queryUserIn(startTime, endTime, identity);
        //获取出去记录
        Map<String,Date>  outTimes = accessService.queryUserOut(startTime, endTime, identity);
        //进出记录合并之后的数据，每个人对应一个最早进入时间和一个最晚出去时间
        List<AttendanceTime> userTimes = mergeTime(inTimes,outTimes);

        if (userTimes.size() > 0) {
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet(identity+"进出记录");
            //设置要导出的文件的名字
            String fileName ="人员进出时间记录-" + format.format(new Date()) + ".xlsx";
            //新增数据行，并且设置单元格数据
            insertDataInSheet(sheet, userTimes);
            try {
                //放excel表格需要存放的地址
                File dir = new File(EXCEL_PATH);
                if (!dir.exists()) {
                    if (dir.mkdir()) {
                        logger.info("excel目录创建成功");
                    }
                }
                FileOutputStream out = new FileOutputStream(EXCEL_PATH + fileName);
                workbook.write(out);
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ResultGenerator.genSuccessResult(BROKER_HOST  + ":8080/"+ fileName);
        } else {
            return ResultGenerator.genSuccessResult("考勤记录为：0");
        }

    }

    private void insertDataInSheet( HSSFSheet sheet, List<AttendanceTime> userTimes) {
        String[] excelHeaders = {"序号", "姓名","最早进入时间", "最晚出去时间"};
        //headers表示excel表中第一行的表头
        HSSFRow row3 = sheet.createRow(0);
        //在excel表中添加表头
        for (int i = 0; i < excelHeaders.length; i++) {
            HSSFCell cell = row3.createCell(i);
            HSSFRichTextString text = new HSSFRichTextString(excelHeaders[i]);
            cell.setCellValue(text);
        }
        //在表中存放查询到的数据放入对应的列
        int rowNum = 1;
        for (AttendanceTime attendance : userTimes) {
            HSSFRow row = sheet.createRow(rowNum);
            row.createCell(0).setCellValue(rowNum);
            row.createCell(1).setCellValue(attendance.getName());
            row.createCell(2).setCellValue(sdf.format(attendance.getStartTime()));
            row.createCell(3).setCellValue(sdf.format(attendance.getEndTime()));
            rowNum++;
        }
    }

    private List<AttendanceTime>  mergeTime(Map<String,Date>  inTime,Map<String,Date>  outTime){
        List<AttendanceTime> userTime = new ArrayList<>();
        if(inTime!=null)
        for (Map.Entry<String,Date> entryIn : inTime.entrySet()) {
            //创建一个存储，姓名对应最早进入时间和最晚出去时间
            AttendanceTime attendanceTime = new AttendanceTime();
            //设置当前人员的姓名
            attendanceTime.setName(entryIn.getKey());
            //设置当前人员最早进入时间
            attendanceTime.setStartTime(entryIn.getValue());
            //寻找最晚出去时间
            if(outTime!=null)
            for (Map.Entry<String,Date> entryOut:outTime.entrySet()) {
                if(entryIn.getKey().equalsIgnoreCase(entryOut.getKey())){
                    //存储当前人员最晚出去时间
                    attendanceTime.setEndTime(entryOut.getValue());
                    //移除已经存储过的
                    outTime.remove(entryIn.getKey());
                    break;
                }
            }
            userTime.add(attendanceTime);
        }
        //只有出去的记录的人员也要保存
        if(outTime!=null&&outTime.size()>0){
            for (Map.Entry<String,Date> entryOut:outTime.entrySet()) {
                //创建一个存储，姓名对应最晚出去时间
                AttendanceTime attendanceTime = new AttendanceTime();
                //设置当前人员的姓名
                attendanceTime.setName(entryOut.getKey());
                //设置当前人员最晚出去时间
                attendanceTime.setEndTime(entryOut.getValue());
                userTime.add(attendanceTime);
            }
        }
        return userTime;
    }
}
