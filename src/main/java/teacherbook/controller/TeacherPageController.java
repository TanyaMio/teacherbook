package teacherbook.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import teacherbook.model.users.TeacherbookUser;
import teacherbook.repositories.users.UserRepository;
import teacherbook.security.DataValidator;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Controller
public class TeacherPageController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private SessionRegistry sessionRegistry;

    private DataValidator dataValidator = new DataValidator();

    @GetMapping("logged-in/{uid}/teacher_home")
    public String teacher_home(@PathVariable String uid, HttpServletRequest request) {
        Optional<TeacherbookUser> user = userRepo.findById(Long.parseLong(uid));
        SessionInformation sessionInfo = sessionRegistry.getSessionInformation(request.getSession().getId());
        if (sessionInfo != null && !sessionInfo.isExpired()) {
            if (dataValidator.emailIsValid(sessionInfo.getPrincipal().toString())) {
                TeacherbookUser userAuth = userRepo.findByUsername(sessionInfo.getPrincipal().toString());
                if (userAuth != null) {
                    if (user.isPresent()) {
                        TeacherbookUser userFound = user.get();
                        if (userAuth.getId().equals(userFound.getId())) {
                            if (userFound.getRoles().contains("*TEACHER*")) {
                                return "teacher_home";
                            } else {
                                return "err";
                            }
                        } else {
                            return "err";
                        }
                    } else {
                        return "err";
                    }
                } else {
                    return "err";
                }
            } else {
                return "err";
            }
        } else {
            return "err";
        }
    }
}
