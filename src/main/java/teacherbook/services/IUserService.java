package teacherbook.services;

import org.apache.catalina.User;
import teacherbook.model.VerificationToken;
import teacherbook.model.users.TeacherbookUser;

public interface IUserService {
    TeacherbookUser registerUser(TeacherbookUser user);
    TeacherbookUser getUser(String verificationToken);
    void saveRegisteredUser(TeacherbookUser user);
    void createVerificationToken(TeacherbookUser user, String token);
    VerificationToken getVerificationToken(String token);
}