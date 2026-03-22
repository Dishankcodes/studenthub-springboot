package com.example.demo.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.Service.EmailService;
import com.example.demo.entity.InternshipApplication;
import com.example.demo.entity.InternshipTest;
import com.example.demo.entity.InternshipTestAttempt;
import com.example.demo.entity.Internships;
import com.example.demo.entity.QuizQuestion;
import com.example.demo.entity.TestAnswer;
import com.example.demo.enums.ApplicationStatus;
import com.example.demo.enums.QuestionFormat;
import com.example.demo.enums.QuizQuestionType;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.InternshipRepository;
import com.example.demo.repository.InternshipTestAttemptRepository;
import com.example.demo.repository.InternshipTestRepository;
import com.example.demo.repository.QuizQuestionRepository;
import com.example.demo.repository.TestAnswerRepository;

@Controller
public class AdminInternshipTestController {

	@Autowired
	private InternshipRepository internshipRepo;

	@Autowired
	private InternshipTestRepository testRepo;

	@Autowired
	private QuizQuestionRepository questionRepo;

	@Autowired
	private InternshipTestAttemptRepository attemptRepo;

	@Autowired
	private ApplicationRepository applicationRepo;

	
	@Autowired
	private TestAnswerRepository answerRepo;
	
	@Autowired
	private EmailService emailService;

	
	@GetMapping("/admin-internship-test")
	public String openTest(@RequestParam Integer internshipId, Model model) {

	    Internships internship = internshipRepo.findById(internshipId).orElse(null);

	    if (internship == null) {
	        return "redirect:/manage-internships";
	    }

	    // ✅ GET TEST (DO NOT AUTO CREATE)
	    InternshipTest test = testRepo.findByInternshipId(internshipId);

	    // ✅ GET QUESTIONS
	    List<QuizQuestion> questions = questionRepo.findByInternshipId(internshipId);

	    // ✅ PASS EVERYTHING TO UI
	    model.addAttribute("test", test);
	    model.addAttribute("internship", internship);
	    model.addAttribute("questions", questions);

	    return "admin-internship-test";
	}
	@PostMapping("/admin/enable-test")
	public String enableTest(@RequestParam Integer internshipId,
	                         RedirectAttributes ra) {

	    Internships i = internshipRepo.findById(internshipId).orElse(null);

	    if (i == null) {
	        return "redirect:/manage-internships";
	    }

	    // ✅ ENABLE TEST
	    i.setHasTest(true);
	    internshipRepo.save(i);

	    // ✅ CREATE TEST OBJECT (IMPORTANT)
	    InternshipTest test = testRepo.findByInternshipId(internshipId);

	    if (test == null) {
	        test = new InternshipTest();
	        test.setInternship(i);
	        test.setDurationMinutes(10);
	        test.setPassingMarks(40);
	        test.setTotalQuestionsToShow(5);
	        testRepo.save(test);
	    }

	    ra.addFlashAttribute("msg", "✅ Test created. Now add questions");

	    return "redirect:/admin-internship-test?internshipId=" + internshipId;
	}

	@PostMapping("/admin-internship-test/bulk-save")
	public String bulkSave(@RequestParam Integer internshipId,
			@RequestParam(required = false) List<String> questionText,
			@RequestParam(required = false) List<String> format, @RequestParam(required = false) List<String> optionA,
			@RequestParam(required = false) List<String> optionB, @RequestParam(required = false) List<String> optionC,
			@RequestParam(required = false) List<String> optionD,
			@RequestParam(required = false) List<String> correctOption, RedirectAttributes ra) {

		// ❌ no questions generated
		if (questionText == null || questionText.isEmpty()) {
			ra.addFlashAttribute("error", "⚠️ Please generate questions before saving");
			return "redirect:/admin-internship-test?internshipId=" + internshipId;
		}

		Internships internship = internshipRepo.findById(internshipId).orElse(null);

		if (internship == null) {
			return "redirect:/manage-internships";
		}

		int position = questionRepo.findByInternshipId(internshipId).size() + 1;

		int mcqIndex = 0;

		for (int i = 0; i < questionText.size(); i++) {

			String text = questionText.get(i);

			// ✅ skip empty safely
			if (text == null || text.trim().isEmpty()) {
				continue;
			}

			QuizQuestion q = new QuizQuestion();
			q.setInternship(internship);
			q.setQuestionText(text.trim());
			q.setMarks(1);
			q.setPosition(position++);
			q.setType(QuizQuestionType.INTERNSHIP);

			String type = format.get(i);

			if ("MCQ".equals(type)) {

				q.setQuestionFormat(QuestionFormat.MCQ);

				// ✅ safe checks for lists
				q.setOptionA(getSafe(optionA, mcqIndex));
				q.setOptionB(getSafe(optionB, mcqIndex));
				q.setOptionC(getSafe(optionC, mcqIndex));
				q.setOptionD(getSafe(optionD, mcqIndex));
				q.setCorrectOption(getSafe(correctOption, mcqIndex));

				mcqIndex++;

			} else if ("TEXT".equals(type)) {

				q.setQuestionFormat(QuestionFormat.TEXT);

			} else if ("CODE".equals(type)) {

				q.setQuestionFormat(QuestionFormat.CODE);
			}

			questionRepo.save(q);
		}

		ra.addFlashAttribute("msg", "Questions Saved Successfully ✅");

		return "redirect:/admin-internship-test?internshipId=" + internshipId;
	}

	private String getSafe(List<String> list, int index) {
		if (list == null || index >= list.size()) {
			return null;
		}
		return list.get(index);
	}

	@PostMapping("/admin-internship-test/add-question")
	public String addQuestion(@RequestParam Integer internshipId, @RequestParam String questionText,
			@RequestParam String format, @RequestParam(required = false) String optionA,
			@RequestParam(required = false) String optionB, @RequestParam(required = false) String optionC,
			@RequestParam(required = false) String optionD, @RequestParam(required = false) String correctOption,
			RedirectAttributes ra) {

		Internships internship = internshipRepo.findById(internshipId).orElse(null);

		if (internship == null) {
			return "redirect:/manage-internships";
		}

		if (questionText == null || questionText.trim().isEmpty()) {
			ra.addFlashAttribute("error", "Question cannot be empty ❌");
			return "redirect:/admin-internship-test?internshipId=" + internshipId;
		}

		QuizQuestion q = new QuizQuestion();

		q.setInternship(internship);
		q.setQuestionText(questionText.trim());
		q.setMarks(1);
		q.setType(QuizQuestionType.INTERNSHIP);

		int position = questionRepo.findByInternshipId(internshipId).size() + 1;
		q.setPosition(position);

		if ("MCQ".equals(format)) {

			if (correctOption == null || correctOption.isEmpty()) {
				ra.addFlashAttribute("error", "Correct option required for MCQ ❌");
				return "redirect:/admin-internship-test?internshipId=" + internshipId;
			}

			q.setQuestionFormat(QuestionFormat.MCQ);
			q.setOptionA(optionA);
			q.setOptionB(optionB);
			q.setOptionC(optionC);
			q.setOptionD(optionD);
			q.setCorrectOption(correctOption);

		} else if ("TEXT".equals(format)) {

			q.setQuestionFormat(QuestionFormat.TEXT);
			q.setCorrectOption(null); // explicit safe

		} else if ("CODE".equals(format)) {

			q.setQuestionFormat(QuestionFormat.CODE);
			q.setCorrectOption(null); // explicit safe
		}
		questionRepo.save(q);

		ra.addFlashAttribute("msg", "Question Added ✅");

		return "redirect:/admin-internship-test?internshipId=" + internshipId;
	}

	@GetMapping("/admin-internship-test/delete")
	public String deleteQuestion(@RequestParam Integer questionId, @RequestParam Integer internshipId,
			RedirectAttributes ra) {

		questionRepo.deleteById(questionId);

		ra.addFlashAttribute("msg", "Question Deleted 🗑️");

		return "redirect:/admin-internship-test?internshipId=" + internshipId;
	}

	@PostMapping("/admin-internship-test/update")
	public String updateTest(@RequestParam Integer internshipId, @RequestParam int duration,
			@RequestParam int passingMarks, @RequestParam int totalQuestions, RedirectAttributes ra) {

		InternshipTest test = testRepo.findByInternshipId(internshipId);

		if (test == null) {
			ra.addFlashAttribute("error", "Test not found ❌");
			return "redirect:/manage-internships";
		}

		test.setDurationMinutes(duration);
		test.setPassingMarks(passingMarks);
		test.setTotalQuestionsToShow(totalQuestions);

		testRepo.save(test);

		ra.addFlashAttribute("msg", "Test Updated ✅");

		return "redirect:/admin-internship-test?internshipId=" + internshipId;
	}

	@PostMapping("/admin-internship-test/confirm")
	public String confirmTest(@RequestParam Integer internshipId, RedirectAttributes ra) {

		InternshipTest test = testRepo.findByInternshipId(internshipId);

		if (test == null) {
			ra.addFlashAttribute("error", "Test not found ❌");
			return "redirect:/admin-internship-test?internshipId=" + internshipId;
		}

		test.setActive(true);
		testRepo.save(test);

		ra.addFlashAttribute("msg", "✅ Test is now LIVE for students");

		return "redirect:/admin-internship-test?internshipId=" + internshipId;
	}

	@GetMapping("/admin-test-results")
	public String testResults(@RequestParam Integer internshipId, Model model) {

		List<InternshipTestAttempt> attempts = attemptRepo.findByInternship_Id(internshipId);

		model.addAttribute("attempts", attempts);
		model.addAttribute("internshipId", internshipId);

		return "admin-test-results";
	}

	@PostMapping("/admin-test-result/update")
	public String updateTestResult(@RequestParam Integer attemptId, @RequestParam Integer internshipId) {

		InternshipTestAttempt attempt = attemptRepo.findById(attemptId).orElse(null);

		if (attempt == null) {
			return "redirect:/admin-test-results?internshipId=" + internshipId;
		}

		// 🔥 Find related application
		InternshipApplication app = applicationRepo
				.findByStudent_StudidAndInternship_Id(attempt.getStudent().getStudid(), internshipId).orElse(null);

		if (app == null) {
			return "redirect:/admin-test-results?internshipId=" + internshipId;
		}

		// ✅ AUTO DECISION (NO ADMIN CHOICE)
		if (attempt.isPassed()) {
			app.setStatus(ApplicationStatus.PASSED);
		} else {
			app.setStatus(ApplicationStatus.FAILED);
		}

		applicationRepo.save(app);

		return "redirect:/admin-test-results?internshipId=" + internshipId;
	}

	@PostMapping("/admin/allow-reattempt")
	public String allowReattempt(@RequestParam Integer studentId, @RequestParam Integer internshipId) {

		InternshipApplication app = applicationRepo.findByStudent_StudidAndInternship_Id(studentId, internshipId)
				.orElse(null);

		if (app != null) {
			app.setAllowReattempt(true);
			app.setStatus(ApplicationStatus.FAILED); // keep failed, just allow retry
			applicationRepo.save(app);
		}

		return "redirect:/admin-test-results?internshipId=" + internshipId;
	}
	
	@GetMapping("/admin-evaluate-test")
	public String evaluateTest(@RequestParam Integer attemptId, Model model) {

	    InternshipTestAttempt attempt = attemptRepo.findById(attemptId).orElse(null);

	    if (attempt == null) return "redirect:/admin-test-results";

	    List<TestAnswer> answers = answerRepo.findByAttempt_Id(attemptId);

	    model.addAttribute("attempt", attempt);
	    model.addAttribute("answers", answers);

	    return "admin-evaluate-test";
	}
	
	@PostMapping("/admin-evaluate-answer")
	public String evaluateAnswer(@RequestParam Integer answerId,
	                             @RequestParam Boolean correct) {

	    TestAnswer ans = answerRepo.findById(answerId).orElse(null);

	    if (ans != null) {
	        ans.setIsCorrect(correct);

	        if (correct) {
	            ans.setAwardedMarks(ans.getQuestion().getMarks());
	        } else {
	            ans.setAwardedMarks(0);
	        }

	        answerRepo.save(ans);
	    }

	    return "redirect:/admin-evaluate-test?attemptId=" + ans.getAttempt().getId();
	}
	@PostMapping("/admin-finalize-score")
	public String finalizeScore(@RequestParam Integer attemptId) {

	    InternshipTestAttempt attempt = attemptRepo.findById(attemptId).orElse(null);

	    List<TestAnswer> answers = answerRepo.findByAttempt_Id(attemptId);

	    int total = 0;
	    int score = 0;

	    for (TestAnswer a : answers) {

	        total += a.getQuestion().getMarks();

	        if (a.getAwardedMarks() != null) {
	            score += a.getAwardedMarks();
	        }
	    }

	    double percentage = (score * 100.0) / total;

	    attempt.setScore(score);
	    attempt.setTotalMarks(total);
	    attempt.setPercentage(percentage);

	    InternshipTest test = testRepo.findByInternshipId(attempt.getInternship().getId());

	    boolean passed = percentage >= test.getPassingMarks();
	    attempt.setPassed(passed);

	    attemptRepo.save(attempt);

	    // 🔥 UPDATE APPLICATION
	    InternshipApplication app = applicationRepo
	            .findByStudent_StudidAndInternship_Id(
	                    attempt.getStudent().getStudid(),
	                    attempt.getInternship().getId())
	            .orElse(null);

	    if (app != null) {

	    	if (passed) {
	    	    app.setStatus(ApplicationStatus.PASSED);
	    	    emailService.sendTestPassedMail(
	    	        app.getEmail(),
	    	        app.getFullName(),
	    	        app.getInternship().getTitle()
	    	    );
	    	} else {
	    	    app.setStatus(ApplicationStatus.FAILED);
	    	    emailService.sendTestFailedMail(
	    	        app.getEmail(),
	    	        app.getFullName(),
	    	        app.getInternship().getTitle()
	    	    );
	    	}

	        applicationRepo.save(app);
	    }
	    return "redirect:/admin-test-results?internshipId=" + attempt.getInternship().getId();
	}
}