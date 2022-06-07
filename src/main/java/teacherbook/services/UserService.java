package teacherbook.services;

import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import teacherbook.model.VerificationToken;
import teacherbook.model.users.TeacherbookUser;
import teacherbook.repositories.VerificationTokenRepository;
import teacherbook.repositories.users.UserRepository;

import javax.transaction.Transactional;

@Service
@Transactional
public class UserService implements IUserService{

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private VerificationTokenRepository tokenRepo;

    @Override
    public TeacherbookUser registerUser(TeacherbookUser user) {
        return null;
    }

    @Override
    public TeacherbookUser getUser(String verificationToken) {
        TeacherbookUser user = userRepo.findById(tokenRepo.findByToken(verificationToken).getUserId()).get();
        return user;
    }

    @Override
    public void saveRegisteredUser(TeacherbookUser user) {
        userRepo.save(user);
    }

    @Override
    public void createVerificationToken(TeacherbookUser user, String token) {
        VerificationToken newToken = new VerificationToken(token, user.getId());
        tokenRepo.save(newToken);
    }

    @Override
    public VerificationToken getVerificationToken(String token) {
        return tokenRepo.findByToken(token);
    }
}
