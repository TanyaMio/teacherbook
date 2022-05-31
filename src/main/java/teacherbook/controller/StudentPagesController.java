package teacherbook.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import teacherbook.model.Course;
import teacherbook.model.StudentGroup;
import teacherbook.model.gradebook.Assignment;
import teacherbook.model.gradebook.Gradebook;
import teacherbook.model.gradebook.GradebookEntry;
import teacherbook.model.gradebook.Homework;
import teacherbook.model.schedulegrid.Semester;
import teacherbook.model.users.Student;
import teacherbook.model.users.TeacherbookUser;
import teacherbook.repositories.SemesterRepository;
import teacherbook.repositories.gradebook.AssignmentRepository;
import teacherbook.repositories.gradebook.GradebookEntryRepository;
import teacherbook.repositories.gradebook.GradebookRepository;
import teacherbook.repositories.gradebook.HomeworkRepository;
import teacherbook.repositories.users.UserRepository;
import teacherbook.security.DataValidator;
import teacherbook.util.Comparators;

import javax.servlet.http.HttpServletRequest;
import java.sql.Date;
import java.util.*;

@Controller
public class StudentPagesController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private SemesterRepository semesterRepo;

    @Autowired
    private AssignmentRepository assignmentRepo;
    @Autowired
    private HomeworkRepository homeworkRepo;
    @Autowired
    private GradebookRepository gradebookRepo;
    @Autowired
    private GradebookEntryRepository gradeRepo;

    @Autowired
    private SessionRegistry sessionRegistry;

    private DataValidator dataValidator = new DataValidator();
    private Comparators comparators = new Comparators();


    @GetMapping("logged-in/{uid}/student_home")
    public String student_home(@PathVariable Long uid,
                               Model model, HttpServletRequest request) {
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            Student student = userRepo.findById(uid).get().getStudent();
            Date today = new Date(System.currentTimeMillis());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            calendar.add(Calendar.DAY_OF_YEAR, 7);
            Date plus_week = new Date(calendar.getTimeInMillis());
            ArrayList<Semester> semesters = new ArrayList<>(semesterRepo.findAllBySchoolAndStartDateBeforeAndEndDateAfter(student.getSchool(), today, today));
            ArrayList<Gradebook> gradebooks;
            ArrayList<Homework> homework = new ArrayList<>();
            for (StudentGroup g: student.getGroups()) {
                for (Semester sem: semesters) {
                    gradebooks = new ArrayList<>(gradebookRepo.findAllBySemesterAndGroup(sem, g));
                    for (Gradebook gb: gradebooks) {
                        homework.addAll(homeworkRepo.findAllByGradebookAndDuedateAfterAndDuedateBefore(gb, today, plus_week));
                    }
                }
            }
            model.addAttribute("homework", homework);
            model.addAttribute("student", student);
            return "student_home";
        } else {
            return checkAuthResult;
        }
    }

    @GetMapping("logged-in/{uid}/student_grades")
    public String student_grades(@PathVariable Long uid,
                               Model model, HttpServletRequest request) {
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            Student student = userRepo.findById(uid).get().getStudent();
            Date today = new Date(System.currentTimeMillis());
            ArrayList<Semester> semesters = new ArrayList<>(semesterRepo.findAllBySchoolAndStartDateBeforeAndEndDateAfter(student.getSchool(), today, today));
            Map<Course, ArrayList<GradebookEntry>> grades = new HashMap<>();
            ArrayList<Gradebook> gradebooks;
            ArrayList<Assignment> assignments;
            for (StudentGroup g: student.getGroups()) {
                for (Semester sem: semesters) {
                    gradebooks = new ArrayList<>(gradebookRepo.findAllBySemesterAndGroup(sem, g));
                    for (Gradebook gb: gradebooks) {
                        Course c = gb.getCourse();
                        if (!grades.containsKey(c)) {
                            grades.put(c, new ArrayList<>());
                        }
                        assignments = new ArrayList<>(gb.getAssignments());
                        for (Assignment a: assignments) {
                            if (a.getDay().getDate().before(today)) {
                                grades.get(c).add(gradeRepo.findByAssignmentAndStudent(a, student));
                            }
                        }
                    }
                }
            }
            for (Course c: grades.keySet()) {
                grades.get(c).sort(comparators.compareGEByDate);
            }
            model.addAttribute("grades", grades);
            return "student_grades";
        } else {
            return checkAuthResult;
        }
    }

    @GetMapping("logged-in/{uid}/student_homework")
    public String student_hw(@PathVariable Long uid,
                                 Model model, HttpServletRequest request) {
        String checkAuthResult = checkAuth(uid, request);
        if (checkAuthResult.equals("**success**")) {
            Student student = userRepo.findById(uid).get().getStudent();
            Date today = new Date(System.currentTimeMillis());
            ArrayList<Semester> semesters = new ArrayList<>(semesterRepo.findAllBySchoolAndStartDateBeforeAndEndDateAfter(student.getSchool(), today, today));
            Map<Course, ArrayList<Homework>> homework = new HashMap<>();
            ArrayList<Gradebook> gradebooks;
            for (StudentGroup g: student.getGroups()) {
                for (Semester sem: semesters) {
                    gradebooks = new ArrayList<>(gradebookRepo.findAllBySemesterAndGroup(sem, g));
                    for (Gradebook gb: gradebooks) {
                        Course c = gb.getCourse();
                        if (!homework.containsKey(c)) {
                            homework.put(c, new ArrayList<>());
                        }
                        homework.get(c).addAll(homeworkRepo.findAllByGradebook(gb));
                    }
                }
            }
            model.addAttribute("homework", homework);
            return "student_hw";
        } else {
            return checkAuthResult;
        }
    }


    private String checkAuth(Long uid, HttpServletRequest request) {
        Optional<TeacherbookUser> user = userRepo.findById(uid);
        SessionInformation sessionInfo = sessionRegistry.getSessionInformation(request.getSession().getId());
        if (sessionInfo != null && !sessionInfo.isExpired()) {
            String uname = sessionInfo.getPrincipal().toString();
            if (dataValidator.loginIsValid(uname.substring(0, uname.lastIndexOf("_")))) {
                TeacherbookUser userAuth = userRepo.findByUsername(uname);
                if (userAuth != null) {
                    if (user.isPresent()) {
                        TeacherbookUser userFound = user.get();
                        if (userAuth.getId().equals(userFound.getId())) {
                            if (userFound.getRoles().contains("*STUDENT*")) {
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
