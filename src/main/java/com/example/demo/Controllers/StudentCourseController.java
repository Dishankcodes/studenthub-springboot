package com.example.demo.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.Course;
import com.example.demo.entity.Enrollment;
import com.example.demo.entity.Lesson;
import com.example.demo.enums.CourseStatus;
import com.example.demo.enums.LessonType;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.EnrollmentRepository;

@Controller
public class StudentCourseController {

	@Autowired
	private CourseRepository courseRepo;
	
	@Autowired
	private EnrollmentRepository enrollmentRepo;
	
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
	        RedirectAttributes ra
	) {
	    Integer studentId = 1; // later from login

	    boolean enrolled =
	            enrollmentRepo.existsByStudentIdAndCourseCourseId(studentId, courseId);

	    if (!enrolled) {
	        Enrollment e = new Enrollment();
	        e.setStudentId(studentId);
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
	        Model model
	) {
	    Integer studentId = 1;

	    // 🔒 ENROLLMENT CHECK (THIS IS THE GUARD)
	    boolean enrolled =
	            enrollmentRepo.existsByStudentIdAndCourseCourseId(studentId, courseId);

	    if (!enrolled) {
	        return "redirect:/student-course-details?courseId=" + courseId;
	    }

	    Course course = courseRepo.findById(courseId).orElseThrow();

	    Lesson currentLesson = course.getModules().stream()
	            .flatMap(m -> m.getLessons().stream())
	            .findFirst()
	            .orElse(null);

	    model.addAttribute("course", course);
	    model.addAttribute("currentLesson", currentLesson);

	    return "student-course-player"; // your learning UI
	}}
