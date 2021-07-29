package kr.stteam.TwtRoute.util;
import org.springframework.context.ApplicationContext;

public class BeanUtils {
    public static Object getBean(String beanId) {

        ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        if( applicationContext == null ) {
            throw new NullPointerException("Spring ApplicationContext is not initialized.(Null pointer exception)");
        }
        /*
        String[] names = applicationContext.getBeanDefinitionNames();
        for (int i=0; i<names.length; i++) {
            System.out.println(names[i]);
        }
        */
        return applicationContext.getBean(beanId);
    }
}
