package cn.itcast.mq.consumer;

import cn.itcast.utils.MailUtil;
import com.alibaba.fastjson.JSON;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

import java.util.Map;

public class EmailConsumer implements MessageListener {
    @Override
    public void onMessage(Message message) {
        byte[] body = message.getBody();
//        把字节转成map
        Map<String,String> map = JSON.parseObject(body, Map.class);
        String to = map.get("to");
        String subject = map.get("subject");
        String content = map.get("content");
        try {
            MailUtil.sendMsg(to,subject,content);
            System.out.println("发送成功");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
