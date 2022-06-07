package teacherbook.email.events.pswdReset;

import org.springframework.context.ApplicationEvent;
import teacherbook.model.users.TeacherbookUser;

import java.util.Locale;

public class OnPasswordResetEvent extends ApplicationEvent {
    private String appUrl;
    private Locale locale;
    private TeacherbookUser user;

    public OnPasswordResetEvent(
            TeacherbookUser user, Locale locale, String appUrl) {
        super(user);
        this.user = user;
        this.locale = locale;
        this.appUrl = appUrl;
    }

    public String getAppUrl() {
        return appUrl;
    }

    public void setAppUrl(String appUrl) {
        this.appUrl = appUrl;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public TeacherbookUser getUser() {
        return user;
    }

    public void setUser(TeacherbookUser user) {
        this.user = user;
    }

}