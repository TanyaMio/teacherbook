package teacherbook.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import teacherbook.model.users.School;
import teacherbook.model.users.TeacherbookUser;
import teacherbook.repositories.users.SchoolRepository;
import teacherbook.repositories.users.UserRepository;
import teacherbook.security.DataValidator;
import teacherbook.security.service.AppUserDetailsService;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Controller
@RequestMapping
public class PageController {

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @Autowired
    private SchoolRepository schoolRepo;

    @Autowired
    private AppUserDetailsService userService;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private SessionRegistry sessionRegistry;

    private DataValidator dataValidator = new DataValidator();

    @GetMapping("/")
    public String index(HttpServletRequest request) {
        return "home_page";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("msg", "admin");
        return "login";
    }

    @GetMapping("/register")
    public String register() { return "registration"; }

    @PostMapping("/register")
    @ResponseBody
    public RedirectView log_res(@RequestParam String email, @RequestParam String name, @RequestParam String password, HttpServletRequest request) {
        RedirectView redirectView = new RedirectView();
        if (dataValidator.emailIsValid(email)) {
            TeacherbookUser existing = userService.findUserByEmail(email);
            if (existing == null) {
                if (dataValidator.emailPswdIsValid(email, password)) {
                    TeacherbookUser newUser = new TeacherbookUser(email, password, true);
                    newUser.setRoles("*SCHOOL_ADMIN*");
                    School newSchool = new School(name);
                    newSchool = schoolRepo.save(newSchool);
                    newUser.setSchool(newSchool);
                    userService.saveUser(newUser);
                    String appUrl = request.getContextPath();
                    //eventPublisher.publishEvent(new OnRegistrationCompleteEvent(newUser, request.getLocale(), appUrl));
                    redirectView.setUrl("http://localhost:8088/login");
                } else {
                    redirectView.setUrl("http://localhost:8088/register?error");
                }
            } else {
                redirectView.setUrl("http://localhost:8088/err?msg=userExists");
            }
        } else {
            redirectView.setUrl("http://localhost:8088/err");
        }
        redirectView.setHosts();
        return redirectView;
    }

    @GetMapping("logged-in/{uid}")
    public RedirectView userhome(@PathVariable String uid, HttpServletRequest request) {
        RedirectView result = new RedirectView();
        Optional<TeacherbookUser> user = userRepo.findById(Long.parseLong(uid));
        SessionInformation sessionInfo = sessionRegistry.getSessionInformation(request.getSession().getId());
        if (sessionInfo != null && !sessionInfo.isExpired()) {
            if (dataValidator.emailIsValid(sessionInfo.getPrincipal().toString())) {
                TeacherbookUser userAuth = userRepo.findByUsername(sessionInfo.getPrincipal().toString());
                if (userAuth != null) {
                    if (user.isPresent()) {
                        TeacherbookUser userFound = user.get();
                        if (userAuth.getId().equals(userFound.getId())) {
                            if (userFound.getRoles().equals("*SCHOOL_ADMIN*")) {
                                result.setUrl("http://localhost:8088/logged-in/" + userFound.getId() + "/admin_home");
                            } else if (userFound.getRoles().contains("*TEACHER*")) {
                                result.setUrl("http://localhost:8088/logged-in/" + userFound.getId() + "/teacher_home");
                            } else if (userFound.getRoles().equals("*PARENT*")){
                                result.setUrl("http://localhost:8088/logged-in/" + userFound.getId() + "/parent_home");
                            } else if (userFound.getRoles().equals("*STUDENT*")){
                                result.setUrl("http://localhost:8088/logged-in/" + userFound.getId() + "/student_home");
                            } else {
                                result.setUrl("http://localhost:8088/err");
                            }
                        } else {
                            result.setUrl("http://localhost:8088/err");
                        }
                    } else {
                        result.setUrl("http://localhost:8088/err");
                    }
                } else {
                    result.setUrl("http://localhost:8088/err");
                }
            } else {
                result.setUrl("http://localhost:8088/err");
            }
        } else {
            result.setUrl("http://localhost:8088/err");
        }
        result.setHosts();
        return result;
    }

    @GetMapping("/logged-in")
    public RedirectView logged_in(HttpServletRequest request) {
        RedirectView result = new RedirectView();
        SecurityContext sc = (SecurityContext) request.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
        Object pr = sc.getAuthentication().getPrincipal();
        if (pr instanceof org.springframework.security.core.userdetails.User) {
            org.springframework.security.core.userdetails.User userAuth = (org.springframework.security.core.userdetails.User) pr;
            sessionRegistry.registerNewSession(request.getSession().getId(), userAuth.getUsername());
            TeacherbookUser user = userRepo.findByUsername(userAuth.getUsername());
            result.setUrl("http://localhost:8088/logged-in/" + user.getId());
            result.setHosts();
            return result;
        }
        result.setUrl("http://localhost:8088/err?msg=userNotFound");
        result.setHosts();
        return result;
    }

}
