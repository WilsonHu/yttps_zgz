package com.eservice.iot.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @Description: java类作用描述
 * @Author: ZT
 * @CreateDate: 2019/3/14 12:41
 */
public class Verify {
    @JsonProperty("device_info")
    DeviceInfo deviceInfo;
    @JsonProperty("id_info")
    IdInfo idInfo;
    @JsonProperty("verify_result")
    VerifyResult verifyResult;

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public IdInfo getIdInfo() {
        return idInfo;
    }

    public void setIdInfo(IdInfo idInfo) {
        this.idInfo = idInfo;
    }

    public VerifyResult getVerifyResult() {
        return verifyResult;
    }

    public void setVerifyResult(VerifyResult verifyResult) {
        this.verifyResult = verifyResult;
    }
}
