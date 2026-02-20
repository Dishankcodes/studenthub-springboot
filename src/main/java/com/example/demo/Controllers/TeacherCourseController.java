package com.example.demo.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.Course;
import com.example.demo.entity.CourseModule;
import com.example.demo.entity.Teacher;
import com.example.demo.enums.CourseStatus;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.TeacherRepository;

@Controller
public class TeacherCourseController {

	@Autowired
	private TeacherRepository teacherRepo;
	@Autowired
	private CourseRepository courseRepo;

	// ===== COURSE MANAGEMENT =====
	@GetMapping("/teacher-course")
	public String courseManagement(Model model) {

		Integer teacherId = 2;

		Teacher teacher = teacherRepo.findById(teacherId).orElse(null);

		// ✅ ONLY fetch NON-DELETED courses
		List<Course> courses = courseRepo.findByTeacherTeacherIdAndStatusNot(teacherId, CourseStatus.DELETED);

		model.addAttribute("courses", courses);
		model.addAttribute("teacher", teacher);
		return "teacher-courses";
	}

	@GetMapping("/teacher-creates-course")
	public String createOrEditCourse(
	        @RequestParam(required = false) Integer courseId,
	        @RequestParam(required = false) Integer editModule,
	        @RequestParam(required = false) Integer openModule,
	        @RequestParam(required = false) Integer openLesson,
	        @RequestParam(required = false) Integer openQuiz,
	        Model model
	) {

	    Course course;

	    if (courseId != null) {
	        course = courseRepo.findById(courseId).orElse(null);
	        if (course == null) {
	            return "redirect:/teacher-course";
	        }
	    } else {
	        course = new Course();
	    }

	    model.addAttribute("course", course);
	    model.addAttribute("editModule", editModule);
	    model.addAttribute("openModule", openModule);
	    model.addAttribute("openLesson", openLesson);
	    model.addAttribute("openQuiz", openQuiz);

	    return "teacher-creates-course";
	}

	@PostMapping("/teacher-creates-course")
	public String saveCourse(@ModelAttribute Course formCourse, @RequestParam String action, Model model) {
		Integer teacherId = 2;
		Teacher teacher = teacherRepo.findById(teacherId).orElse(null);

		if (teacher == null) {
			return "redirect:/teacher-course";
		}

		Course course;

		/* ================= CREATE vs UPDATE ================= */

		if (formCourse.getCourseId() != null) {
			// UPDATE existing course
			course = courseRepo.findById(formCourse.getCourseId()).orElseThrow();

			course.setTitle(formCourse.getTitle());
			course.setDescription(formCourse.getDescription());
			course.setLevel(formCourse.getLevel());
			course.setType(formCourse.getType());
			course.setPrice(formCourse.getPrice());

		} else {
			// CREATE new course
			boolean exists = courseRepo.existsByTeacherTeacherIdAndTitleAndStatusNot(teacherId, formCourse.getTitle(),
					CourseStatus.DELETED);

			if (exists) {
				model.addAttribute("course", formCourse);
				model.addAttribute("error", "You already created a course with this title.");
				return "teacher-creates-course";
			}

			course = formCourse;
			course.setTeacher(teacher);
			course.setStatus(CourseStatus.DRAFT);
		}

		/* ================= STATUS HANDLING ================= */

		if ("publish".equals(action)) {
			course.setStatus(CourseStatus.PUBLISHED);
		} else {
			// draft OR next
			course.setStatus(CourseStatus.DRAFT);
		}

		Course savedCourse = courseRepo.save(course);

		/* ================= REDIRECT FLOW ================= */

		if ("publish".equals(action)) {
			return "redirect:/teacher-course";
		}

		// draft OR next
		return "redirect:/teacher-creates-course?courseId=" + savedCourse.getCourseId();
	}

	@GetMapping("/teacher-course/edit/{id}")
	public String editCourse(@PathVariable("id") Integer courseId, Model model) {

		Course course = courseRepo.findById(courseId).orElse(null);

		if (course == null) {
			return "redirect:/teacher-course";
		}

		model.addAttribute("course", course);
		return "teacher-creates-course";
	}

	@PostMapping("/teacher-course/delete/{id}")
	public String deleteCourse(@PathVariable("id") Integer courseId) {
		Integer teacherId = 2;

		Course course = courseRepo.findById(courseId).orElse(null);

		if (course != null && course.getTeacher().getTeacherId().equals(teacherId)) {

			course.setStatus(CourseStatus.DELETED);
			courseRepo.save(course);
			return "redirect:/teacher-course";
		}

		return "redirect:/teacher-course";
	}

	@PostMapping("/teacher-course/status")
	public String updateCourseStatus(@RequestParam Integer courseId, @RequestParam String action) {
		Course course = courseRepo.findById(courseId).orElseThrow();

		if ("publish".equals(action)) {
			course.setStatus(CourseStatus.PUBLISHED);
			courseRepo.save(course);
			return "redirect:/teacher-course";
		}

		// draft or next
		course.setStatus(CourseStatus.DRAFT);
		courseRepo.save(course);

		return "redirect:/teacher-creates-course?courseId=" + courseId;
	}

}
