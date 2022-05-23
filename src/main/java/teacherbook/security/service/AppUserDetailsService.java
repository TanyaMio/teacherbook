package teacherbook.security.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import teacherbook.model.users.TeacherbookUser;
import teacherbook.repositories.users.UserRepository;

import java.util.ArrayList;

@Service
public class AppUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private final PasswordEncoder passwordEncoder;


    public AppUserDetailsService(PasswordEncoder pE) {
        this.passwordEncoder = pE;
    }

    public TeacherbookUser findUserByEmail(String uname) {
        return userRepo.findByUsername(uname);
    }

    public void saveUser(TeacherbookUser user) {
        user.setPasshash(passwordEncoder.encode(user.getPasshash()));
        userRepo.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String uname) throws UsernameNotFoundException {
        TeacherbookUser user = userRepo.findByUsername(uname);
        if(user != null) {
            UserDetails userDetails = buildUserForAuthentication(user, new ArrayList<GrantedAuthority>());
            return userDetails;
        } else {
            throw new UsernameNotFoundException("username not found");
        }
    }

    private UserDetails buildUserForAuthentication(TeacherbookUser user, ArrayList<GrantedAuthority> grantedAuthorities) {
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPasshash(), user.isEnabled(), true, true, true, grantedAuthorities);
    }
}
