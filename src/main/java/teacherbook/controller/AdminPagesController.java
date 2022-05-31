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
import teacherbook.model.gradebook.Assignment;
import teacherbook.model.gradebook.Gradebook;
import teacherbook.model.gradebook.GradebookEntry;
import teacherbook.model.gradebook.Homework;
import teacherbook.model.schedulegrid.*;
import teacherbook.model.users.School;
import teacherbook.model.users.Student;
import teacherbook.model.users.Teacher;
import teacherbook.model.users.TeacherbookUser;
import teacherbook.repositories.*;
import teacherbook.repositories.gradebook.AssignmentRepository;
import teacherbook.repositories.gradebook.GradebookEntryRepository;
import teacherbook.repositories.gradebook.GradebookRepository;
import teacherbook.repositories.gradebook.HomeworkRepository;
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
    private GradebookRepository gradebookRepo;
    @Autowired
    private AssignmentRepository assignmentRepo;
    @Autowired
    private GradebookEntryRepository gradeRepo;
    @Autowired
    private HomeworkRepository homeworkRepo;

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

    @GetMapping("/logged-in/{uid}/members/teacher/{tid}/delete")
    public RedirectView delete_teacher(@PathVariable Long uid, @PathVariable Long tid,
                                     HttpServletRequest request) {
        RedirectView redirectView = new RedirectView();
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            School school = userRepo.findById(uid).get().getSchool();
            Optional<Teacher> teacher = teacherRepo.findById(tid);
            if (teacher.isPresent() && teacher.get().getSchool().equals(school)) {
                Teacher teacherFound = teacher.get();
                ArrayList<ScheduleEntry> schedule = new ArrayList<>(scheduleRepo.findAllByTeacher(teacherFound));
                scheduleRepo.deleteAll(schedule);
                userRepo.delete(teacherFound.getTBuser());
                teacherRepo.delete(teacherFound);
                redirectView.setUrl("http://localhost:8088/logged-in/"+ uid +"/members/teachers");
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

    @GetMapping("logged-in/{uid}/members/students")
    public String students(@PathVariable Long uid, Model model, HttpServletRequest request) {
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            TeacherbookUser userFound = userRepo.findById(uid).get();
            ArrayList<Student> students = new ArrayList<>(userFound.getSchool().getStudents());
            students.sort(Student::compareTo);
            model.addAttribute("students", students);
            return "manage_students";
        } else {
            return checkAuthResult;
        }
    }

    @GetMapping("logged-in/{uid}/members/student/{sid}/student_info")
    public String student_info(@PathVariable Long uid, @PathVariable Long sid,
                               Model model, HttpServletRequest request) {
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            TeacherbookUser userFound = userRepo.findById(uid).get();
            Optional<Student> student = studentRepo.findById(sid);
            if (student.isPresent() && student.get().getSchool().equals(userFound.getSchool())) {
                model.addAttribute("student", student.get());
                return "student_info";
            } else {
                return "err";
            }
        } else {
            return checkAuthResult;
        }
    }

    @GetMapping("logged-in/{uid}/members/student/{sid}/remove_from/{gid}")
    public RedirectView student_remove_from(@PathVariable Long uid,
                                            @PathVariable Long sid, @PathVariable Long gid,
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
                redirectView.setUrl("http://localhost:8088/logged-in/" + uid + "/members/student/" + sid + "/student_info");
            } else {
                redirectView.setUrl("http://localhost:8088/err");
            }
        } else {
            redirectView.setUrl("http://localhost:8088/err");
        }
        redirectView.setHosts();
        return redirectView;
    }

    @GetMapping("logged-in/{uid}/members/student/{sid}/delete")
    public RedirectView student_delete(@PathVariable Long uid, @PathVariable Long sid,
                                       HttpServletRequest request) {
        RedirectView redirectView = new RedirectView();
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            School school = userRepo.findById(uid).get().getSchool();
            Optional<Student> st = studentRepo.findById(sid);
            if (st.isPresent() && st.get().getSchool().equals(school)) {
                Student student = st.get();
                userRepo.delete(student.getUser());
                studentRepo.delete(student);
                redirectView.setUrl("http://localhost:8088/logged-in/"+ uid +"/members/students");
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
                    if(calendar.get(d).getRotationday() != null) {
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
                students.sort(Student::compareTo);
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

    @GetMapping("/logged-in/{uid}/group/{gid}/delete")
    public RedirectView delete_group(@PathVariable Long uid, @PathVariable Long gid,
                                     HttpServletRequest request) {
        RedirectView redirectView = new RedirectView();
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            School school = userRepo.findById(uid).get().getSchool();
            Optional<StudentGroup> group = groupRepo.findById(gid);
            if (group.isPresent() && group.get().getSchool().equals(school)) {
                StudentGroup groupFound = group.get();
                ArrayList<ScheduleEntry> schedule = new ArrayList<>(scheduleRepo.findAllByGroup(groupFound));
                scheduleRepo.deleteAll(schedule);
                ArrayList<Student> students = new ArrayList<>(groupFound.getStudents());
                for (Student s: students) {
                    s.removeGroup(groupFound);
                    studentRepo.save(s);
                }
                groupRepo.delete(groupFound);
                redirectView.setUrl("http://localhost:8088/logged-in/"+ uid +"/groups/list");
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
                    if (calendar.get(d).getRotationday() != null) {
                        workdays_fetched++;
                        date_str += "\n" + calendar.get(d).getRotationday().getName();
                        calendar_table.get(row_ind).set(col_ind, date_str);
                        ArrayList<ScheduleEntry> schedule;
                        if (for_a.equals("group")) {
                            schedule = new ArrayList<>(scheduleRepo.findAllBySemesterAndGroupAndRotationDay(semester,
                                    groupRepo.findById(for_id).get(), calendar.get(d).getRotationday()));
                        } else {
                            schedule = new ArrayList<>(scheduleRepo.findAllBySemesterAndTeacherAndRotationDay(semester,
                                    teacherRepo.findById(for_id).get(), calendar.get(d).getRotationday()));
                        }
                        schedule.sort(ScheduleEntry::compareTo);
                        int schedule_i = 0;
                        for (int i=1; i<=timeslots.size(); i++) {
                            if (schedule_i < schedule.size() &&
                                    schedule.get(schedule_i).getTimeslot().equals(timeslots.get(i-1))) {
                                calendar_table.get(row_ind+i).set(col_ind, schedule.get(schedule_i));
                                schedule_i++;
                            } else {
                                calendar_table.get(row_ind+i).set(col_ind, calendar.get(d).getRotationday());
                            }
                        }
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
                    if (col_ind==7) {
                        for(int i=1; i<8; i++) {
                            if (calendar_table.get(row_ind).get(i) == null) {
                                calendar_table.get(row_ind).set(i, "");
                                for (int j=1; j<=timeslots.size(); j++) {
                                    calendar_table.get(row_ind+j).set(i, "");
                                }
                            }
                        }
                        if (workdays_fetched < rotation_len) {
                            row_ind = row_ind + timeslots.size() + 1;
                            calendar_table.add(new ArrayList<Object>(Arrays.asList(new Object[8])));
                            calendar_table.get(1).set(0, "");
                            for (int i=1; i <= timeslots.size(); i++) {
                                calendar_table.add(new ArrayList<>(Arrays.asList(new Object[8])));
                                calendar_table.get(row_ind+i).set(0, timeslots.get(i-1));
                            }
                        }
                    }
                }
                model.addAttribute("calendar", calendar_table);
                model.addAttribute("for_str", for_a);
                if (for_a.equals("group")) {
                    model.addAttribute("schedule_for", groupRepo.findById(for_id).get());
                    ArrayList<Teacher> teachers = new ArrayList<>(school.getTeachers());
                    ArrayList<String> teachernames = new ArrayList<>();
                    for (Teacher t: teachers) {
                        teachernames.add(t.getFullname());
                    }
                    model.addAttribute("teachernames", teachernames);
                } else {
                    model.addAttribute("schedule_for", teacherRepo.findById(for_id).get());
                    ArrayList<StudentGroup> groups = new ArrayList<>(school.getGroups());
                    ArrayList<String> groupnames = new ArrayList<>();
                    for (StudentGroup g: groups) {
                        groupnames.add(g.getName());
                    }
                    model.addAttribute("groupnames", groupnames);
                }
                ArrayList<Course> courses = new ArrayList<>(school.getCourses());
                ArrayList<String> coursesnames = new ArrayList<>();
                for (Course c: courses) {
                    coursesnames.add(c.getName());
                }
                model.addAttribute("coursesnames", coursesnames);
                return "schedule_edit";
            } else {
                return "err";
            }
        } else {
            return checkAuthResult;
        }
    }

    @GetMapping("logged-in/{uid}/semester/{sid}/schedule_for/{for_a}/{for_id}/delete/{se_id}")
    @ResponseBody
    public RedirectView schedule_entry_delete(@PathVariable Long uid, @PathVariable Long sid,
                                              @PathVariable String for_a, @PathVariable Long for_id,
                                              @PathVariable Long se_id,
                                              HttpServletRequest request) {
        RedirectView redirectView = new RedirectView();
        if (!(for_a.equals("group") || for_a.equals("teacher"))) {
            redirectView.setUrl("http://localhost:8088/logged-in/err");
            redirectView.setHosts();
            return redirectView;
        } else if (for_a.equals("group") && !groupRepo.findById(for_id).isPresent()) {
            redirectView.setUrl("http://localhost:8088/logged-in/err");
            redirectView.setHosts();
            return redirectView;
        } else if (for_a.equals("teacher") && !teacherRepo.findById(for_id).isPresent()) {
            redirectView.setUrl("http://localhost:8088/logged-in/err");
            redirectView.setHosts();
            return redirectView;
        }
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            School school = userRepo.findById(uid).get().getSchool();
            Optional<Semester> op_semester = semesterRepo.findById(sid);
            if (op_semester.isPresent() &&
                    op_semester.get().getSchool().equals(school)) {
                Semester semester = op_semester.get();
                if ((for_a.equals("group") && !groupRepo.findById(for_id).get().getSchool().equals(school)) ||
                        (for_a.equals("teacher") && !teacherRepo.findById(for_id).get().getSchool().equals(school))) {
                    redirectView.setUrl("http://localhost:8088/logged-in/err");
                    redirectView.setHosts();
                    return redirectView;
                }
                Optional<ScheduleEntry> se = scheduleRepo.findById(se_id);
                if (se.isPresent() && se.get().getSemester().equals(semester)) {
                    ScheduleEntry scheduleEntry = se.get();
                    Gradebook gradebook = gradebookRepo.findBySemesterAndGroupAndCourse(semester, scheduleEntry.getGroup(), scheduleEntry.getCourse());
                    if (gradebook != null) {
                        //delete columns and information in them
                        ArrayList<Assignment> assignments = new ArrayList<>(gradebook.getAssignments());
                        for (Assignment a: assignments) {
                            if (a.getDay().getRotationday().equals(scheduleEntry.getRotationDay()) &&
                                    a.getTimeslot().equals(scheduleEntry.getTimeslot())) {
                                if (a.getHomework() != null) {
                                    if (a.getHomework().isHas_column()) {
                                        Homework hw = a.getHomework();
                                        Assignment c = hw.getColumn();
                                        gradeRepo.deleteAll(c.getEntries());
                                        hw.setColumn(null);
                                        homeworkRepo.save(hw);
                                        assignmentRepo.delete(c);
                                    }
                                    homeworkRepo.delete(a.getHomework());
                                }
                                gradeRepo.deleteAll(a.getEntries());
                                assignmentRepo.delete(a);
                            }
                        }
                        //check if the teacher still has access
                        Teacher teacher = scheduleEntry.getTeacher();
                        boolean has_access = false;
                        ArrayList<ScheduleEntry> teacherSchedule = new ArrayList<>(scheduleRepo.findAllBySemesterAndTeacher(semester, teacher));
                        for (ScheduleEntry seT: teacherSchedule) {
                            if (seT.getGroup().equals(scheduleEntry.getGroup()) &&
                                    seT.getCourse().equals(scheduleEntry.getCourse())) {
                                has_access = true;
                                break;
                            }
                        }
                        if (!has_access) {
                            teacher.removeGradebook(gradebook);
                            teacherRepo.save(teacher);
                        }
                        ArrayList<ScheduleEntry> scheduleEntries = new ArrayList<>(scheduleRepo.findAllBySemesterAndGroupAndCourse(semester, scheduleEntry.getGroup(), scheduleEntry.getCourse()));
                        if (scheduleEntries.isEmpty()) {
                            ArrayList<Teacher> teachers = new ArrayList<>(gradebook.getTeachers());
                            for (Teacher t: teachers) {
                                if(t.getGradebooks().contains(gradebook)) {
                                    t.removeGradebook(gradebook);
                                    teacherRepo.save(t);
                                }
                            }
                            gradebookRepo.delete(gradebook);
                        }
                    }
                    scheduleRepo.delete(scheduleEntry);
                    redirectView.setUrl("http://localhost:8088/logged-in/" + uid + "/semester/" + sid + "/schedule_for/" + for_a + "/" + for_id + "/edit");
                } else {
                    redirectView.setUrl("http://localhost:8088/logged-in/err");
                }
            } else {
                redirectView.setUrl("http://localhost:8088/logged-in/err");
            }
        } else {
            String url = "http://localhost:8088/" + checkAuthResult;
            redirectView.setUrl(url);
        }
        redirectView.setHosts();
        return redirectView;
    }

    @GetMapping("logged-in/{uid}/semester/{sid}/schedule_for/{for_a}/{for_id}/generate_columns")
    @ResponseBody
    public String generate_columns(@PathVariable Long uid, @PathVariable Long sid,
                                   @PathVariable String for_a, @PathVariable Long for_id,
                                   HttpServletRequest request) {
        if (!(for_a.equals("group") || for_a.equals("teacher"))) {
            return "Error";
        } else if (for_a.equals("group") && !groupRepo.findById(for_id).isPresent()) {
            return "Error";
        } else if (for_a.equals("teacher") && !teacherRepo.findById(for_id).isPresent()) {
            return "Error";
        }
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            Optional<Semester> op_semester = semesterRepo.findById(sid);
            if (op_semester.isPresent() &&
                    op_semester.get().getSchool().equals(userRepo.findById(uid).get().getSchool())) {
                School school = userRepo.findById(uid).get().getSchool();
                if ((for_a.equals("group") && !groupRepo.findById(for_id).get().getSchool().equals(school)) ||
                        (for_a.equals("teacher") && !teacherRepo.findById(for_id).get().getSchool().equals(school))) {
                    return "Error";
                }
                Semester semester = op_semester.get();
                ArrayList<ScheduleEntry> schedule;
                StudentGroup group = null;
                Teacher teacher = null;
                Course course = null;
                if (for_a.equals("group")) {
                    group = groupRepo.findById(for_id).get();
                    schedule = new ArrayList<>(scheduleRepo.findAllBySemesterAndGroup(semester, group));
                } else {
                    teacher = teacherRepo.findById(for_id).get();
                    schedule = new ArrayList<>(scheduleRepo.findAllBySemesterAndTeacher(semester, teacher));
                }
                HashMap<StudentGroup, HashMap<Course, Gradebook>> teacher_gradebooks = new HashMap<>();
                HashMap<Course, Gradebook> group_gradebooks = new HashMap<>();
                Gradebook gradebook;
                for (ScheduleEntry se: schedule) {
                    if (for_a.equals("group")) {
                        teacher = se.getTeacher();
                    } else {
                        group = se.getGroup();
                        if (teacher_gradebooks.containsKey(group)) {
                            group_gradebooks = teacher_gradebooks.get(group);
                        } else {
                            teacher_gradebooks.put(group, new HashMap<>());
                            group_gradebooks = teacher_gradebooks.get(group);
                        }
                    }
                    course = se.getCourse();
                    if (group_gradebooks.containsKey(course)) {
                        gradebook = group_gradebooks.get(course);
                    } else {
                        gradebook = gradebookRepo.findBySemesterAndGroupAndCourse(se.getSemester(), group, course);
                        if (gradebook == null) {
                            String n = group.getName() + " " + course.getName();
                            gradebook = new Gradebook(n, se.getSemester(), group, course);
                            group_gradebooks.put(course, gradebook);
                            gradebook = gradebookRepo.save(gradebook);
                        }
                    }
                    if (!teacher.getGradebooks().contains(gradebook)) {
                        teacher.addGradebook(gradebook);
                        teacherRepo.save(teacher);
                    }
                    ArrayList<CalendarDay> days = new ArrayList<>(calendarRepo.findAllBySemesterAndRotationday(se.getSemester(), se.getRotationDay()));
                    Timeslot t = se.getTimeslot();
                    Assignment assignment;
                    GradebookEntry entry;
                    for (CalendarDay day: days) {
                        assignment = assignmentRepo.findByGradebookAndAndDayAndTimeslot(gradebook, day, t);
                        if (assignment == null) {
                            LocalDate d = day.getDate().toLocalDate();
                            String name = "Class " + d.getDayOfMonth() + "/" + d.getMonth().getValue() + "\n" + t.getName();
                            assignment = new Assignment(name, gradebook, false, day, t);
                            assignment = assignmentRepo.save(assignment);
                        }
                        for (Student s: group.getStudents()) {
                            entry = gradeRepo.findByAssignmentAndStudent(assignment, s);
                            if (entry == null) {
                                entry = new GradebookEntry(gradebook, s, assignment);
                            }
                            if (!entry.isAttendance()) {
                                entry.setAttendance(true);
                                entry.setAttendanceValue(false);
                            }
                            gradeRepo.save(entry);
                        }
                    }
                }
                if (for_a.equals("group")) {
                    teacher_gradebooks.put(group, group_gradebooks);
                }
                for (StudentGroup sg: teacher_gradebooks.keySet()) {
                    for (Course c: teacher_gradebooks.get(sg).keySet()) {
                        gradebookRepo.save(teacher_gradebooks.get(sg).get(c));
                    }
                }
                return "Success!";
            } else {
                return "Error";
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

    @PostMapping("logged-in/{uid}/members/student/{sid}/edit_info")
    @ResponseBody
    public RedirectView student_edit(@PathVariable Long uid, @PathVariable Long sid,
                                     @RequestParam String firstname, @RequestParam String lastname,
                                     HttpServletRequest request) {
        RedirectView redirectView = new RedirectView();
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            School school = userRepo.findById(uid).get().getSchool();
            Optional<Student> st = studentRepo.findById(sid);
            if (st.isPresent() && st.get().getSchool().equals(school)) {
                Student student = st.get();
                student.setFirst_name(firstname);
                student.setLast_name(lastname);
                studentRepo.save(student);
                redirectView.setUrl("http://localhost:8088/logged-in/"+ uid +"/members/student/" + sid + "/student_info");
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

    @PostMapping("logged-in/{uid}/members/student/{sid}/edit_login")
    @ResponseBody
    public RedirectView student_edit_login(@PathVariable Long uid, @PathVariable Long sid,
                                     @RequestParam String username, @RequestParam(required = false) String pswd,
                                     HttpServletRequest request) {
        RedirectView redirectView = new RedirectView();
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            School school = userRepo.findById(uid).get().getSchool();
            Optional<Student> st = studentRepo.findById(sid);
            if (st.isPresent() && st.get().getSchool().equals(school)) {
                TeacherbookUser student = st.get().getUser();
                if (dataValidator.loginIsValid(username)) {
                    String uname = username + "_" + school.getSchool_id();
                    if (!student.getUsername().equals(uname)) {
                        student.setUsername(uname);
                    }
                    if (pswd != null) {
                        if (dataValidator.pswdIsValid(pswd)) {
                            student.setPasshash(pswd);
                            userService.saveUser(student);
                        } else {
                            redirectView.setUrl("http://localhost:8088/err");
                            redirectView.setHosts();
                            return redirectView;
                        }
                    } else {
                        userRepo.save(student);
                    }
                    redirectView.setUrl("http://localhost:8088/logged-in/"+ uid +"/members/student/" + sid + "/student_info");
                } else {
                    redirectView.setUrl("http://localhost:8088/err");
                }
                redirectView.setUrl("http://localhost:8088/logged-in/"+ uid +"/members/student/" + sid + "/student_info");
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
                    day.setRotationday(rotation.get(rotation_ind));
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
            if (groupRepo.findByNameAndSchool(name, school) != null) {
                String url = "http://localhost:8088/err";
                redirectView.setUrl(url);
                redirectView.setHosts();
                return redirectView;
            }
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
                        studentFound.addGroup(group);
                        studentRepo.save(studentFound);
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
                java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
                ArrayList<Semester> semesters = new ArrayList<>(semesterRepo.findAllBySchoolAndEndDateAfter(school, today));
                Optional<Student> student;
                Student studentFound;
                for (String s : students) {
                    student = studentRepo.findById(Long.parseLong(s));
                    if (student.isPresent() && student.get().getSchool().equals(school)) {
                        studentFound = student.get();
                        ArrayList<StudentGroup> groups = new ArrayList<>(studentFound.getGroups());
                        for (StudentGroup g: groups) {
                            for (Semester sem: semesters) {
                                ArrayList<ScheduleEntry> scheduleGroupFound = new ArrayList<>(scheduleRepo.findAllBySemesterAndGroup(sem, groupFound));
                                ArrayList<ScheduleEntry> scheduleG = new ArrayList<>(scheduleRepo.findAllBySemesterAndGroup(sem, g));
                                for (ScheduleEntry seG: scheduleG) {
                                    for (ScheduleEntry seGF: scheduleGroupFound) {
                                        if (seG.getRotationDay().equals(seGF.getRotationDay()) && seG.getTimeslot().equals(seGF.getTimeslot())) {
                                            redirectView.setUrl("http://localhost:8088/err");
                                            redirectView.setHosts();
                                            return redirectView;
                                        }
                                    }
                                }
                            }
                        }
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

    @PostMapping("/logged-in/{uid}/group/{gid}/rename")
    @ResponseBody
    public RedirectView rename_group(@PathVariable Long uid, @PathVariable Long gid,
                                     @RequestParam String newname,
                                     HttpServletRequest request) {
        RedirectView redirectView = new RedirectView();
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            School school = userRepo.findById(uid).get().getSchool();
            Optional<StudentGroup> group = groupRepo.findById(gid);
            if (group.isPresent() && group.get().getSchool().equals(school)) {
                StudentGroup groupFound = group.get();
                groupFound.setName(newname);
                groupRepo.save(groupFound);
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
    public RedirectView add_edit_course(@PathVariable Long uid,
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

    @PostMapping("logged-in/{uid}/semester/{sid}/schedule_for/{for_a}/{for_id}/edit")
    @ResponseBody
    public RedirectView add_edit_schedule(@PathVariable Long uid, @PathVariable Long sid,
                                          @PathVariable String for_a, @PathVariable Long for_id,
                                          @RequestParam long rd_id, @RequestParam long ts_id,
                                          @RequestParam String se_id,
                                          @RequestParam String course,
                                          @RequestParam String teacher,
                                          @RequestParam String group,
                                          HttpServletRequest request) {
        RedirectView redirectView = new RedirectView();
        if (!(for_a.equals("group") || for_a.equals("teacher"))) {
            redirectView.setUrl("http://localhost:8088/err");
            redirectView.setHosts();
            return redirectView;
        } else if (for_a.equals("group") && !groupRepo.findById(for_id).isPresent()) {
            redirectView.setUrl("http://localhost:8088/err");
            redirectView.setHosts();
            return redirectView;
        } else if (for_a.equals("teacher") && !teacherRepo.findById(for_id).isPresent()) {
            redirectView.setUrl("http://localhost:8088/err");
            redirectView.setHosts();
            return redirectView;
        }
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            School school = userRepo.findById(uid).get().getSchool();
            Optional<Semester> semester = semesterRepo.findById(sid);
            if ((for_a.equals("group") && !groupRepo.findById(for_id).get().getSchool().equals(school)) ||
                    (for_a.equals("teacher") && !teacherRepo.findById(for_id).get().getSchool().equals(school))) {
                redirectView.setUrl("http://localhost:8088/err");
                redirectView.setHosts();
                return redirectView;
            }
            if (semester.isPresent() && semester.get().getSchool().equals(school)) {
                Semester semesterFound = semester.get();
                if (!rotationRepo.findById(rd_id).isPresent() ||
                        !rotationRepo.findById(rd_id).get().getSemester().equals(semesterFound) ||
                        !timeslotRepo.findById(ts_id).isPresent() ||
                        !timeslotRepo.findById(ts_id).get().getSemester().equals(semesterFound) ||
                        course.isBlank() || teacher.isBlank() || group.isBlank() ||
                        courseRepo.findByNameAndSchool(course.strip(), school) == null ||
                        teacherRepo.findByFullnameAndSchool(teacher.strip(), school) == null ||
                        groupRepo.findByNameAndSchool(group.strip(), school) == null) {
                    redirectView.setUrl("http://localhost:8088/err");
                } else if (se_id.isBlank()){
                    RotationDay rd = rotationRepo.findById(rd_id).get();
                    Timeslot ts = timeslotRepo.findById(ts_id).get();
                    Teacher dbTeacher = teacherRepo.findByFullnameAndSchool(teacher.strip(), school);
                    if (scheduleRepo.findBySemesterAndTeacherAndRotationDayAndTimeslot(semesterFound, dbTeacher, rd, ts) != null) {
                        redirectView.setUrl("http://localhost:8088/err");
                        redirectView.setHosts();
                        return redirectView;
                    }
                    StudentGroup dbGroup = groupRepo.findByNameAndSchool(group.strip(), school);
                    for (Student s: dbGroup.getStudents()) {
                        if (s.getGroups().size() > 1) {
                            ArrayList<StudentGroup> groups = new ArrayList<>(s.getGroups());
                            groups.remove(dbGroup);
                            for (StudentGroup g: groups) {
                                if (scheduleRepo.findBySemesterAndGroupAndRotationDayAndTimeslot(semesterFound, g, rd, ts) != null) {
                                    redirectView.setUrl("http://localhost:8088/err");
                                    redirectView.setHosts();
                                    return redirectView;
                                }
                            }
                        }
                    }
                    ScheduleEntry newEntry = new ScheduleEntry();
                    newEntry.setSemester(semesterFound);
                    newEntry.setRotationDay(rd);
                    newEntry.setTimeslot(ts);
                    newEntry.setCourse(courseRepo.findByNameAndSchool(course.strip(), school));
                    newEntry.setTeacher(dbTeacher);
                    newEntry.setGroup(dbGroup);
                    scheduleRepo.save(newEntry);
                    redirectView.setUrl("http://localhost:8088/logged-in/" + uid + "/semester/" + sid + "/schedule_for/" + for_a + "/" + for_id + "/edit");
                } else {
                    try {
                        ScheduleEntry se = scheduleRepo.findById(Long.parseLong(se_id)).get();
                        RotationDay rd = rotationRepo.findById(rd_id).get();
                        Timeslot ts = timeslotRepo.findById(ts_id).get();
                        Teacher dbTeacher = teacherRepo.findByFullnameAndSchool(teacher.strip(), school);
                        StudentGroup dbGroup = groupRepo.findByNameAndSchool(group.strip(), school);
                        if (!se.getSemester().equals(semesterFound) ||
                                !se.getRotationDay().equals(rd) || !se.getTimeslot().equals(ts) ||
                                (for_a.equals("group") && !se.getGroup().equals(dbGroup)) ||
                                (for_a.equals("teacher") && !se.getTeacher().equals(dbTeacher))) {
                            redirectView.setUrl("http://localhost:8088/err");
                            redirectView.setHosts();
                            return redirectView;
                        }
                        if (!se.equals(scheduleRepo.findBySemesterAndTeacherAndRotationDayAndTimeslot(semesterFound, dbTeacher, rd, ts))) {
                            redirectView.setUrl("http://localhost:8088/err");
                            redirectView.setHosts();
                            return redirectView;
                        }
                        for (Student s: dbGroup.getStudents()) {
                            if (s.getGroups().size() > 1) {
                                ArrayList<StudentGroup> groups = new ArrayList<>(s.getGroups());
                                groups.remove(dbGroup);
                                for (StudentGroup g: groups) {
                                    if (!se.equals(scheduleRepo.findBySemesterAndGroupAndRotationDayAndTimeslot(semesterFound, g, rd, ts))) {
                                        redirectView.setUrl("http://localhost:8088/err");
                                        redirectView.setHosts();
                                        return redirectView;
                                    }
                                }
                            }
                        }
                        se.setGroup(dbGroup);
                        se.setTeacher(dbTeacher);
                        se.setCourse(courseRepo.findByNameAndSchool(course.strip(), school));
                        scheduleRepo.save(se);
                        redirectView.setUrl("http://localhost:8088/logged-in/" + uid + "/semester/" + sid + "/schedule_for/" + for_a + "/" + for_id + "/edit");

                    } catch (Exception e) {
                        redirectView.setUrl("http://localhost:8088/err");
                        redirectView.setHosts();
                        return redirectView;
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
