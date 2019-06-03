package cn.itcast.core.service;


import cn.itcast.core.dao.user.UserDao;
import cn.itcast.core.pojo.user.User;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private JmsTemplate jmsTemplate;
//
    @Autowired
    private ActiveMQQueue smsDestination;

    @Value("${template_code}")
    private String template_code;
    @Value("${sign_name}")
    private String sign_name;

    @Override
    public void sendCode(final String phone) {

        //1.生成一个随机的数字,作为验证码
        StringBuffer sb = new StringBuffer();
        for (int i=0;i<7;i++){
            int s = new Random().nextInt(10);
            sb.append(s);
        }
        //2.手机号作为key,验证码作为value保存到redis中,生存时间为10分钟
        redisTemplate.boundValueOps(phone).set(sb.toString(),60*10, TimeUnit.SECONDS);
        //3.将手机号,短信内容,模板编号,签名封装成map消息发送给消息服务器

        final String smscode = sb.toString();
        jmsTemplate.send(smsDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                MapMessage message = session.createMapMessage();
                message.setString("mobile", phone);//手机号
                message.setString("template_code", template_code);//模板编码
                message.setString("sign_name", sign_name);//签名
                Map map=new HashMap();
                map.put("code", smscode);	//验证码
                message.setString("param", JSON.toJSONString(map));
                return (Message) message;

            }
        });


    }

    @Override
    public Boolean checkSmsCode(String phone, String smscode) {


        if(phone==null||smscode==null||"".equals(phone)||"".equals(smscode)){

            return false;
        }



        String redisSmsCode = (String) redisTemplate.boundValueOps(phone).get();

        if(smscode.equals(redisSmsCode)){
            return true;
        }
        return false;
    }

    @Override
    public void add(User user) {

        userDao.insertSelective(user);

    }
}
