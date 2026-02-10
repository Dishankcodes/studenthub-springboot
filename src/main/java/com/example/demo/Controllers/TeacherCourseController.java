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
        List<Course> courses =
                courseRepo.findByTeacherTeacherIdAndStatusNot(
                        teacherId,
                        CourseStatus.DELETED
                );

        model.addAttribute("courses", courses);
        model.addAttribute("teacher", teacher);
		return "teacher-courses";
	}

	@GetMapping("/teacher-creates-course")
	public String createCourse(Model model) {

		model.addAttribute("course", new Course());
		return "teacher-creates-course";
	}

	@PostMapping("/teacher-creates-course")
	public String saveCourse(@ModelAttribute Course course,
			@RequestParam("action") String action ) {

		Integer teacherId = 2;
		Teacher teacher = teacherRepo.findById(teacherId).orElse(null);
		
		course.setTeacher(teacher);
		
		if("publish".equals(action)) {
		course.setStatus(CourseStatus.PUBLISHED);
		}
		else {
			course.setStatus(CourseStatus.DRAFT);
		}
		
		courseRepo.save(course);
		return "redirect:/teacher-course";

	}
	
	@GetMapping("/teacher-course/edit/{id}")
	public String editCourse(@PathVariable("id") Integer courseId,
			Model model)
	{
		
		model.addAttribute("course", courseRepo.findById(courseId));
		return "teacher-creates-course";
	}
	
	
	@PostMapping("/teacher-course/delete/{id}")
	public String deleteCourse(@PathVariable("id") Integer courseId)
	{
		Integer teacherId= 2;
		
		Course course = courseRepo.findById(courseId).orElse(null);
	

		
		if (course != null && 
	            course.getTeacher().getTeacherId().equals(teacherId)) {

	            course.setStatus(CourseStatus.DELETED);
	            courseRepo.save(course);
	            return "redirect:/teacher-course";
	        }
		
		
		
		return "redirect:/teacher-course";
	}
}
