package teacherbook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import teacherbook.session.TeacherbookSessionListener;

import javax.servlet.http.HttpSessionListener;

@ServletComponentScan
@SpringBootApplication
public class TeacherBookApp {

    public static void main(String[] args) {
        SpringApplication.run(TeacherBookApp.class, args);
    }

    @Bean
    public ServletListenerRegistrationBean<HttpSessionListener> httpSessionListener() {
        ServletListenerRegistrationBean<HttpSessionListener> listenerRegBean =
                new ServletListenerRegistrationBean<>();

        listenerRegBean.setListener(new TeacherbookSessionListener());
        return listenerRegBean;
    }
}