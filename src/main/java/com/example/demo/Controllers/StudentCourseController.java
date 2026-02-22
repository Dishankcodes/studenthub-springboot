package com.example.demo.Controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.Course;
import com.example.demo.entity.Enrollment;
import com.example.demo.entity.Lesson;
import com.example.demo.entity.LessonProgress;
import com.example.demo.entity.Quiz;
import com.example.demo.entity.QuizQuestion;
import com.example.demo.entity.Student;
import com.example.demo.enums.CourseStatus;
import com.example.demo.enums.LessonType;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.EnrollmentRepository;
import com.example.demo.repository.LessonProgressRepository;
import com.example.demo.repository.LessonRepository;
import com.example.demo.repository.QuizQuestionRepository;
import com.example.demo.repository.StudentRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class StudentCourseController {

	@Autowired
	private CourseRepository courseRepo;
	
	@Autowired
	private EnrollmentRepository enrollmentRepo;
	
	@Autowired
	private LessonProgressRepository lessonProgressRepo;
	
	@Autowired
	private LessonRepository lessonRepo;
	
	@Autowired
	private QuizQuestionRepository quizQuestionRepo;
	
	@Autowired
	private StudentRepository studentRepo;
	
	@GetMapping("/student-course")
	public String exploreCourse(Model model) {

		List<Course> courses = courseRepo.findByStatus(CourseStatus.PUBLISHED);
		
		model.addAttribute("courses", courses);
		return "student-course";
	}
	@GetMapping("/student-course-details")
	public String viewCourse(
	        @RequestParam Integer courseId,
	        Model model
	) {
	    Course course = courseRepo.findPublishedCourseForStudent(
	            courseId,
	            CourseStatus.PUBLISHED
	    );

	    if (course == null) {
	        return "redirect:/student-course";
	    }

	    Lesson previewLesson = course.getModules().stream()
	            .flatMap(m -> m.getLessons().stream())
	            .filter(Lesson::isFreePreview)
	            .filter(l -> l.getType() == LessonType.VIDEO)
	            .findFirst()
	            .orElse(null);

	    model.addAttribute("course", course);
	    model.addAttribute("previewLesson", previewLesson);

	    return "student-course-details";
	}

	@PostMapping("/student-enroll")
	public String confirmEnrollment(
	        @RequestParam Integer courseId,
	        RedirectAttributes ra,
	        HttpSession session
			) {
			    Integer studentId = (Integer) session.getAttribute("studentId");

			    if (studentId == null) {
			        return "redirect:/student-login";
			    }

			    Student student = studentRepo.findById(studentId).orElseThrow();

	    boolean enrolled =
	            enrollmentRepo.existsByStudentStudidAndCourseCourseId(studentId, courseId);

	    if (!enrolled) {
	        Enrollment e = new Enrollment();
	        e.setStudent(student);
	        e.setCourse(courseRepo.findById(courseId).orElseThrow());
	        enrollmentRepo.save(e);
	    }

	    ra.addFlashAttribute("success", "Enrolled successfully!");

	    // ✅ ALWAYS GO TO LEARNING PAGE
	    return "redirect:/student-course-player/" + courseId;
	}
	
	@GetMapping("/student-course-player/{courseId}")
	public String openCoursePlayer(
	        @PathVariable Integer courseId,
	        @RequestParam(required = false) Integer lessonId,
	        Model model,
	        HttpSession session
			) {
			    Integer studentId = (Integer) session.getAttribute("studentId");

			    if (studentId == null) {
			        return "redirect:/student-login";
			    }


	    // 🔒 enrollment check
	    boolean enrolled =
	            enrollmentRepo.existsByStudentStudidAndCourseCourseId(studentId, courseId);

	    if (!enrolled) {
	        return "redirect:/student-course-details?courseId=" + courseId;
	    }

	    Course course = courseRepo.findById(courseId).orElseThrow();

	    Lesson currentLesson;

	    if (lessonId != null) {
	        // open clicked lesson
	        currentLesson = course.getModules().stream()
	                .flatMap(m -> m.getLessons().stream())
	                .filter(l -> l.getLessonId().equals(lessonId))
	                .findFirst()
	                .orElse(null);
	    } else {
	        // default = first lesson
	        currentLesson = course.getModules().stream()
	                .flatMap(m -> m.getLessons().stream())
	                .findFirst()
	                .orElse(null);
	    }
	    
	    
	    long completedLessons =
	            lessonProgressRepo
	                .countByStudentStudidAndLessonModuleCourseCourseIdAndCompletedTrue(
	                        studentId, courseId);

	    long totalLessons = course.getModules().stream()
	            .mapToLong(m -> m.getLessons().size())
	            .sum();

	    int progressPercent =
	            totalLessons == 0 ? 0 :
	            (int)((completedLessons * 100) / totalLessons);

	    List<Integer> completedLessonIds =
	            lessonProgressRepo.findCompletedLessonIds(studentId, courseId);
	    
	    // 🔥 QUIZ QUESTIONS
	    if (currentLesson != null && currentLesson.getType() == LessonType.QUIZ) {
	        model.addAttribute(
	            "questions",
	            quizQuestionRepo.findByQuiz(currentLesson.getQuiz())
	        );
	    }
	    
	    model.addAttribute("completedLessonIds", completedLessonIds);
	    model.addAttribute("progressPercent", progressPercent);
	    model.addAttribute("course", course);
	    model.addAttribute("currentLesson", currentLesson);

	    return "student-course-player";
	}
	
	@PostMapping("/student/lesson/complete")
	@ResponseBody
	public String completeLesson(
	        @RequestParam Integer lessonId,
	        HttpSession session
			) {
			    Integer studentId = (Integer) session.getAttribute("studentId");

			    if (studentId == null) {
			        return "redirect:/student-login";
			    }
			    Student student = studentRepo.findById(studentId).orElseThrow();

	    if (!lessonProgressRepo
	            .existsByStudentStudidAndLessonLessonId(studentId, lessonId)) {

	        LessonProgress lp = new LessonProgress();
	        lp.setStudent(student);
	        lp.setLesson(lessonRepo.findById(lessonId).orElseThrow());
	        lp.setCompleted(true);
	        lp.setCompletedAt(LocalDateTime.now());

	        lessonProgressRepo.save(lp);
	    }

	    return "ok";
	}
	
	@PostMapping("/student/quiz/submit")
	public String submitQuiz(
	        @RequestParam Integer lessonId,
	        @RequestParam Map<String, String> answers,
	        HttpSession session
			) {
			    Integer studentId = (Integer) session.getAttribute("studentId");

			    if (studentId == null) {
			        return "redirect:/student-login";
			    }

			    Student student = studentRepo.findById(studentId).orElseThrow();
			 
	    Lesson lesson = lessonRepo.findById(lessonId).orElseThrow();
	    Quiz quiz = lesson.getQuiz();

	    int score = 0;

	    List<QuizQuestion> questions =
	            quizQuestionRepo.findByQuiz(quiz);

	    for (QuizQuestion q : questions) {
	        String given = answers.get("q_" + q.getQuestionId());
	        if (q.getCorrectOption().equals(given)) {
	            score++;
	        }
	    }

	    // 🔥 Save progress
	    if (!lessonProgressRepo
        .existsByStudentStudidAndLessonLessonId(studentId, lesson.getLessonId())) {

    LessonProgress lp = new LessonProgress();
    lp.setStudent(student);
    lp.setLesson(lesson);
    lp.setCompleted(true);
    lp.setCompletedAt(LocalDateTime.now());

    lessonProgressRepo.save(lp);
}

	    // 🔁 Redirect back to course
	    return "redirect:/student-course-player/" +
	           lesson.getModule().getCourse().getCourseId() +
	           "?lessonId=" + lessonId;
	}
}
