package com.example.demo.Controllers;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.InternshipApplication;
import com.example.demo.entity.InternshipTest;
import com.example.demo.entity.InternshipTestAttempt;
import com.example.demo.entity.Internships;
import com.example.demo.entity.QuizQuestion;
import com.example.demo.entity.Student;
import com.example.demo.entity.TestAnswer;
import com.example.demo.enums.ApplicationStatus;
import com.example.demo.enums.QuestionFormat;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.EnrollmentRepository;
import com.example.demo.repository.InternshipRepository;
import com.example.demo.repository.InternshipTestAttemptRepository;
import com.example.demo.repository.InternshipTestRepository;
import com.example.demo.repository.QuizQuestionRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.TestAnswerRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class StudentInternshipController {

	@Autowired
	private InternshipRepository internshipRepo;

	@Autowired
	private ApplicationRepository applicationRepo;

	@Autowired
	private StudentRepository studentRepo;

	@Autowired
	private EnrollmentRepository enrollmentRepo;

	@Autowired
	private QuizQuestionRepository questionRepo;

	@Autowired
	private InternshipTestAttemptRepository attemptRepo;
	
	@Autowired
	private InternshipTestRepository testRepo;
	
	@Autowired
	private TestAnswerRepository answerRepo;

	@GetMapping("/student-internships")
	public String studentInternships(Model model, HttpSession session) {

		Integer studentId = (Integer) session.getAttribute("studentId");

		if (studentId == null) {
			return "redirect:/student-login";
		}

		List<Internships> internships = internshipRepo.findAll();

		List<InternshipApplication> applications = applicationRepo.findByStudent_Studid(studentId);

		Map<Integer, InternshipApplication> appMap = new HashMap<>();

		for (InternshipApplication app : applications) {
			appMap.put(app.getInternship().getId(), app);
		}

		model.addAttribute("internships", internships);
		model.addAttribute("appMap", appMap);

		return "student-internships";
	}

	@GetMapping("/student-internship-detail")
	public String internshipDetail(@RequestParam Integer id, Model model, HttpSession session) {

		Integer studentId = (Integer) session.getAttribute("studentId");

		if (studentId == null)
			return "redirect:/student-login";

		Internships internship = internshipRepo.findById(id).orElse(null);

		if (internship == null) {
		    return "redirect:/student-internships";
		}
		InternshipApplication application = applicationRepo.findByStudent_StudidAndInternship_Id(studentId, id)
				.orElse(null);

		// ✅ SAFE DEBUG
		if (application != null) {
			System.out.println("STATUS: " + application.getStatus());
		} else {
			System.out.println("NO APPLICATION");
		}

		InternshipTestAttempt attempt = attemptRepo.findByStudentStudidAndInternshipId(studentId, id);

		
		model.addAttribute("testAttempt", attempt);
		model.addAttribute("internship", internship);
		model.addAttribute("app", application);

		return "student-internship-detail";
	}

	@PostMapping("/student/apply")
	public String applyInternship(@RequestParam Integer internshipId, @RequestParam String fullName,
			@RequestParam String email, @RequestParam String phone, @RequestParam(required = false) String coverLetter,
			@RequestParam(required = false) MultipartFile resume, RedirectAttributes redirectAttributes,
			HttpSession session) throws Exception {

		Integer studentId = (Integer) session.getAttribute("studentId");

		if (studentId == null) {
			return "redirect:/student-login";
		}

		Internships internship = internshipRepo.findById(internshipId).orElse(null);

		Student student = studentRepo.findById(studentId).orElse(null);

		// already applied
		if (applicationRepo.existsByStudent_StudidAndInternship_Id(studentId, internshipId)) {
			redirectAttributes.addFlashAttribute("error", "Already applied.");
			return "redirect:/student-internship-detail?id=" + internshipId;
		}

		// course eligibility
		if (internship.getRequiredCourse() != null) {

			List<Integer> completedCourses = enrollmentRepo.findCompletedCourses(studentId);

			if (!completedCourses.contains(internship.getRequiredCourse().getCourseId())) {
				redirectAttributes.addFlashAttribute("error", "Complete required course first.");
				return "redirect:/student-internship-detail?id=" + internshipId;
			}
		}

		InternshipApplication app = new InternshipApplication();

		app.setStudent(student);
		app.setInternship(internship);
		app.setStatus(ApplicationStatus.PENDING);

		app.setFullName(fullName);
		app.setEmail(email);
		app.setPhone(phone);
		app.setCoverLetter(coverLetter);

		if (resume != null && !resume.isEmpty()) {

			String basePath = System.getProperty("user.dir") + "/uploads/resumes/" + studentId;

			File dir = new File(basePath);
			if (!dir.exists()) {
				dir.mkdirs();
			}

			String fileName = System.currentTimeMillis() + "_" + resume.getOriginalFilename();

			File destination = new File(dir, fileName);
			resume.transferTo(destination);

			app.setResumeUrl("/uploads/resumes/" + studentId + "/" + fileName);
		}

		applicationRepo.save(app);

		
		
		redirectAttributes.addFlashAttribute("success", "Applied Successfully 🚀");

		return "redirect:/student-internship-detail?id=" + internshipId;
	}

	@GetMapping("/student/test/start")
	public String startTest(@RequestParam Integer internshipId,
	                        Model model,
	                        HttpSession session) {

	    Integer studentId = (Integer) session.getAttribute("studentId");

	    if (studentId == null) {
	        return "redirect:/student-login";
	    }

	    // 🔒 Prevent reattempt
	    InternshipTestAttempt existing =
	            attemptRepo.findByStudentStudidAndInternshipId(studentId, internshipId);

	    if (existing != null && existing.isSubmitted()) {
	        return "redirect:/student-internship-detail?id=" + internshipId;
	    }

	    // 🔥 Get test
	    InternshipTest test = testRepo.findByInternshipId(internshipId);

	    // ❌ Block if test not confirmed
	    if (test == null || !test.isActive()) {
	        return "redirect:/student-internship-detail?id=" + internshipId;
	    }

	    // 🔐 Try to get existing questions from session
	    Object obj = session.getAttribute("testQuestions_" + internshipId);

	    List<QuizQuestion> selected = null;

	    if (obj instanceof List<?>) {
	        selected = (List<QuizQuestion>) obj;
	    }

	    // 🔁 If first time → generate & store
	    if (selected == null) {

	        List<QuizQuestion> allQuestions =
	                questionRepo.findByInternshipId(internshipId);

	        if (allQuestions == null || allQuestions.isEmpty()) {
	            return "redirect:/student-internship-detail?id=" + internshipId;
	        }

	        // 🔀 Shuffle only once
	        Collections.shuffle(allQuestions);

	        int limit = test.getTotalQuestionsToShow();

	        selected = allQuestions.stream()
	                .limit(limit)
	                .toList();

	        // 💾 Save in session (IMPORTANT)
	        session.setAttribute("testQuestions_" + internshipId, selected);
	    }

	    // 🎯 Send to UI
	    model.addAttribute("questions", selected);
	    model.addAttribute("internshipId", internshipId);

	    return "student-test";
	}
	@PostMapping("/student/test/submit")
	public String submitTest(
	        @RequestParam Integer internshipId,
	        @RequestParam Map<String, String> formData,
	        HttpSession session,
	        RedirectAttributes ra) {

	    Integer studentId = (Integer) session.getAttribute("studentId");

	    if (studentId == null) {
	        return "redirect:/student-login";
	    }

	    Student student = studentRepo.findById(studentId).orElse(null);

	    if (student == null) {
	        return "redirect:/student-login";
	    }

	    // 🔒 BLOCK MULTIPLE ATTEMPTS
	    InternshipTestAttempt existing =
	            attemptRepo.findByStudentStudidAndInternshipId(studentId, internshipId);

	    if (existing != null && existing.isSubmitted()) {
	        ra.addFlashAttribute("msg", "⚠️ You have already submitted this assessment.");
	        return "redirect:/student-internship-detail?id=" + internshipId;
	    }

	    List<QuizQuestion> questions =
	            questionRepo.findByInternshipId(internshipId);

	    // 🔀 SHUFFLE QUESTIONS (REAL EXAM FEEL)
	    Collections.shuffle(questions);

	    int score = 0;
	    int total = 0;

	    InternshipTest test = testRepo.findByInternshipId(internshipId);

	    // 🔥 CREATE ATTEMPT FIRST
	    InternshipTestAttempt attempt = new InternshipTestAttempt();
	    attempt.setStudent(student);
	    attempt.setInternship(internshipRepo.findById(internshipId).orElse(null));
	    attempt.setSubmitted(false);

	    attempt = attemptRepo.save(attempt);

	    // 🔥 LOOP QUESTIONS
	    for (QuizQuestion q : questions) {

	        total += q.getMarks();

	        String key = "q_" + q.getQuestionId();
	        String value = formData.get(key);

	        TestAnswer ans = new TestAnswer();
	        ans.setAttempt(attempt);
	        ans.setQuestion(q);

	        if (q.getQuestionFormat() == QuestionFormat.MCQ) {

	            ans.setSelectedOption(value);

	            if (value != null && value.equals(q.getCorrectOption())) {
	                score += q.getMarks();
	            }

	        } else {
	            // TEXT / CODE
	            ans.setAnswerText(value);
	        }

	        answerRepo.save(ans);
	    }

	    boolean passed = score >= test.getPassingMarks();

	    attempt.setScore(score);
	    attempt.setTotalMarks(total);
	    attempt.setPassed(passed);
	    attempt.setSubmitted(true);

	    attemptRepo.save(attempt);

	    // 🎯 REAL PRODUCT MESSAGES
	    if (passed) {
	        ra.addFlashAttribute("msg",
	                "🎉 Assessment completed successfully! You have cleared the test.\n" +
	                "Our team will review your profile and contact you shortly.\n" +
	                "You can also track updates in your dashboard.");
	    } else {
	        ra.addFlashAttribute("msg",
	                "✅ Assessment submitted.\n" +
	                "Our team will review your responses and get back to you.\n" +
	                "Keep checking your dashboard for updates.");
	    }

	    session.removeAttribute("testQuestions_" + internshipId);
	    return "redirect:/student-internship-detail?id=" + internshipId;
	}
}