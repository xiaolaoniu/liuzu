package cn.itcast;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class StatProvider {
    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext app = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext-*.xml");
        app.start();
        System.in.read();//代码不继续运行，在控制台回车就继续
    }
}
