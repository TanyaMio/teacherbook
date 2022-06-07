package teacherbook.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import teacherbook.model.gradebook.Assignment;
import teacherbook.model.gradebook.Gradebook;
import teacherbook.model.gradebook.GradebookEntry;
import teacherbook.model.gradebook.Homework;
import teacherbook.model.schedulegrid.CalendarDay;
import teacherbook.model.schedulegrid.Semester;
import teacherbook.model.users.Student;
import teacherbook.model.users.Teacher;
import teacherbook.model.users.TeacherbookUser;
import teacherbook.repositories.CalendarDayRepository;
import teacherbook.repositories.SemesterRepository;
import teacherbook.repositories.gradebook.AssignmentRepository;
import teacherbook.repositories.gradebook.GradebookEntryRepository;
import teacherbook.repositories.gradebook.GradebookRepository;
import teacherbook.repositories.gradebook.HomeworkRepository;
import teacherbook.repositories.users.TeacherRepository;
import teacherbook.repositories.users.UserRepository;
import teacherbook.security.DataValidator;

import javax.servlet.http.HttpServletRequest;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@Controller
public class TeacherPageController {

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private TeacherRepository teacherRepo;

    @Autowired
    private SemesterRepository semesterRepo;

    @Autowired
    private GradebookRepository gradebookRepo;
    @Autowired
    private GradebookEntryRepository gradeRepo;
    @Autowired
    private AssignmentRepository assignmentRepo;
    @Autowired
    private HomeworkRepository homeworkRepo;

    @Autowired
    private CalendarDayRepository calendarRepo;

    @Autowired
    private SessionRegistry sessionRegistry;

    private DataValidator dataValidator = new DataValidator();

    @GetMapping("logged-in/{uid}/teacher_home")
    public String teacher_home(@PathVariable Long uid,
                               Model model, HttpServletRequest request) {
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            Teacher teacher = userRepo.findById(uid).get().getTeacher();
            Collection<Gradebook> allgradebooks = teacher.getGradebooks();
            ArrayList<Gradebook> currgradebooks = new ArrayList<>();
            Date today = new Date(System.currentTimeMillis());
            ArrayList<Semester> semesters = new ArrayList<>(semesterRepo.findAllBySchoolAndStartDateBeforeAndEndDateAfter(teacher.getSchool(), today, today));
            for (Gradebook g: allgradebooks) {
                if (semesters.contains(g.getSemester())) {
                    currgradebooks.add(g);
                }
            }
            model.addAttribute("teacher", teacher);
            model.addAttribute("gradebooks", currgradebooks);
            return "teacher_home";
        } else {
            return checkAuthResult;
        }
    }

    @GetMapping("logged-in/{uid}/teacher_info")
    public String teacher_info(@PathVariable Long uid,
                               Model model, HttpServletRequest request) {
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            model.addAttribute("teacher", userRepo.findById(uid).get().getTeacher());
            return "teacher_info";
        } else {
            return checkAuthResult;
        }
    }

    @GetMapping("logged-in/{uid}/gradebook/{gb_id}/edit")
    public String gradebook_edit(@PathVariable Long uid, @PathVariable Long gb_id,
                                 Model model, HttpServletRequest request) {
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            Teacher teacher = userRepo.findById(uid).get().getTeacher();
            Date today = new Date(System.currentTimeMillis());
            ArrayList<Semester> semesters = new ArrayList<>(semesterRepo.findAllBySchoolAndStartDateBeforeAndEndDateAfter(teacher.getSchool(), today, today));
            Optional<Gradebook> g = gradebookRepo.findById(gb_id);
            if (g.isPresent() && teacher.getGradebooks().contains(g.get()) &&
                    semesters.contains(g.get().getSemester())) {
                Gradebook gradebook = g.get();
                ArrayList<Student> students = new ArrayList<>(gradebook.getGroup().getStudents());
                students.sort(Student::compareTo);
                ArrayList<ArrayList<Object>> gradebook_data = new ArrayList<>();
                gradebook_data.add(new ArrayList<>());
                gradebook_data.get(0).add("Student Name");
                for (int i=0; i<students.size(); i++) {
                    gradebook_data.add(new ArrayList<>());
                    gradebook_data.get(i+1).add(students.get(i).getFullname());
                }
                ArrayList<Assignment> assignments = new ArrayList<>(gradebook.getAssignments());
                assignments.sort(Assignment::compareTo);
                ArrayList<GradebookEntry> entries;
                for(Assignment a: assignments) {
                    gradebook_data.get(0).add(a);
                    entries = new ArrayList<>(a.getEntries());
                    entries.sort(GradebookEntry::compareTo);
                    for (int i=0; i<entries.size(); i++) {
                        gradebook_data.get(i+1).add(entries.get(i));
                    }
                }
                model.addAttribute("gradebook", gradebook);
                model.addAttribute("gradebook_data", gradebook_data);
                return "gradebook_edit";
            } else {
                return "err";
            }
        } else {
            return checkAuthResult;
        }
    }

    @GetMapping("logged-in/{uid}/gradebook/{gb_id}/edit/attendance/{ge_id}/set_to/{val}")
    @ResponseBody
    public String attendanceChange(@PathVariable Long uid, @PathVariable Long gb_id,
                                    @PathVariable Long ge_id, @PathVariable boolean val,
                                    HttpServletRequest request) {
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            Teacher teacher = userRepo.findById(uid).get().getTeacher();
            Optional<Gradebook> g = gradebookRepo.findById(gb_id);
            Date today = new Date(System.currentTimeMillis());
            ArrayList<Semester> semesters = new ArrayList<>(semesterRepo.findAllBySchoolAndStartDateBeforeAndEndDateAfter(teacher.getSchool(), today, today));
            if (g.isPresent() && teacher.getGradebooks().contains(g.get()) &&
                    semesters.contains(g.get().getSemester())) {
                Gradebook gradebook = g.get();
                Optional<GradebookEntry> ge = gradeRepo.findById(ge_id);
                if (ge.isPresent() && ge.get().getGradebook().equals(gradebook)) {
                    GradebookEntry entry = ge.get();
                    entry.setAttendanceValue(val);
                    gradeRepo.save(entry);
                    return "Success!!!";
                } else {
                    return "err";
                }
            } else {
                return "err";
            }
        } else {
            return checkAuthResult;
        }
    }

    @GetMapping("logged-in/{uid}/gradebook/{gb_id}/delete_column/{cid}")
    @ResponseBody
    public RedirectView deleteColumn(@PathVariable Long uid, @PathVariable Long gb_id,
                                     @PathVariable Long cid,
                                     HttpServletRequest request) {
        RedirectView redirectView = new RedirectView();
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            Teacher teacher = userRepo.findById(uid).get().getTeacher();
            Optional<Gradebook> g = gradebookRepo.findById(gb_id);
            Date today = new Date(System.currentTimeMillis());
            ArrayList<Semester> semesters = new ArrayList<>(semesterRepo.findAllBySchoolAndStartDateBeforeAndEndDateAfter(teacher.getSchool(), today, today));
            if (g.isPresent() && teacher.getGradebooks().contains(g.get()) &&
                    semesters.contains(g.get().getSemester())) {
                Gradebook gradebook = g.get();
                Optional<Assignment> a = assignmentRepo.findById(cid);
                if (a.isPresent() && a.get().getGradebook().equals(gradebook)) {
                    Assignment assignment = a.get();
                    if (assignment.getHomework() != null) {
                        if (assignment.getHomework().isHas_column()) {
                            Homework hw = assignment.getHomework();
                            Assignment c = hw.getColumn();
                            gradeRepo.deleteAll(c.getEntries());
                            hw.setColumn(null);
                            homeworkRepo.save(hw);
                            assignmentRepo.delete(c);
                        }
                        homeworkRepo.delete(assignment.getHomework());
                    }
                    gradeRepo.deleteAll(assignment.getEntries());
                    assignmentRepo.delete(assignment);
                    redirectView.setUrl("http://localhost:8088/logged-in/" + uid + "/gradebook/" + gb_id + "/edit");
                }else {
                    redirectView.setUrl("http://localhost:8088/err");
                }
            } else {
                redirectView.setUrl("http://localhost:8088/err");
            }
        } else {
            redirectView.setUrl("http://localhost:8088/err");
        }
        redirectView.setHosts();
        return redirectView;
    }

    @GetMapping("logged-in/{uid}/gradebook/{gb_id}/delete_hw/{hw_id}")
    @ResponseBody
    public RedirectView deleteHW(@PathVariable Long uid, @PathVariable Long gb_id,
                                     @PathVariable Long hw_id,
                                     HttpServletRequest request) {
        RedirectView redirectView = new RedirectView();
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            Teacher teacher = userRepo.findById(uid).get().getTeacher();
            Optional<Gradebook> g = gradebookRepo.findById(gb_id);
            Date today = new Date(System.currentTimeMillis());
            ArrayList<Semester> semesters = new ArrayList<>(semesterRepo.findAllBySchoolAndStartDateBeforeAndEndDateAfter(teacher.getSchool(), today, today));
            if (g.isPresent() && teacher.getGradebooks().contains(g.get()) &&
                    semesters.contains(g.get().getSemester())) {
                Gradebook gradebook = g.get();
                Optional<Homework> homework = homeworkRepo.findById(hw_id);
                if (homework.isPresent() && homework.get().getGradebook().equals(gradebook)) {
                    Homework hw = homework.get();
                    if (hw.isHas_column()) {
                        Assignment c = hw.getColumn();
                        gradeRepo.deleteAll(c.getEntries());
                        hw.setColumn(null);
                        homeworkRepo.save(hw);
                        assignmentRepo.delete(c);
                    }
                    homeworkRepo.delete(hw);
                    redirectView.setUrl("http://localhost:8088/logged-in/" + uid + "/gradebook/" + gb_id + "/edit");
                }else {
                    redirectView.setUrl("http://localhost:8088/err");
                }
            } else {
                redirectView.setUrl("http://localhost:8088/err");
            }
        } else {
            redirectView.setUrl("http://localhost:8088/err");
        }
        redirectView.setHosts();
        return redirectView;
    }

    @PostMapping("logged-in/{uid}/edit_info")
    public RedirectView infoChange(@PathVariable Long uid,
                                   @RequestParam(required = false) String honorific,
                                   @RequestParam(required = false) String lastname,
                                   @RequestParam(required = false) String firstname,
                                   @RequestParam(required = false) String email,
                                   HttpServletRequest request) {
        RedirectView redirectView = new RedirectView();
        String[] h = {"", "Mr.", "Ms.", "Mrs.", "Mx."};
        ArrayList<String> honorifics = new ArrayList<>(Arrays.asList(h));
        if ((honorific != null && !honorifics.contains(honorific)) ||
                (email != null && !dataValidator.emailIsValid(email))) {
            redirectView.setUrl("http://localhost:8088/err");
            redirectView.setHosts();
            return redirectView;
        }
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            Teacher teacher = userRepo.findById(uid).get().getTeacher();
            if (honorific != null) {
                teacher.setHonorific(honorific);
            }
            if (firstname != null) {
                teacher.setFirst_name(firstname);
            }
            if (lastname != null) {
                teacher.setLast_name(lastname);
            }
            if (email != null) {
                TeacherbookUser user = teacher.getTBuser();
                user.setUsername(email);
                userRepo.save(user);
            }
            teacherRepo.save(teacher);
            redirectView.setUrl("http://localhost:8088/logged-in/" + uid + "/teacher_info");
        } else {
            redirectView.setUrl("http://localhost:8088/err");
        }
        redirectView.setHosts();
        return redirectView;
    }

    @PostMapping("logged-in/{uid}/gradebook/{gb_id}/edit")
    @ResponseBody
    public RedirectView gradeChange(@PathVariable Long uid, @PathVariable Long gb_id,
                                    @RequestParam Long entry,
                                    @RequestParam int grade,
                                    @RequestParam(required = false) String note,
                                    HttpServletRequest request) {
        RedirectView redirectView = new RedirectView();
        if (grade < 0 || grade > 100) {
            redirectView.setUrl("http://localhost:8088/err");
            redirectView.setHosts();
            return redirectView;
        }
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            Teacher teacher = userRepo.findById(uid).get().getTeacher();
            Optional<Gradebook> g = gradebookRepo.findById(gb_id);
            Date today = new Date(System.currentTimeMillis());
            ArrayList<Semester> semesters = new ArrayList<>(semesterRepo.findAllBySchoolAndStartDateBeforeAndEndDateAfter(teacher.getSchool(), today, today));
            if (g.isPresent() && teacher.getGradebooks().contains(g.get()) &&
                    semesters.contains(g.get().getSemester())) {
                Gradebook gradebook = g.get();
                Optional<GradebookEntry> ge = gradeRepo.findById(entry);
                if (ge.isPresent() && ge.get().getGradebook().equals(gradebook)) {
                    GradebookEntry dbEntry = ge.get();
                    dbEntry.setGrade(grade);
                    if (note != null && !note.isBlank()) {
                        dbEntry.setNote(note);
                    }
                    gradeRepo.save(dbEntry);
                    redirectView.setUrl("http://localhost:8088/logged-in/" + uid + "/gradebook/" + gb_id + "/edit");
                } else {
                    redirectView.setUrl("http://localhost:8088/err");
                }
            } else {
                redirectView.setUrl("http://localhost:8088/err");
            }
        } else {
            redirectView.setUrl("http://localhost:8088/err");
        }
        redirectView.setHosts();
        return redirectView;
    }


    @PostMapping("logged-in/{uid}/gradebook/{gb_id}/homework")
    @ResponseBody
    public RedirectView hwChange(@PathVariable Long uid, @PathVariable Long gb_id,
                                    @RequestParam Long a_id,
                                    @RequestParam(required = false) Long hw_id,
                                    @RequestParam String hw_content,
                                    @RequestParam String duedate,
                                    @RequestParam(required = false) Boolean hascolumn,
                                    @RequestParam(required = false) String cname,
                                    HttpServletRequest request) {
        RedirectView redirectView = new RedirectView();
        if (hw_content.isBlank() || duedate.isBlank()) {
            redirectView.setUrl("http://localhost:8088/err");
            redirectView.setHosts();
            return redirectView;
        }
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            Teacher teacher = userRepo.findById(uid).get().getTeacher();
            Optional<Gradebook> g = gradebookRepo.findById(gb_id);
            Date today = new Date(System.currentTimeMillis());
            ArrayList<Semester> semesters = new ArrayList<>(semesterRepo.findAllBySchoolAndStartDateBeforeAndEndDateAfter(teacher.getSchool(), today, today));
            if (g.isPresent() && teacher.getGradebooks().contains(g.get()) &&
                    semesters.contains(g.get().getSemester())) {
                Gradebook gradebook = g.get();
                Optional<Assignment> a = assignmentRepo.findById(a_id);
                if (a.isPresent() && a.get().getGradebook().equals(gradebook)) {
                    Assignment assignment = a.get();
                    if ((assignment.getHomework() == null && hw_id == null) ||
                            assignment.getHomework().getId().equals(hw_id)) {
                        if (hascolumn == null) {
                            hascolumn = false;
                        }
                        Homework homework = assignment.getHomework();
                        if (homework == null) {
                            homework = new Homework(hw_content, Date.valueOf(duedate),
                                    gradebook, gradebook.getGroup(),
                                    assignment, hascolumn);
                        } else {
                            if (!hw_content.equals(homework.getContent())) {
                                homework.setContent(hw_content);
                            }
                            if (!Date.valueOf(duedate).equals(homework.getDuedate())) {
                                homework.setDuedate(Date.valueOf(duedate));
                            }
                            if (hascolumn && !homework.isHas_column()) {
                                homework.setHas_column(hascolumn);
                            }
                        }
                        if (homework.isHas_column()) {
                            Assignment column;
                            if (homework.getColumn() == null) {
                                String name;
                                if (cname != null && !cname.isBlank()) {
                                    name = cname;
                                } else {
                                    name = assignment.getName() + "\n" + "Homework";
                                }
                                CalendarDay day = calendarRepo.findBySemesterAndDate(gradebook.getSemester(), Date.valueOf(duedate));
                                if (day == null) {
                                    day = new CalendarDay(Date.valueOf(duedate));
                                    day = calendarRepo.save(day);
                                }
                                column = new Assignment(name, gradebook, true, day, null);
                            } else {
                                column = homework.getColumn();
                                if (cname != null && !cname.isBlank() && !cname.equals(column.getName())) {
                                    column.setName(cname);
                                }
                                if (!column.getDay().getDate().equals(Date.valueOf(duedate))) {
                                    CalendarDay day = calendarRepo.findBySemesterAndDate(gradebook.getSemester(), Date.valueOf(duedate));
                                    if (day == null) {
                                        day = new CalendarDay(Date.valueOf(duedate));
                                        day = calendarRepo.save(day);
                                    }
                                    column.setDay(day);
                                }
                            }
                            column = assignmentRepo.save(column);
                            homework.setColumn(column);
                            GradebookEntry entry;
                            for(Student s: gradebook.getGroup().getStudents()) {
                                entry = gradeRepo.findByAssignmentAndStudent(column, s);
                                if (entry == null) {
                                    entry = new GradebookEntry(gradebook, s, column);
                                    entry.setAttendance(false);
                                    gradeRepo.save(entry);
                                }
                            }
                        }
                        homework = homeworkRepo.save(homework);
                        assignment.setHomework(homework);
                        assignmentRepo.save(assignment);
                        redirectView.setUrl("http://localhost:8088/logged-in/" + uid + "/gradebook/" + gb_id + "/edit");
                    } else {
                        redirectView.setUrl("http://localhost:8088/err");
                    }
                } else {
                    redirectView.setUrl("http://localhost:8088/err");
                }
            } else {
                redirectView.setUrl("http://localhost:8088/err");
            }
        } else {
            redirectView.setUrl("http://localhost:8088/err");
        }
        redirectView.setHosts();
        return redirectView;
    }


    @PostMapping("logged-in/{uid}/gradebook/{gb_id}/edit_column")
    @ResponseBody
    public RedirectView editColumn(@PathVariable Long uid, @PathVariable Long gb_id,
                                 @RequestParam(required = false) Long col_id,
                                 @RequestParam String col_name,
                                 @RequestParam(required = false) String date,
                                 @RequestParam(required = false) Boolean att,
                                 HttpServletRequest request) {
        RedirectView redirectView = new RedirectView();
        if (col_name.isBlank() || (col_id == null && (date == null || date.isBlank()) )) {
            redirectView.setUrl("http://localhost:8088/err");
            redirectView.setHosts();
            return redirectView;
        }
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            Teacher teacher = userRepo.findById(uid).get().getTeacher();
            Optional<Gradebook> g = gradebookRepo.findById(gb_id);
            Date today = new Date(System.currentTimeMillis());
            ArrayList<Semester> semesters = new ArrayList<>(semesterRepo.findAllBySchoolAndStartDateBeforeAndEndDateAfter(teacher.getSchool(), today, today));
            if (g.isPresent() && teacher.getGradebooks().contains(g.get()) &&
                    semesters.contains(g.get().getSemester())) {
                Gradebook gradebook = g.get();
                if (col_id == null) {
                    Semester semester = gradebook.getSemester();
                    if (semester.getStartDate().compareTo(Date.valueOf(date)) <= 0 &&
                            semester.getEndDate().compareTo(Date.valueOf(date)) >= 0) {
                        CalendarDay day = calendarRepo.findBySemesterAndDate(gradebook.getSemester(), Date.valueOf(date));
                        if (day == null) {
                            day = new CalendarDay(Date.valueOf(date));
                            day = calendarRepo.save(day);
                        }
                        Assignment column = new Assignment(col_name, gradebook, true, day, null);
                        if (att == null) {
                            att = false;
                        }
                        column = assignmentRepo.save(column);
                        GradebookEntry entry;
                        for(Student s: gradebook.getGroup().getStudents()) {
                            entry = new GradebookEntry(gradebook, s, column);
                            entry.setAttendance(att);
                            if (att) {
                                entry.setAttendanceValue(false);
                            }
                            gradeRepo.save(entry);
                        }
                    } else {
                        redirectView.setUrl("http://localhost:8088/err");
                        redirectView.setHosts();
                        return redirectView;
                    }
                } else {
                    Optional<Assignment> c = assignmentRepo.findById(col_id);
                    if (c.isPresent() && c.get().getGradebook().equals(gradebook)) {
                        Assignment column = c.get();
                        if (!column.getName().equals(col_name)) {
                            column.setName(col_name);
                            assignmentRepo.save(column);
                        }
                    } else {
                        redirectView.setUrl("http://localhost:8088/err");
                        redirectView.setHosts();
                        return redirectView;
                    }
                }
                redirectView.setUrl("http://localhost:8088/logged-in/" + uid + "/gradebook/" + gb_id + "/edit");
            } else {
                redirectView.setUrl("http://localhost:8088/err");
            }
        } else {
            redirectView.setUrl("http://localhost:8088/err");
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
                            if (userFound.getRoles().contains("*TEACHER*")) {
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
