package com.example.demo.Controllers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import com.example.demo.entity.Course;
import com.example.demo.entity.CourseHighlight;
import com.example.demo.entity.CourseModule;
import com.example.demo.entity.Teacher;
import com.example.demo.enums.CourseStatus;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.TeacherRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class TeacherCourseController {

	@Autowired
	private TeacherRepository teacherRepo;
	@Autowired
	private CourseRepository courseRepo;
	
	private static final String UPLOAD_BASE =
	        System.getProperty("user.dir") + File.separator + "uploads";

	// ===== COURSE MANAGEMENT =====
	@GetMapping("/teacher-course")
	public String courseManagement(Model model,HttpSession session) {

		Integer teacherId = 2;

		 Teacher teacher = teacherRepo.findById(teacherId).orElse(null);

//		Boolean loggedIn = (Boolean) session.getAttribute("TEACHER_LOGGED_IN");
//		Integer teacherId = (Integer) session.getAttribute("teacherId");
//
//		if (loggedIn == null || !loggedIn || teacherId == null) {
//		    return "redirect:/teacher-auth";
//		}
//
//		Teacher teacher = teacherRepo.findById(teacherId).orElse(null);
//		if (teacher == null) {
//		    session.invalidate();
//		    return "redirect:/teacher-auth";
//		}
		
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
	public String saveCourse(
	        @ModelAttribute Course formCourse,
	        @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnail,
	        @RequestParam String action,
	        @RequestParam(required = false) List<String> highlightTexts,
	        Model model,
	        HttpSession session
	) throws IOException {

		
	    Integer teacherId = 2;
//		Boolean loggedIn = (Boolean) session.getAttribute("TEACHER_LOGGED_IN");
//	Integer teacherId = (Integer) session.getAttribute("teacherId");
//
//		if (loggedIn == null || !loggedIn || teacherId == null) {
//		    return "redirect:/teacher-auth";
//		}
//
		Teacher teacher = teacherRepo.findById(teacherId).orElse(null);
//		if (teacher == null) {
//		    session.invalidate();
//		    return "redirect:/teacher-auth";
//		}
		
		/* ================= HIGHLIGHT VALIDATION (HERE) ================= */

	    if (highlightTexts != null) {

	        // remove empty entries first
	        highlightTexts.removeIf(
	            t -> t == null || t.trim().isEmpty()
	        );

	        if (highlightTexts.size() > 8) {
	            model.addAttribute("error", "Maximum 8 highlights allowed.");
	            model.addAttribute("course", formCourse); // keep form data
	            return "teacher-creates-course";
	        }
	    }

	    Course course;

	    /* ================= CREATE vs UPDATE ================= */

	    if (formCourse.getCourseId() != null) {
	        // UPDATE
	        course = courseRepo.findById(formCourse.getCourseId()).orElseThrow();

	        course.setTitle(formCourse.getTitle());
	        course.setDescription(formCourse.getDescription());
	        course.setLevel(formCourse.getLevel());
	        course.setType(formCourse.getType());
	        course.setPrice(formCourse.getPrice());

	    } else {
	        // CREATE
	        boolean exists = courseRepo
	                .existsByTeacherTeacherIdAndTitleAndStatusNot(
	                        teacherId,
	                        formCourse.getTitle(),
	                        CourseStatus.DELETED
	                );

	        if (exists) {
	            model.addAttribute("course", formCourse);
	            model.addAttribute("error", "You already created a course with this title.");
	            return "teacher-creates-course";
	        }

	        course = new Course();
	        course.setTeacher(teacher);
	        course.setTitle(formCourse.getTitle());
	        course.setDescription(formCourse.getDescription());
	        course.setLevel(formCourse.getLevel());
	        course.setType(formCourse.getType());
	        course.setPrice(formCourse.getPrice());
	    }

	    /* ================= STATUS ================= */

	    course.setStatus(
	            "publish".equals(action)
	                    ? CourseStatus.PUBLISHED
	                    : CourseStatus.DRAFT
	    );

	    /* ================= SAVE COURSE FIRST (NO HIGHLIGHTS) ================= */

	    course = courseRepo.save(course); // 🔥 ID guaranteed here

	    /* ================= THUMBNAIL ================= */

	    if (thumbnail != null && !thumbnail.isEmpty()) {
	        String path = saveThumbnail(thumbnail, course.getCourseId());
	        course.setThumbnailURL(path);
	    }

	    /* ================= HIGHLIGHTS (SAFE & FINAL) ================= */

	    course.getHighlights().clear(); // orphanRemoval = true

	    if (highlightTexts != null) {
	        for (String text : highlightTexts) {
	            if (text != null && !text.trim().isEmpty()) {
	                CourseHighlight h = new CourseHighlight();
	                h.setCourse(course);           // 🔥 NEVER NULL
	                h.setText(text.trim());
	                course.getHighlights().add(h);
	            }
	        }
	    }

	    /* ================= FINAL SAVE ================= */

	    courseRepo.save(course);

	    /* ================= REDIRECT ================= */

	    if ("publish".equals(action)) {
	        return "redirect:/teacher-course";
	    }
	    model.addAttribute("highlightTexts", highlightTexts);

	    return "redirect:/teacher-creates-course?courseId=" + course.getCourseId();
	}

	@GetMapping("/teacher-course/edit/{id}")
	public String editCourse(@PathVariable Integer id) {
	    return "redirect:/teacher-creates-course?courseId=" + id;
	}

	@PostMapping("/teacher-course/delete/{id}")
	public String deleteCourse(@PathVariable("id") Integer courseId,

			HttpSession session) {
		Integer teacherId = 2;
//
//		 Boolean loggedIn =
//		            (Boolean) session.getAttribute("TEACHER_LOGGED_IN");
//		    Integer teacherId =
//		            (Integer) session.getAttribute("teacherId");
//
//		    if (loggedIn == null || !loggedIn || teacherId == null) {
//		        return "redirect:/teacher-auth";
//		    }
		    
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
	
	private String saveThumbnail(MultipartFile file, Integer courseId) throws IOException {

	    String folderPath = UPLOAD_BASE
	            + File.separator + "course-thumbnails"
	            + File.separator + courseId;

	    File dir = new File(folderPath);
	    if (!dir.exists()) {
	        dir.mkdirs();   // this WILL create full path
	    }

	    String fileName = System.currentTimeMillis()
	            + "_" + file.getOriginalFilename();

	    File destination = new File(dir, fileName);

	    file.transferTo(destination);

	    // what we store in DB (relative URL)
	    return "/uploads/course-thumbnails/" + courseId + "/" + fileName;
	}
	
	
	@PostMapping("/teacher-course/thumbnail")
	public String updateThumbnail(
	        @RequestParam Integer courseId,
	        @RequestParam MultipartFile thumbnailFile
	) throws IOException {

	    Course course = courseRepo.findById(courseId).orElseThrow();
	    String path = saveThumbnail(thumbnailFile, courseId);
	    course.setThumbnailURL(path);
	    courseRepo.save(course);

	    return "redirect:/teacher-creates-course?courseId=" + courseId
	    		+ "&msg-thumbnail_updated";
	}
	
}
