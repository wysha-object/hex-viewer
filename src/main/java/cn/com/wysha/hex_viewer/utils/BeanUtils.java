package cn.com.wysha.hex_viewer.utils;

import org.springframework.context.ConfigurableApplicationContext;

public class BeanUtils {
    private static ConfigurableApplicationContext applicationContext;

    public static void setApplicationContext (ConfigurableApplicationContext context) {
        if (applicationContext != null) throw new IllegalStateException();
        if (context == null) throw new IllegalArgumentException();
        applicationContext = context;
    }

    public static ConfigurableApplicationContext getApplicationContext () {
        if (applicationContext == null) throw new IllegalStateException();
        return applicationContext;
    }

    public static <T> void registerBean (String beanName, T bean) {
        applicationContext.getBeanFactory().registerSingleton(beanName, bean);
    }

    public static <T> void registerBean (T bean) {
        registerBean(bean.getClass().toString(), bean);
    }

    public static <T> T getBean (Class<T> tClass) {
        if (applicationContext == null) throw new IllegalStateException();
        return applicationContext.getBean(tClass);
    }
}
