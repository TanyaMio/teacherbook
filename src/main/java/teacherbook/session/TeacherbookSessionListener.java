package teacherbook.session;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.time.Duration;

@WebListener
public class TeacherbookSessionListener implements HttpSessionListener {

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        event.getSession().setMaxInactiveInterval(Integer.parseInt(String.valueOf(Duration.ofDays(30))));
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
    }
}