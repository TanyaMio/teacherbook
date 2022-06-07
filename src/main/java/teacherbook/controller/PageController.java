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
import teacherbook.email.events.pswdReset.OnPasswordResetEvent;
import teacherbook.email.events.registration.OnRegistrationCompleteEvent;
import teacherbook.model.VerificationToken;
import teacherbook.model.users.School;
import teacherbook.model.users.Teacher;
import teacherbook.model.users.TeacherbookUser;
import teacherbook.repositories.users.SchoolRepository;
import teacherbook.repositories.users.UserRepository;
import teacherbook.security.DataValidator;
import teacherbook.security.service.AppUserDetailsService;
import teacherbook.services.IUserService;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.Optional;

@Controller
@RequestMapping
public class PageController {

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @Autowired
    private SchoolRepository schoolRepo;

    @Autowired
    private IUserService service;
    @Autowired
    private AppUserDetailsService userService;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private SessionRegistry sessionRegistry;

    private DataValidator dataValidator = new DataValidator();

    @GetMapping("/")
    public RedirectView index(HttpServletRequest request) {
        RedirectView redirectView = new RedirectView();
        SecurityContext sc = (SecurityContext) request.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
        if (sc != null) {
            Object pr = sc.getAuthentication().getPrincipal();
            if (pr instanceof org.springframework.security.core.userdetails.User) {
                org.springframework.security.core.userdetails.User userAuth = (org.springframework.security.core.userdetails.User) pr;
                sessionRegistry.registerNewSession(request.getSession().getId(), userAuth.getUsername());
                TeacherbookUser user = userRepo.findByUsername(userAuth.getUsername());
                redirectView.setUrl("http://localhost:8088/logged-in/" + user.getId());
                redirectView.setHosts();
                return redirectView;
            }
        }
        String sessionID = request.getSession().getId();
        SessionInformation sessionInfo = sessionRegistry.getSessionInformation(sessionID);
        if (sessionInfo != null) {
            String email = sessionInfo.getPrincipal().toString();
            if (dataValidator.emailIsValid(email)) {
                TeacherbookUser userFound = userRepo.findByUsername(email);
                if (userFound != null) {
                    redirectView.setUrl("http://localhost:8088/logged-in/" + userFound.getId());
                } else {
                    redirectView.setUrl("http://localhost:8088/home");
                }
            } else {
                redirectView.setUrl("http://localhost:8088/home");
            }
        } else {
            redirectView.setUrl("http://localhost:8088/home");
        }
        redirectView.setHosts();
        return redirectView;
    }

    @GetMapping ("/home")
    public String home() {
        return "home_page";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("msg", "admin");
        return "login";
    }

    @GetMapping("/register")
    public String register() { return "registration"; }

    @GetMapping("/registrationConfirm")
    public RedirectView confirmRegistration(@RequestParam String token) {
        RedirectView result = new RedirectView();
        VerificationToken verificationToken = service.getVerificationToken(token);
        if(verificationToken == null) {
            result.setUrl("http://localhost:8088/err?msg=badToken");
            result.setHosts();
            return result;
        }
        TeacherbookUser user = service.getUser(token);
        Calendar cal = Calendar.getInstance();
        if((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            result.setUrl("http://localhost:8088/err?msg=tokenExpired");
            result.setHosts();
            return result;
        }
        user.setEnabled(true);
        service.saveRegisteredUser(user);
        result.setUrl("http://localhost:8088/login");
        result.setHosts();
        return result;
    }

    @GetMapping("/pswd_reset")
    public String resetPswd() { return "pswd_reset_req"; }

    @GetMapping("/pswd_reset/{id}/passwordResetConfirm")
    public String resetPswdPage(@PathVariable String id, @RequestParam String token, Model model) {
        VerificationToken verificationToken = service.getVerificationToken(token);
        if(verificationToken == null) {
            model.addAttribute("msg", "Bad token. Please, make sure to use the appropriate link.");
            model.addAttribute("loggedin", false);
            return "errorpage";
        }
        TeacherbookUser user = service.getUser(token);
        Calendar cal = Calendar.getInstance();
        if((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            model.addAttribute("msg", "Token expired. Please, request a new link.");
            model.addAttribute("loggedin", false);
            return "errorpage";
        }
        return "pswd_reset_form";
    }

    @PostMapping("/register")
    @ResponseBody
    public RedirectView log_res(@RequestParam String email, @RequestParam String name, @RequestParam String password, HttpServletRequest request) {
        RedirectView redirectView = new RedirectView();
        if (dataValidator.emailIsValid(email)) {
            TeacherbookUser existing = userService.findUserByEmail(email);
            if (existing == null) {
                if (dataValidator.emailPswdIsValid(email, password)) {
                    TeacherbookUser newUser = new TeacherbookUser(email, password, false);
                    newUser.setRoles("*SCHOOL_ADMIN*");
                    School newSchool = new School(name);
                    newSchool = schoolRepo.save(newSchool);
                    newUser.setSchool(newSchool);
                    userService.saveUser(newUser);
                    String appUrl = request.getContextPath();
                    eventPublisher.publishEvent(new OnRegistrationCompleteEvent(newUser, request.getLocale(), appUrl));
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

    @PostMapping("/pswd_reset/{id}/passwordResetConfirm")
    @ResponseBody
    public RedirectView pswd_reset_apply(@PathVariable Long id, @RequestParam String password, HttpServletRequest request) {
        Optional<TeacherbookUser> existing = userRepo.findById(id);
        RedirectView redirectView = new RedirectView();
        if (existing.get() == null) {
            redirectView.setUrl("http://localhost:8088/err?msg=userNotFound");
            redirectView.setHosts();
            return redirectView;
        } else {
            TeacherbookUser usertoUpdate = existing.get();
            if (dataValidator.pswdIsValid(password)) {
                usertoUpdate.setPasshash(password);
                usertoUpdate.setEnabled(true);
                userService.saveUser(usertoUpdate);
            }
        }
        redirectView.setUrl("http://localhost:8088/login");
        redirectView.setHosts();
        return redirectView;
    }

    @PostMapping("/pswd_reset")
    @ResponseBody
    public RedirectView pswd_reset(@RequestParam String email, HttpServletRequest request) {
        TeacherbookUser existing = userService.findUserByEmail(email);
        RedirectView redirectView = new RedirectView();
        if (existing == null) {
            redirectView.setUrl("http://localhost:8088/err?msg=userNotFound");
            redirectView.setHosts();
            return redirectView;
        }
        eventPublisher.publishEvent(new OnPasswordResetEvent(existing, request.getLocale(), request.getContextPath()));
        existing.setEnabled(false);
        userRepo.save(existing);
        redirectView.setUrl("http://localhost:8088/login");
        redirectView.setHosts();
        return redirectView;
    }


    @GetMapping("logged-in/{uid}")
    public RedirectView userhome(@PathVariable String uid, HttpServletRequest request) {
        RedirectView result = new RedirectView();
        Optional<TeacherbookUser> user = userRepo.findById(Long.parseLong(uid));
        SessionInformation sessionInfo = sessionRegistry.getSessionInformation(request.getSession().getId());
        if (sessionInfo != null && !sessionInfo.isExpired()) {
            String uname = sessionInfo.getPrincipal().toString();
            if (dataValidator.emailIsValid(uname) ||
            dataValidator.loginIsValid(uname.substring(0, uname.lastIndexOf("_")))) {
                TeacherbookUser userAuth = userRepo.findByUsername(uname);
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
