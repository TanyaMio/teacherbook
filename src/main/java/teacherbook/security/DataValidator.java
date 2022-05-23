package teacherbook.security;

import java.util.regex.Pattern;

public class DataValidator {
    private static final String emailRegex = "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
    private static Pattern emailPattern = Pattern.compile(emailRegex);
    private static final String pswdRegex = "((?=.*[a-z])(?=.*\\d)(?=.*[A-Z]).{6,30})";
    private static Pattern pswdPattern = Pattern.compile(pswdRegex);
    private static final String loginRegex = "[a-zA-Z0-9]{6,20}";
    private static Pattern loginPattern = Pattern.compile(loginRegex);
    private static final String timeRegex = "^([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$";
    private static Pattern timePattern = Pattern.compile(timeRegex);

    public DataValidator() {}

    public boolean emailPswdIsValid(String email, String password) {
        return (emailPattern.matcher(email).matches() && pswdPattern.matcher(password).matches());
    }

    public boolean emailIsValid(String email) {
        return emailPattern.matcher(email).matches();
    }

    public boolean loginIsValid(String login) {
        return loginPattern.matcher(login).matches();
    }

    public boolean pswdIsValid (String pswd) {
        return  pswdPattern.matcher(pswd).matches();
    }

    public boolean timeIsValid (String t) {return timePattern.matcher(t).matches();}
}