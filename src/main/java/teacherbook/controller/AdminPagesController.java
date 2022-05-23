package teacherbook.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import teacherbook.model.Course;
import teacherbook.model.StudentGroup;
import teacherbook.model.schedulegrid.*;
import teacherbook.model.users.School;
import teacherbook.model.users.Student;
import teacherbook.model.users.Teacher;
import teacherbook.model.users.TeacherbookUser;
import teacherbook.repositories.*;
import teacherbook.repositories.users.StudentRepository;
import teacherbook.repositories.users.TeacherRepository;
import teacherbook.repositories.users.UserRepository;
import teacherbook.security.DataValidator;
import teacherbook.security.service.AppUserDetailsService;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.*;

@Controller
public class AdminPagesController {

    @Autowired
    private AppUserDetailsService userService;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private TeacherRepository teacherRepo;
    @Autowired
    private StudentRepository studentRepo;

    @Autowired
    private StudentGroupRepository groupRepo;

    @Autowired
    private SemesterRepository semesterRepo;
    @Autowired
    private RotationDayRepository rotationRepo;
    @Autowired
    private CalendarDayRepository calendarRepo;
    @Autowired
    private TimeslotRepository timeslotRepo;
    @Autowired
    private CourseRepository courseRepo;
    @Autowired
    private ScheduleEntryRepository scheduleRepo;

    @Autowired
    private SessionRegistry sessionRegistry;

    private DataValidator dataValidator = new DataValidator();

    @GetMapping("logged-in/{uid}/admin_home")
    public String admin_home(@PathVariable Long uid, HttpServletRequest request) {
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            return "school_admin_home";
        } else {
            return checkAuthResult;
        }
    }

    @GetMapping("logged-in/{uid}/members/teachers")
    public String teachers(@PathVariable Long uid, Model model, HttpServletRequest request) {
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            TeacherbookUser userFound = userRepo.findById(uid).get();
            model.addAttribute("teachers", userFound.getSchool().getTeachers());
            return "manage_teachers";
        } else {
            return checkAuthResult;
        }
    }

    @GetMapping("logged-in/{uid}/members/students")
    public String students(@PathVariable Long uid, Model model, HttpServletRequest request) {
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            TeacherbookUser userFound = userRepo.findById(uid).get();
            model.addAttribute("students", userFound.getSchool().getStudents());
            return "manage_students";
        } else {
            return checkAuthResult;
        }
    }

    @GetMapping("logged-in/{uid}/semesters/list")
    public String semesters(@PathVariable Long uid, Model model, HttpServletRequest request) {
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            TeacherbookUser userFound = userRepo.findById(uid).get();
            model.addAttribute("semesters", userFound.getSchool().getSemesters());
            return "manage_semesters";
        } else {
            return checkAuthResult;
        }
    }

    @GetMapping("logged-in/{uid}/courses/list")
    public String courses(@PathVariable Long uid, Model model, HttpServletRequest request) {
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            TeacherbookUser userFound = userRepo.findById(uid).get();
            Collection<Course> courses = userFound.getSchool().getCourses();
            model.addAttribute("courses", courses);
            return "manage_courses";
        } else {
            return checkAuthResult;
        }
    }

    @GetMapping("logged-in/{uid}/members/teacher_invite")
    public String teacher_invite(@PathVariable Long uid, HttpServletRequest request) {
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            return "teacher_registration";
        } else {
            return checkAuthResult;
        }
    }

    @GetMapping("logged-in/{uid}/semesters/create_semester")
    public String create_semester(@PathVariable Long uid, HttpServletRequest request) {
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            return "semester_create";
        } else {
            return checkAuthResult;
        }
    }

    @GetMapping("logged-in/{uid}/semester/{sid}/rotation_config")
    public String rotation_rename(@PathVariable Long uid, @PathVariable Long sid,
                                    Model model, HttpServletRequest request) {
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            Optional<Semester> op_semester = semesterRepo.findById(sid);
            if (op_semester.isPresent() &&
                    op_semester.get().getSchool().equals(userRepo.findById(uid).get().getSchool())) {
                Semester semester = op_semester.get();
                List<CalendarDay> calendar = calendarRepo.findAllBySemester(semester);
                calendar.sort(CalendarDay::compareTo);
                ArrayList<Map<String, ArrayList<ArrayList<String>>>> table_data = new ArrayList<>();
                String current_month;
                int row_ind = 0;
                String[] weekday_names = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
                LocalDate date;
                Map<String, ArrayList<ArrayList<String>>> calendar_table = null;
                int rotation_len = semester.getRotation_length();
                int workdays_fetched = 0;
                for (int d=0; d < calendar.size() && workdays_fetched < rotation_len; d++) {
                    date = calendar.get(d).getDate().toLocalDate();
                    current_month = date.getMonth().getDisplayName(TextStyle.FULL, request.getLocale());
                    if (calendar_table == null || !calendar_table.containsKey(current_month)) {
                        if (calendar_table != null) {
                            table_data.add(calendar_table);
                        }
                        calendar_table = new HashMap<>();
                        calendar_table.put(current_month, new ArrayList<>());
                        calendar_table.get(current_month).add(new ArrayList<String>());
                        calendar_table.get(current_month).add(new ArrayList<String>());
                        calendar_table.get(current_month).get(0).addAll(Arrays.asList(weekday_names));
                        row_ind = 1;
                    }
                    calendar_table.get(current_month).get(row_ind).add(calendar.get(d).toString());
                    if(calendar.get(d).getRotation_day() != null) {
                        workdays_fetched++;
                    }
                    if(date.getDayOfWeek().getValue() == 7 &&
                            (d < calendar.size()-1 && calendar.get(d+1).getDate().toLocalDate().getMonth() == date.getMonth())) {
                        calendar_table.get(current_month).add(new ArrayList<String>());
                        while (calendar_table.get(current_month).get(row_ind).size() < 7) {
                            calendar_table.get(current_month).get(row_ind).add(0, "");
                        }
                        row_ind++;
                    }
                }
                if (calendar_table != null && !table_data.contains(calendar_table)) {
                    table_data.add(calendar_table);
                }
                model.addAttribute("calendars", table_data);
                ArrayList<RotationDay> rotation = new ArrayList<>(semester.getRotation());
                rotation.sort(RotationDay::compareTo);
                model.addAttribute("rotation", rotation);
                return "rotation_rename";
            } else {
                return "err";
            }
        } else {
            return checkAuthResult;
        }
    }

    @GetMapping("logged-in/{uid}/semester/{sid}/edit_timeslots")
    public String edit_timeslots(@PathVariable Long uid, @PathVariable Long sid,
                                  Model model, HttpServletRequest request) {
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            Optional<Semester> op_semester = semesterRepo.findById(sid);
            if (op_semester.isPresent() &&
                    op_semester.get().getSchool().equals(userRepo.findById(uid).get().getSchool())) {
                Semester semester = op_semester.get();
                ArrayList<Timeslot> timeslots = new ArrayList<>(semester.getTimeslots());
                timeslots.sort(Timeslot::compareTo);
                model.addAttribute("timeslots", timeslots);
                return "timeslots_edit";
            } else {
                return "err";
            }
        } else {
            return checkAuthResult;
        }
    }

    @GetMapping("logged-in/{uid}/semester/{sid}/calendar")
    public String semester_calendar(@PathVariable Long uid, @PathVariable String sid,
                                    Model model, HttpServletRequest request) {
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            Optional<Semester> op_semester = semesterRepo.findById(Long.parseLong(sid));
            if (op_semester.isPresent() &&
                    op_semester.get().getSchool().equals(userRepo.findById(uid).get().getSchool())) {
                Semester semester = op_semester.get();
                List<CalendarDay> calendar = calendarRepo.findAllBySemester(semester);
                calendar.sort(CalendarDay::compareTo);
                ArrayList<Map<String, ArrayList<ArrayList<String>>>> table_data = new ArrayList<>();
                String current_month;
                int row_ind = 0;
                String[] weekday_names = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
                LocalDate date;
                Map<String, ArrayList<ArrayList<String>>> calendar_table = null;
                for (int d=0; d < calendar.size(); d++) {
                    date = calendar.get(d).getDate().toLocalDate();
                    current_month = date.getMonth().getDisplayName(TextStyle.FULL, request.getLocale());
                    if (calendar_table == null || !calendar_table.containsKey(current_month)) {
                        if (calendar_table != null) {
                            table_data.add(calendar_table);
                        }
                        calendar_table = new HashMap<>();
                        calendar_table.put(current_month, new ArrayList<>());
                        calendar_table.get(current_month).add(new ArrayList<String>());
                        calendar_table.get(current_month).add(new ArrayList<String>());
                        calendar_table.get(current_month).get(0).addAll(Arrays.asList(weekday_names));
                        row_ind = 1;
                    }
                    calendar_table.get(current_month).get(row_ind).add(calendar.get(d).toString());
                    if(date.getDayOfWeek().getValue() == 7 &&
                            (d < calendar.size()-1 && calendar.get(d+1).getDate().toLocalDate().getMonth() == date.getMonth())) {
                        calendar_table.get(current_month).add(new ArrayList<String>());
                        while (calendar_table.get(current_month).get(row_ind).size() < 7) {
                            calendar_table.get(current_month).get(row_ind).add(0, "");
                        }
                        row_ind++;
                    }
                }
                if (calendar_table != null && !table_data.contains(calendar_table)) {
                    table_data.add(calendar_table);
                }
                model.addAttribute("calendars", table_data);
                School school = userRepo.findById(uid).get().getSchool();
                ArrayList<StudentGroup> groups = new ArrayList<>(school.getGroups());
                ArrayList<String> groupnames = new ArrayList<>();
                for (StudentGroup g: groups) {
                    groupnames.add(g.getName());
                }
                model.addAttribute("groupnames", groupnames);
                ArrayList<Teacher> teachers = new ArrayList<>(school.getTeachers());
                ArrayList<String> teachernames = new ArrayList<>();
                for (Teacher t: teachers) {
                    teachernames.add(t.getFullname());
                }
                model.addAttribute("teachernames", teachernames);
                return "semester_calendar";
            } else {
                return "err";
            }
        } else {
            return checkAuthResult;
        }
    }

    @GetMapping("/logged-in/{uid}/groups/list")
    public String groups_list(@PathVariable Long uid, Model model, HttpServletRequest request) {
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            School school = userRepo.findById(uid).get().getSchool();
            ArrayList<StudentGroup> groups = new ArrayList<StudentGroup>(school.getGroups());
            ArrayList<Student> students = new ArrayList<>(school.getStudents());
            ArrayList<String> studentnames = new ArrayList<>();
            ArrayList<Long> studentids = new ArrayList<>();
            for (Student s: students) {
                studentnames.add(s.getFullname());
                studentids.add(s.getStudent_id());
            }
            model.addAttribute("studentnames", studentnames);
            model.addAttribute("studentvalues", studentids);
            model.addAttribute("groups", groups);
            return "manage_groups";
        } else {
            return checkAuthResult;
        }
    }

    @GetMapping("/logged-in/{uid}/group/{gid}/group_view")
    public String group_view(@PathVariable Long uid, @PathVariable Long gid,
                             Model model, HttpServletRequest request){
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            School school = userRepo.findById(uid).get().getSchool();
            Optional<StudentGroup> group = groupRepo.findById(gid);
            if (group.isPresent() && group.get().getSchool().equals(school)) {
                StudentGroup groupFound = group.get();
                model.addAttribute("group", groupFound);
                ArrayList<Student> students = new ArrayList<>(school.getStudents());
                ArrayList<String> studentnames = new ArrayList<>();
                ArrayList<Long> studentids = new ArrayList<>();
                for (Student s: students) {
                    if (!s.getGroups().contains(groupFound)) {
                        studentnames.add(s.getFullname());
                        studentids.add(s.getStudent_id());
                    }
                }
                model.addAttribute("studentnames", studentnames);
                model.addAttribute("studentvalues", studentids);
                return "group_view";
            } else {
                return "err";
            }
        } else {
            return checkAuthResult;
        }
    }

    @GetMapping("logged-in/{uid}/group/{gid}/remove_student/{sid}")
    public RedirectView remove_from_group(@PathVariable Long uid, @PathVariable Long gid,
                                          @PathVariable Long sid,
                                          HttpServletRequest request) {
        RedirectView redirectView = new RedirectView();
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            School school = userRepo.findById(uid).get().getSchool();
            Optional<StudentGroup> group = groupRepo.findById(gid);
            Optional<Student> student = studentRepo.findById(sid);
            if (group.isPresent() && group.get().getSchool().equals(school) &&
                    student.isPresent() && student.get().getSchool().equals(school)) {
                Student studentFound = student.get();
                studentFound.removeGroup(group.get());
                studentRepo.save(studentFound);
                redirectView.setUrl("http://localhost:8088/logged-in/" + uid + "/group/" + gid + "/group_view");
            } else {
                redirectView.setUrl("http://localhost:8088/err");
            }
        } else {
            redirectView.setUrl("http://localhost:8088/err");
        }
        redirectView.setHosts();
        return redirectView;
    }

    @GetMapping("logged-in/{uid}/semester/{sid}/schedule_for/{for_a}/{for_id}/edit")
    public String schedule_edit(@PathVariable Long uid, @PathVariable Long sid,
                                @PathVariable String for_a, @PathVariable Long for_id,
                                Model model, HttpServletRequest request) {
        if (!(for_a.equals("group") || for_a.equals("teacher"))) {
            return "err";
        } else if (for_a.equals("group") && !groupRepo.findById(for_id).isPresent()) {
            return "err";
        } else if (for_a.equals("teacher") && !teacherRepo.findById(for_id).isPresent()) {
            return "err";
        }
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            Optional<Semester> op_semester = semesterRepo.findById(sid);
            if (op_semester.isPresent() &&
                    op_semester.get().getSchool().equals(userRepo.findById(uid).get().getSchool())) {
                School school = userRepo.findById(uid).get().getSchool();
                if ((for_a.equals("group") && !groupRepo.findById(for_id).get().getSchool().equals(school)) ||
                        (for_a.equals("teacher") && !teacherRepo.findById(for_id).get().getSchool().equals(school))) {
                    return "err";
                }
                Semester semester = op_semester.get();
                List<CalendarDay> calendar = calendarRepo.findAllBySemester(semester);
                calendar.sort(CalendarDay::compareTo);
                String[] header = {"", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
                LocalDate date;
                ArrayList<ArrayList<Object>> calendar_table = new ArrayList<>();
                calendar_table.add(new ArrayList<>(Arrays.asList(header)));
                calendar_table.add(new ArrayList<>(Arrays.asList(new Object[8])));
                calendar_table.get(1).set(0, "");
                ArrayList<Timeslot> timeslots = new ArrayList<>(semester.getTimeslots());
                timeslots.sort(Timeslot::compareTo);
                int row_ind = 1;
                for (int i=1; i <= timeslots.size(); i++) {
                    calendar_table.add(new ArrayList<>(Arrays.asList(new Object[8])));
                    calendar_table.get(row_ind+i).set(0, timeslots.get(i-1));
                }
                int rotation_len = semester.getRotation_length();
                int workdays_fetched = 0;
                int col_ind = 0;
                for (int d=0; d < calendar.size() && workdays_fetched < rotation_len; d++) {
                    date = calendar.get(d).getDate().toLocalDate();
                    col_ind = date.getDayOfWeek().getValue();
                    String date_str = date.getMonth().getDisplayName(TextStyle.SHORT, request.getLocale()) + " "
                            + date.getDayOfMonth();
                    if (calendar.get(d).getRotation_day() != null) {
                        date_str += "\n" + calendar.get(d).getRotation_day().getName();
                        calendar_table.get(row_ind).set(col_ind, date_str);
                        ArrayList<ScheduleEntry> schedule;
                        if (for_a.equals("group")) {
                            schedule = new ArrayList<>(scheduleRepo.findAllBySemesterAndGroupAndRotationDay(semester,
                                    groupRepo.findById(for_id).get(), calendar.get(d).getRotation_day()));
                        } else {
                            schedule = new ArrayList<>(scheduleRepo.findAllBySemesterAndTeacherAndRotationDay(semester,
                                    teacherRepo.findById(for_id).get(), calendar.get(d).getRotation_day()));
                        }
                        schedule.sort(ScheduleEntry::compareTo);
                        for (ScheduleEntry se: schedule) {
                            int inc = timeslots.indexOf(se.getTimeslot()) + 1;
                            calendar_table.get(row_ind+inc).set(col_ind, se);
                        }
                    } else {
                        calendar_table.get(row_ind).set(col_ind, date_str);
                        for (int i = 1; i <= timeslots.size(); i++) {
                            calendar_table.get(row_ind + i).set(col_ind, "");
                        }
                    }
                    //end-of-week actions
                }
                model.addAttribute("calendars", calendar_table);
                ArrayList<RotationDay> rotation = new ArrayList<>(semester.getRotation());
                rotation.sort(RotationDay::compareTo);
                model.addAttribute("rotation", rotation);
                return "rotation_rename";
            } else {
                return "err";
            }
        } else {
            return checkAuthResult;
        }
    }

    @PostMapping("/logged-in/{uid}/members/teacher_invite")
    @ResponseBody
    public RedirectView teacher_reg(@PathVariable Long uid,
                                    @RequestParam String email,
                                    @RequestParam String name1, @RequestParam String name2,
                                    @RequestParam String password,
                                    HttpServletRequest request) {
        RedirectView redirectView = new RedirectView();
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**") &&
                !email.isBlank() && !password.isBlank() && !name1.isBlank() && !name2.isBlank()) {
            TeacherbookUser userFound = userRepo.findById(uid).get();
            if (dataValidator.emailIsValid(email)) {
                TeacherbookUser existing = userService.findUserByEmail(email);
                if (existing == null) {
                    if (dataValidator.emailPswdIsValid(email, password)) {
                        TeacherbookUser newUser = new TeacherbookUser(email, password, true);
                        newUser.setRoles("*TEACHER*");
                        Teacher newTeacher = new Teacher(name1, name2);
                        newTeacher.setSchool(userFound.getSchool());
                        newTeacher = teacherRepo.save(newTeacher);
                        newUser.setTeacher(newTeacher);
                        userService.saveUser(newUser);
                        String appUrl = request.getContextPath();
                        //eventPublisher.publishEvent(new OnRegistrationCompleteEvent(newUser, request.getLocale(), appUrl));
                        redirectView.setUrl("http://localhost:8088/logged-in/" + uid + "/members/teachers");
                    } else {
                        redirectView.setUrl("http://localhost:8088/logged-in/" + uid + "/members/teachers?error");
                    }
                } else if (existing.getRoles().equals("*PARENT*")) {
                    Teacher newTeacher = new Teacher(name1, name2);
                    newTeacher.setSchool(userFound.getSchool());
                    newTeacher = teacherRepo.save(newTeacher);
                    existing.setTeacher(newTeacher);
                    existing.setRoles("*PARENT**TEACHER*");
                    userRepo.save(existing);
                    redirectView.setUrl("http://localhost:8088/logged-in/" + uid + "/members/teachers");
                } else {
                    redirectView.setUrl("http://localhost:8088/err?msg=userExists");
                }
            }
        } else {
            String url = "http://localhost:8088/" + checkAuthResult;
            redirectView.setUrl(url);
        }
        redirectView.setHosts();
        return redirectView;
    }

    @PostMapping("logged-in/{uid}/members/students")
    @ResponseBody
    public RedirectView student_reg(@PathVariable Long uid,
                                    @RequestParam String login,
                                    @RequestParam String name1, @RequestParam String name2,
                                    @RequestParam String password,
                                    HttpServletRequest request) {
        RedirectView redirectView = new RedirectView();
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**") &&
                !login.isBlank() && !password.isBlank() && !name1.isBlank() && !name2.isBlank()) {
            TeacherbookUser userFound = userRepo.findById(uid).get();
            if (dataValidator.loginIsValid(login)) {
                login = login+"_"+userFound.getSchool().getSchool_id();
                TeacherbookUser existing = userRepo.findByUsernameAndSchool(login, userFound.getSchool());
                if (existing == null) {
                    if (dataValidator.pswdIsValid(password)) {
                        TeacherbookUser newUser = new TeacherbookUser(login, password, true);
                        newUser.setRoles("*STUDENT*");
                        Student newStudent = new Student(name1, name2);
                        newStudent.setSchool(userFound.getSchool());
                        newStudent = studentRepo.save(newStudent);
                        newUser.setStudent(newStudent);
                        userService.saveUser(newUser);
                        redirectView.setUrl("http://localhost:8088/logged-in/" + uid + "/members/students");
                    } else {
                        redirectView.setUrl("http://localhost:8088/logged-in/" + uid + "/members/students?error");
                    }
                } else {
                    redirectView.setUrl("http://localhost:8088/err?msg=userExists");
                }
            } else {
                redirectView.setUrl("http://localhost:8088/logged-in/" + uid + "/members/students?error");
            }
        } else {
            String url = "http://localhost:8088/" + checkAuthResult;
            redirectView.setUrl(url);
        }
        redirectView.setHosts();
        return redirectView;
    }

    @PostMapping("/logged-in/{uid}/semesters/create_semester")
    @ResponseBody
    public RedirectView new_semester(@PathVariable Long uid,
                                     @RequestParam String name,
                                     @RequestParam String start_date, @RequestParam String end_date,
                                     @RequestParam int rotation_len,
                                     @RequestParam int n_timeslots,
                                     @RequestParam String[] workdays,
                                     HttpServletRequest request) {
        RedirectView redirectView = new RedirectView();
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**") &&
                !name.isBlank() && !start_date.isBlank() && !end_date.isBlank() &&
                rotation_len > 0 && n_timeslots > 0 && workdays.length > 0) {
            TeacherbookUser userFound = userRepo.findById(uid).get();
            String working = "";
            if(Arrays.asList(workdays).contains("monday")) {
                working += "MONDAY_";
            }
            if(Arrays.asList(workdays).contains("tuesday")) {
                working += "TUESDAY_";
            }
            if(Arrays.asList(workdays).contains("wednesday")) {
                working += "WEDNESDAY_";
            }
            if(Arrays.asList(workdays).contains("thursday")) {
                working += "THURSDAY_";
            }
            if(Arrays.asList(workdays).contains("friday")) {
                working += "FRIDAY_";
            }
            if(Arrays.asList(workdays).contains("saturday")) {
                working += "SATURDAY_";
            }
            if(Arrays.asList(workdays).contains("sunday")) {
                working += "SUNDAY";
            }
            if (working.isBlank()) {
                redirectView.setUrl("http://localhost:8088/err");
            }
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Date start = null;
            Date end = null;
            try {
                start = formatter.parse(start_date);
                end = formatter.parse(end_date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Semester newSemester = new Semester(name, new java.sql.Date(start.getTime()), new java.sql.Date(end.getTime()), rotation_len, working);
            newSemester.setSchool(userFound.getSchool());
            newSemester = semesterRepo.save(newSemester);
            ArrayList<RotationDay> rotation = new ArrayList<>();
            RotationDay rd;
            String rdname;
            for (int i=1; i<=rotation_len; i++) {
                rdname = "Rotation Day " + i;
                rd = new RotationDay(rdname, i-1);
                rd.setSemester(newSemester);
                rotation.add(rotationRepo.save(rd));
            }
            LocalDate startDate = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate endDate = end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            CalendarDay day;
            int rotation_ind = 0;
            for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
                day = new CalendarDay(java.sql.Date.valueOf(date));
                day.setSemester(newSemester);
                if (working.contains(date.getDayOfWeek().toString())) {
                    day.setRotation_day(rotation.get(rotation_ind));
                    rotation_ind = rotation_ind+1 < newSemester.getRotation_length() ? rotation_ind+1 : 0;
                }
                calendarRepo.save(day);
            }
            String timeslot_name;
            for (int i=1; i<=n_timeslots; i++) {
                timeslot_name = "Lesson " + i;
                Timeslot timeslot = new Timeslot(timeslot_name, "00:00", "00:00");
                timeslot.setSemester(newSemester);
                timeslotRepo.save(timeslot);
            }
            redirectView.setUrl("http://localhost:8088/logged-in/" + uid + "/semester/" + newSemester.getId() + "/rotation_config");
        } else {
            String url = "http://localhost:8088/" + checkAuthResult;
            redirectView.setUrl(url);
        }
        redirectView.setHosts();
        return redirectView;
    }

    @PostMapping("logged-in/{uid}/semester/{sid}/rotation_config")
    @ResponseBody
    public RedirectView rename_days(@PathVariable Long uid, @PathVariable Long sid,
                                    @RequestParam String[] dayname,
                                    HttpServletRequest request) {
        RedirectView result = new RedirectView();
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            Optional<Semester> op_semester = semesterRepo.findById(sid);
            if (op_semester.isPresent() &&
                    op_semester.get().getSchool().equals(userRepo.findById(uid).get().getSchool())) {
                Semester semester = op_semester.get();
                ArrayList<RotationDay> rotation = new ArrayList<>(semester.getRotation());
                rotation.sort(RotationDay::compareTo);
                for(int i=0; i<rotation.size(); i++) {
                    if ( i< dayname.length && !dayname[i].isEmpty()) {
                        rotation.get(i).setName(dayname[i]);
                        rotationRepo.save(rotation.get(i));
                    }
                }
                result.setUrl("http://localhost:8088/logged-in/" + uid + "/semester/" + semester.getId() + "/calendar");
            } else {
                result.setUrl("http://localhost:8088/err");
            }
        } else {
            String url = "http://localhost:8088/" + checkAuthResult;
            result.setUrl(url);
        }
        result.setHosts();
        return result;
    }

    @PostMapping("logged-in/{uid}/semester/{sid}/edit_timeslots")
    @ResponseBody
    public RedirectView config_timeslots(@PathVariable Long uid, @PathVariable Long sid,
                                         @RequestParam String[] timeslotname,
                                         @RequestParam String[] time_start,
                                         @RequestParam String[] time_end,
                                         HttpServletRequest request) {
        RedirectView result = new RedirectView();
        String checkAuthResult = checkAuth(uid, request);
        if (timeslotname.length != time_end.length || time_end.length != time_start.length) {
            result.setUrl("http://localhost:8088/err");
            result.setHosts();
            return result;
        } else {
            for(int i=0; i<time_end.length; i++) {
                if (time_end[i].isBlank() || time_start[i].isBlank() ||
                        !dataValidator.timeIsValid(time_start[i]) || !dataValidator.timeIsValid(time_end[i])) {
                    result.setUrl("http://localhost:8088/err");
                    result.setHosts();
                    return result;
                } else {
                    int st_h = Integer.parseInt(time_start[i].substring(0, time_start[i].indexOf(":")));
                    int end_h = Integer.parseInt(time_end[i].substring(0, time_end[i].indexOf(":")));
                    if (st_h > end_h) {
                        result.setUrl("http://localhost:8088/err");
                        result.setHosts();
                        return result;
                    } else if (st_h == end_h) {
                        int st_m = Integer.parseInt(time_start[i].substring(time_start[i].indexOf(":")+1));
                        int end_m = Integer.parseInt(time_end[i].substring(time_end[i].indexOf(":")+1));
                        if (st_m >= end_m) {
                            result.setUrl("http://localhost:8088/err");
                            result.setHosts();
                            return result;
                        }
                    }
                    if (i<time_end.length-1){
                        st_h = Integer.parseInt(time_start[i+1].substring(0, time_start[i+1].indexOf(":")));
                        end_h = Integer.parseInt(time_end[i].substring(0, time_end[i].indexOf(":")));
                        if (st_h < end_h) {
                            result.setUrl("http://localhost:8088/err");
                            result.setHosts();
                            return result;
                        } else if (st_h == end_h) {
                            int st_m = Integer.parseInt(time_start[i+1].substring(time_start[i+1].indexOf(":")+1));
                            int end_m = Integer.parseInt(time_end[i].substring(time_end[i].indexOf(":")+1));
                            if (st_m < end_m) {
                                result.setUrl("http://localhost:8088/err");
                                result.setHosts();
                                return result;
                            }
                        }
                    }
                }
            }
        }
        if (checkAuthResult.equals("**success**")) {
            Optional<Semester> op_semester = semesterRepo.findById(sid);
            if (op_semester.isPresent() &&
                    op_semester.get().getSchool().equals(userRepo.findById(uid).get().getSchool())) {
                Semester semester = op_semester.get();
                ArrayList<Timeslot> timeslots = new ArrayList<>(semester.getTimeslots());
                timeslots.sort(Timeslot::compareTo);
                for(int i=0; i<timeslots.size(); i++) {
                    if (i<timeslotname.length && !timeslotname[i].isEmpty()) {
                        timeslots.get(i).setName(timeslotname[i]);
                    }
                    if (i<time_start.length && dataValidator.timeIsValid(time_start[i])) {
                        timeslots.get(i).setStart_time(time_start[i]);
                    }
                    if (i<time_end.length && dataValidator.timeIsValid(time_end[i])) {
                        timeslots.get(i).setEnd_time(time_end[i]);
                    }
                    timeslotRepo.save(timeslots.get(i));
                }
                result.setUrl("http://localhost:8088/logged-in/" + uid + "/semester/" + semester.getId() + "/calendar");
            } else {
                result.setUrl("http://localhost:8088/err");
            }
        } else {
            String url = "http://localhost:8088/" + checkAuthResult;
            result.setUrl(url);
        }
        result.setHosts();
        return result;
    }

    @PostMapping("/logged-in/{uid}/groups/list")
    @ResponseBody
    public RedirectView add_group(@PathVariable Long uid,
                                  @RequestParam String name, @RequestParam(required = false) String[] students,
                                  HttpServletRequest request) {
        RedirectView redirectView = new RedirectView();
        String checkAuthResult = checkAuth(uid, request);
        if (!name.isBlank() && checkAuthResult.equals("**success**")) {
            School school = userRepo.findById(uid).get().getSchool();
            StudentGroup group = new StudentGroup(name);
            group.setSchool(school);
            group = groupRepo.save(group);
            if (students != null) {
                Optional<Student> student;
                Student studentFound;
                for (String s: students) {
                    student = studentRepo.findById(Long.parseLong(s));
                    if (student.isPresent() && student.get().getSchool().equals(school)) {
                        studentFound = student.get();
                        if (studentFound.getSchool().equals(school)) {
                            studentFound.addGroup(group);
                            studentRepo.save(studentFound);
                        }
                    }
                }
            }
            redirectView.setUrl("http://localhost:8088/logged-in/"+ uid +"/groups/list");
        } else {
            String url = "http://localhost:8088/" + checkAuthResult;
            redirectView.setUrl(url);
        }
        redirectView.setHosts();
        return redirectView;
    }

    @PostMapping("/logged-in/{uid}/group/{gid}/group_view")
    @ResponseBody
    public RedirectView add_to_group(@PathVariable Long uid, @PathVariable Long gid,
                                     @RequestParam String[] students,
                                     HttpServletRequest request) {
        RedirectView redirectView = new RedirectView();
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            School school = userRepo.findById(uid).get().getSchool();
            Optional<StudentGroup> group = groupRepo.findById(gid);
            if (group.isPresent() && group.get().getSchool().equals(school)) {
                StudentGroup groupFound = group.get();
                Optional<Student> student;
                Student studentFound;
                for (String s : students) {
                    student = studentRepo.findById(Long.parseLong(s));
                    if (student.isPresent() && student.get().getSchool().equals(school)) {
                        studentFound = student.get();
                        if (studentFound.getSchool().equals(school)) {
                            studentFound.addGroup(groupFound);
                            studentRepo.save(studentFound);
                        }
                    }
                }
                redirectView.setUrl("http://localhost:8088/logged-in/"+ uid +"/group/" + gid + "/group_view");
            } else {
                redirectView.setUrl("http://localhost:8088/err");
            }
        } else {
            String url = "http://localhost:8088/" + checkAuthResult;
            redirectView.setUrl(url);
        }
        redirectView.setHosts();
        return redirectView;
    }

    @PostMapping("logged-in/{uid}/courses/list")
    @ResponseBody
    public RedirectView add_edit_group(@PathVariable Long uid,
                                       @RequestParam String name,
                                       @RequestParam String oldname,
                                       @RequestParam int credits,
                                       HttpServletRequest request) {
        RedirectView redirectView = new RedirectView();
        String checkAuthResult = checkAuth(uid, request);
        if (!name.isBlank() && checkAuthResult.equals("**success**")) {
            School school = userRepo.findById(uid).get().getSchool();
            Course course;
            if (oldname.isBlank()) {
                course = courseRepo.findByNameAndSchool(name, school);
                if (course != null) {
                    redirectView.setUrl("http://localhost:8088/err");
                    redirectView.setHosts();
                    return redirectView;
                }
                course = new Course(name);
                course.setSchool(school);
            } else {
                course = courseRepo.findByNameAndSchool(oldname, school);
                if (course == null) {
                    redirectView.setUrl("http://localhost:8088/err");
                    redirectView.setHosts();
                    return redirectView;
                }
                course.setName(name);
            }
            if (credits > 0) {
                course.setCredits(credits);
            }
            courseRepo.save(course);
            redirectView.setUrl("http://localhost:8088/logged-in/"+ uid +"/courses/list");
        } else {
            String url = "http://localhost:8088/" + checkAuthResult;
            redirectView.setUrl(url);
        }
        redirectView.setHosts();
        return redirectView;
    }

    @PostMapping("logged-in/{uid}/semester/{sid}/calendar")
    @ResponseBody
    public RedirectView go_to_schedule(@PathVariable Long uid, @PathVariable Long sid,
                                       @RequestParam String for_a,
                                       @RequestParam String name,
                                       HttpServletRequest request) {
        RedirectView redirectView = new RedirectView();
        if(!(for_a.equals("group") || for_a.equals("teacher")) || name.isBlank()) {
            redirectView.setUrl("http://localhost:8088/err");
            redirectView.setHosts();
            return redirectView;
        }
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            School school = userRepo.findById(uid).get().getSchool();
            Optional<Semester> semester = semesterRepo.findById(sid);
            if (semester.isPresent() && semester.get().getSchool().equals(school)) {
                if (for_a.equals("group")) {
                    StudentGroup group = groupRepo.findByNameAndSchool(name, school);
                    if (group != null) {
                        redirectView.setUrl("http://localhost:8088/logged-in/" + uid + "/semester/" + sid +
                                "/schedule_for/group/" + group.getId() + "/edit");
                    }
                } else {
                    Teacher teacher = teacherRepo.findByFullnameAndSchool(name, school);
                    if (teacher != null) {
                        redirectView.setUrl("http://localhost:8088/logged-in/" + uid + "/semester/" + sid +
                                "/schedule_for/teacher/" + teacher.getTeacher_id() + "/edit");
                    }
                }
            } else {
                redirectView.setUrl("http://localhost:8088/err");
            }
        } else {
            String url = "http://localhost:8088/" + checkAuthResult;
            redirectView.setUrl(url);
        }
        redirectView.setHosts();
        return redirectView;
    }

    private String checkAuth(Long uid, HttpServletRequest request) {
        Optional<TeacherbookUser> user = userRepo.findById(uid);
        SessionInformation sessionInfo = sessionRegistry.getSessionInformation(request.getSession().getId());
        if (sessionInfo != null && !sessionInfo.isExpired()) {
            if (dataValidator.emailIsValid(sessionInfo.getPrincipal().toString())) {
                TeacherbookUser userAuth = userRepo.findByUsername(sessionInfo.getPrincipal().toString());
                if (userAuth != null) {
                    if (user.isPresent()) {
                        TeacherbookUser userFound = user.get();
                        if (userAuth.getId().equals(userFound.getId())) {
                            if (userFound.getRoles().contains("*SCHOOL_ADMIN*")) {
                                return "**success**";
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
