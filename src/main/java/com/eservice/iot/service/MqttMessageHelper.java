package com.eservice.iot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


/**
 * @author Wilson Hu  2017-12-01
 */
@Component
@Service
public class MqttMessageHelper {

    @Autowired
    MqttService.MyGateway myGateway;

    private final static Logger logger = LoggerFactory.getLogger(MqttMessageHelper.class);

    /**
     * 向MQTT发送数据
     *
     * @param topic
     * @param msg
     */
    public void sendToClient(String topic, String msg) {
        try {
            /**
             * @result: 1 ==>"Approve"; 0 ==>"Reject"
             * @msg: description
             * {
             *     "result": 1,
             *     "name":"visitor name",
			 *     "isChinese":1,
             *     "msg":"xxx"
             * }
             */
            myGateway.sendToMqtt(topic, msg);
        } catch (Exception e) {
            Logger logger = LoggerFactory.getLogger(MqttMessageHelper.class);
            logger.error("MQTT消息发送异常", e);
        }
    }

    /**
     * 用于接收MQTT数据，具体业务需要解析message对象后完成
     *
     * @param message
     * @throws Exception
     */
    public void handleMessage(Message<?> message) throws Exception {
        String topic = message.getHeaders().get(MqttHeaders.TOPIC).toString();
        if (topic != null) {
            String payload = message.getPayload().toString();
            if (payload != null) {
                System.out.println(message);
            } else {
                logger.warn("Data is invalid!");
            }
            logger.info("Topic:" + topic + " || Payload:" + payload);

        }
    }
}
