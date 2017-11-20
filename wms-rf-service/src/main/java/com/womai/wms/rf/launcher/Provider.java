package com.womai.wms.rf.launcher;


import com.womai.wms.rf.manager.auth.login.BaseShellManagerImpl;
import com.womai.wms.rf.service.TelnetService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: zhangqingyun@cofco.com
 * Date: 12-11-8
 * Time: 下午3:42
 * To change this template use File | Settings | File Templates.
 */
public class Provider {

    private static Log logger = LogFactory.getLog(Provider.class);
    private static volatile boolean running = true;
    private static ApplicationContext ctx;

    public static void main(String[] args) {

        try {
            ctx = new ClassPathXmlApplicationContext(
                    new String[]{
                            "applicationContext.xml",
                            "log4j.xml"
//                             "spring-config-dubbo.xml",
//                             "spring-config-jedis.xml",
//                            "spring-config-netty.xml"
                    }
            );
            logger.info(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]").format(new Date()) + " RF service server started!");
            TelnetService telnetService = ctx.getBean(TelnetService.class);
//            TelnetService telnetService = new TelnetService();
            telnetService.init();


//            TelnetService test  = (TelnetService) ctx.getBean("telnetService");
//            System.out.println(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]").format(new Date()) + " Dubbo service server started!");
//            test.init();

        } catch (RuntimeException e) {
            e.printStackTrace();
            running = false;
            logger.error(e.getMessage(), e);
            System.exit(1);
        }
        synchronized (Provider.class) {
            while (running) {
                try {
                    Provider.class.wait();
                } catch (Throwable e) {
                }
            }
        }
    }

}
