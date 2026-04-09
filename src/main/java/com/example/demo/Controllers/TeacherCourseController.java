package com.example.demo.Controllers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.Course;
import com.example.demo.entity.CourseHighlight;
import com.example.demo.entity.Teacher;
import com.example.demo.enums.CourseStatus;
import com.example.demo.enums.TeacherStatus;
import com.example.demo.repository.CourseModuleRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.LessonProgressRepository;
import com.example.demo.repository.TeacherRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class TeacherCourseController {

	@Autowired
	private TeacherRepository teacherRepo;
	@Autowired
	private CourseRepository courseRepo;

	@Autowired
	private LessonProgressRepository progressRepo;
	@Autowired
	private CourseModuleRepository moduleRepo;

	private static final String UPLOAD_BASE = System.getProperty("user.dir") + File.separator + "uploads";

	@GetMapping("/teacher-course")
	public String courseManagement(Model model, HttpSession session) {

		Integer teacherId = (Integer) session.getAttribute("teacherId");
		if (teacherId == null)
			return "redirect:/teacher-auth";

		Teacher teacher = teacherRepo.findById(teacherId).orElse(null);
		if (teacher == null) {
			session.invalidate();
			return "redirect:/teacher-auth";
		}

		List<Course> courses = courseRepo.findByTeacherTeacherIdAndStatusNot(teacherId, CourseStatus.DELETED);

		model.addAttribute("courses", courses);
		model.addAttribute("teacher", teacher);

		return "teacher-courses";
	}

	@GetMapping("/teacher-creates-course")
	public String createOrEditCourse(@RequestParam(required = false) Integer courseId,
			@RequestParam(required = false) Integer editModule, @RequestParam(required = false) Integer openModule,
			@RequestParam(required = false) Integer openLesson, @RequestParam(required = false) Integer openQuiz,
			Model model, HttpSession session) {

		Integer teacherId = (Integer) session.getAttribute("teacherId");
		if (teacherId == null)
			return "redirect:/teacher-auth";

		Course course;

		if (courseId != null) {
			course = courseRepo.findById(courseId).orElse(null);

			if (course == null || !course.getTeacher().getTeacherId().equals(teacherId)) {
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
	public String saveCourse(@ModelAttribute Course formCourse,
			@RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnail,
			@RequestParam String action, @RequestParam(required = false) List<String> highlightTexts, Model model,
			HttpSession session) throws IOException {

		Integer teacherId = (Integer) session.getAttribute("teacherId");
		if (teacherId == null)
			return "redirect:/teacher-auth";

		Teacher teacher = teacherRepo.findById(teacherId).orElse(null);
		if (teacher == null) {
			session.invalidate();
			return "redirect:/teacher-auth";
		}

		if (teacher.getStatus() == TeacherStatus.BLOCKED) {
			model.addAttribute("error", "You are blocked by admin. No activity allowed.");
			model.addAttribute("course", formCourse);
			return "teacher-creates-course";
		}

		if (teacher.getStatus() == TeacherStatus.SUSPENDED) {
			model.addAttribute("error", "Your account is suspended. You cannot create or edit courses.");
			model.addAttribute("course", formCourse);
			return "teacher-creates-course";
		}

		/* ================= HIGHLIGHT VALIDATION (HERE) ================= */

		if (highlightTexts != null) {

			// remove empty entries first
			highlightTexts.removeIf(t -> t == null || t.trim().isEmpty());

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
			boolean exists = courseRepo.existsByTeacherTeacherIdAndTitleAndStatusNot(teacherId, formCourse.getTitle(),
					CourseStatus.DELETED);

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

		course.setStatus("publish".equals(action) ? CourseStatus.PUBLISHED : CourseStatus.DRAFT);

		course = courseRepo.save(course);

		if (thumbnail != null && !thumbnail.isEmpty()) {
			String path = saveThumbnail(thumbnail, course.getCourseId());
			course.setThumbnailURL(path);
		}

		course.getHighlights().clear();

		if (highlightTexts != null) {
			for (String text : highlightTexts) {
				if (text != null && !text.trim().isEmpty()) {
					CourseHighlight h = new CourseHighlight();
					h.setCourse(course); // 🔥 NEVER NULL
					h.setText(text.trim());
					course.getHighlights().add(h);
				}
			}
		}

		courseRepo.save(course);

		if ("publish".equals(action)) {
			return "redirect:/teacher-course";
		}

		model.addAttribute("highlightTexts", highlightTexts);
		return "redirect:/teacher-creates-course?courseId=" + course.getCourseId();
	}

	@GetMapping("/teacher-course/edit/{id}")
	public String editCourse(@PathVariable Integer id, HttpSession session) {

		Integer teacherId = (Integer) session.getAttribute("teacherId");
		if (teacherId == null)
			return "redirect:/teacher-auth";

		Teacher teacher = teacherRepo.findById(teacherId).orElse(null);
		if (teacher == null) {
			session.invalidate();
			return "redirect:/teacher-auth";
		}

		if (teacher.getStatus() == TeacherStatus.BLOCKED) {
			return "redirect:/teacher-course?error=blocked";
		}

		if (teacher.getStatus() == TeacherStatus.SUSPENDED) {
			return "redirect:/teacher-course?error=suspended";
		}

		return "redirect:/teacher-creates-course?courseId=" + id;
	}

	@PostMapping("/teacher-course/delete/{id}")
	public String deleteCourse(@PathVariable("id") Integer courseId,

			HttpSession session) {
		Integer teacherId = (Integer) session.getAttribute("teacherId");
		if (teacherId == null)
			return "redirect:/teacher-auth";

		Course course = courseRepo.findById(courseId).orElse(null);

		if (course != null && course.getTeacher().getTeacherId().equals(teacherId)) {

			course.setStatus(CourseStatus.DELETED);
			courseRepo.save(course);
			return "redirect:/teacher-course";
		}

		return "redirect:/teacher-course";
	}

	@PostMapping("/teacher-course/status")
	public String updateCourseStatus(@RequestParam Integer courseId, @RequestParam String action, HttpSession session) {

		Integer teacherId = (Integer) session.getAttribute("teacherId");
		if (teacherId == null)
			return "redirect:/teacher-auth";

		Teacher teacher = teacherRepo.findById(teacherId).orElse(null);
		if (teacher == null) {
			session.invalidate();
			return "redirect:/teacher-auth";
		}
		if (teacher.getStatus() == TeacherStatus.BLOCKED) {
			return "redirect:/teacher-course?error=blocked";
		}

		if (teacher.getStatus() == TeacherStatus.SUSPENDED) {
			return "redirect:/teacher-course?error=suspended";
		}

		Course course = courseRepo.findById(courseId).orElseThrow();

		if ("publish".equals(action)) {
			course.setStatus(CourseStatus.PUBLISHED);
		} else {
			course.setStatus(CourseStatus.DRAFT);
		}

		courseRepo.save(course);

		return "redirect:/teacher-course";
	}

	private String saveThumbnail(MultipartFile file, Integer courseId) throws IOException {

		String folderPath = UPLOAD_BASE + File.separator + "course-thumbnails" + File.separator + courseId;

		File dir = new File(folderPath);
		if (!dir.exists()) {
			dir.mkdirs(); 
		}

		String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

		File destination = new File(dir, fileName);

		file.transferTo(destination);

		
		return "/uploads/course-thumbnails/" + courseId + "/" + fileName;
	}

	@PostMapping("/teacher-course/thumbnail")
	public String updateThumbnail(@RequestParam Integer courseId, @RequestParam MultipartFile thumbnailFile)
			throws IOException {

		Course course = courseRepo.findById(courseId).orElseThrow();
		String path = saveThumbnail(thumbnailFile, courseId);
		course.setThumbnailURL(path);
		courseRepo.save(course);

		return "redirect:/teacher-creates-course?courseId=" + courseId + "&msg-thumbnail_updated";
	}

	@GetMapping("/teacher-course/analytics/{id}")
	public String courseAnalytics(@PathVariable Integer id, Model model) {

		Course course = courseRepo.findById(id).orElse(null);

		if (course == null) {
			return "redirect:/teacher-course";
		}

		long totalStudents = course.getEnrollments().size();

		long activeStudents = course.getEnrollments().stream().filter(e -> e.getStatus().name().equals("ACTIVE"))
				.count();

		long suspendedStudents = course.getEnrollments().stream().filter(e -> e.getStatus().name().equals("SUSPENDED"))
				.count();

		long moduleCount = moduleRepo.countByCourseCourseId(id);

		long lessonCount = course.getModules().stream().mapToLong(m -> m.getLessons().size()).sum();

		long quizCount = course.getModules().stream().flatMap(m -> m.getLessons().stream())
				.filter(l -> l.getQuiz() != null).count();

		List<Map<String, Object>> studentProgressList = new ArrayList<>();

		course.getEnrollments().forEach(enrollment -> {

			Integer studentId = enrollment.getStudent().getStudid();

			long completedLessons = progressRepo
					.countByStudentStudidAndLessonModuleCourseCourseIdAndCompletedTrue(studentId, course.getCourseId());

			int progress = 0;

			if (lessonCount > 0) {
				progress = (int) ((completedLessons * 100) / lessonCount);
			}

			Map<String, Object> data = new HashMap<>();

			data.put("student", enrollment.getStudent());
			data.put("completed", completedLessons);
			data.put("total", lessonCount);
			data.put("progress", progress);

			studentProgressList.add(data);
		});

		model.addAttribute("course", course);
		model.addAttribute("totalStudents", totalStudents);
		model.addAttribute("activeStudents", activeStudents);
		model.addAttribute("suspendedStudents", suspendedStudents);
		model.addAttribute("moduleCount", moduleCount);
		model.addAttribute("lessonCount", lessonCount);
		model.addAttribute("quizCount", quizCount);
		model.addAttribute("modules", course.getModules());
		model.addAttribute("studentProgressList", studentProgressList);

		return "course-analytics";
	}

}
