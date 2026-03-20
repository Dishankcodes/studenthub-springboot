package com.example.demo.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.InternshipTest;
import com.example.demo.entity.Internships;
import com.example.demo.entity.QuizQuestion;
import com.example.demo.enums.QuestionFormat;
import com.example.demo.enums.QuizQuestionType;
import com.example.demo.repository.InternshipRepository;
import com.example.demo.repository.InternshipTestRepository;
import com.example.demo.repository.QuizQuestionRepository;

@Controller
public class AdminInternshipTestController {

    @Autowired
    private InternshipRepository internshipRepo;

    @Autowired
    private InternshipTestRepository testRepo;

    @Autowired
    private QuizQuestionRepository questionRepo;

    @GetMapping("/admin/internship/test")
    public String openTest(
            @RequestParam Integer internshipId,
            Model model) {

        Internships internship = internshipRepo.findById(internshipId).orElse(null);

        if (internship == null) {
            return "redirect:/manage-internships";
        }

        InternshipTest test = testRepo.findByInternshipId(internshipId);

        if (test == null) {
            test = new InternshipTest();
            test.setInternship(internship);
            test.setDurationMinutes(10);
            test.setPassingMarks(40);
            test.setTotalQuestionsToShow(5);
            testRepo.save(test);
        }

        List<QuizQuestion> questions =
                questionRepo.findByInternshipId(internshipId);

        model.addAttribute("test", test);
        model.addAttribute("internship", internship);
        model.addAttribute("questions", questions);

        return "admin-internship-test";
    }

    @PostMapping("/admin/internship/test/add-question")
    public String addQuestion(
            @RequestParam Integer internshipId,
            @RequestParam String questionText,
            @RequestParam String format,
            @RequestParam(required = false) String optionA,
            @RequestParam(required = false) String optionB,
            @RequestParam(required = false) String optionC,
            @RequestParam(required = false) String optionD,
            @RequestParam(required = false) String correctOption,
            RedirectAttributes ra) {

        Internships internship =
                internshipRepo.findById(internshipId).orElse(null);

        if (internship == null) {
            return "redirect:/manage-internships";
        }

        if (questionText == null || questionText.trim().isEmpty()) {
            ra.addFlashAttribute("error", "Question cannot be empty ❌");
            return "redirect:/admin/internship/test?internshipId=" + internshipId;
        }

        QuizQuestion q = new QuizQuestion();

        q.setInternship(internship);
        q.setQuestionText(questionText);
        q.setMarks(1);
        q.setType(QuizQuestionType.INTERNSHIP);

        int position =
                questionRepo.findByInternshipId(internshipId).size() + 1;
        q.setPosition(position);
        q.setQuiz(null);

        switch (format) {

            case "MCQ":
                if (correctOption == null || correctOption.isEmpty()) {
                    ra.addFlashAttribute("error", "Select correct option ❌");
                    return "redirect:/admin/internship/test?internshipId=" + internshipId;
                }

                q.setQuestionFormat(QuestionFormat.MCQ);
                q.setOptionA(optionA);
                q.setOptionB(optionB);
                q.setOptionC(optionC);
                q.setOptionD(optionD);
                q.setCorrectOption(correctOption);
                break;

            case "TEXT":
                q.setQuestionFormat(QuestionFormat.TEXT);
                q.setOptionA(null);
                q.setOptionB(null);
                q.setOptionC(null);
                q.setOptionD(null);
                q.setCorrectOption(null);
                break;

            case "CODE":
                q.setQuestionFormat(QuestionFormat.CODE);
                q.setOptionA(null);
                q.setOptionB(null);
                q.setOptionC(null);
                q.setOptionD(null);
                q.setCorrectOption(null);
                break;
        }

        questionRepo.save(q);

        ra.addFlashAttribute("msg", "Question Added ✅");

        return "redirect:/admin/internship/test?internshipId=" + internshipId;
    }

    
    @GetMapping("/admin/internship/test/delete")
    public String deleteQuestion(
            @RequestParam Integer questionId,
            @RequestParam Integer internshipId,
            RedirectAttributes ra) {

        questionRepo.deleteById(questionId);

        ra.addFlashAttribute("msg", "Question Deleted 🗑️");

        return "redirect:/admin/internship/test?internshipId=" + internshipId;
    }

    
    @PostMapping("/admin/internship/test/update")
    public String updateTest(
            @RequestParam Integer internshipId,
            @RequestParam int duration,
            @RequestParam int passingMarks,
            @RequestParam int totalQuestions,
            RedirectAttributes ra) {

        InternshipTest test = testRepo.findByInternshipId(internshipId);

        test.setDurationMinutes(duration);
        test.setPassingMarks(passingMarks);
        test.setTotalQuestionsToShow(totalQuestions);

        testRepo.save(test);

        ra.addFlashAttribute("msg", "Test Updated ✅");

        return "redirect:/admin/internship/test?internshipId=" + internshipId;
    }
}