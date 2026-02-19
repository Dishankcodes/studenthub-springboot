package com.example.demo.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.Lesson;
import com.example.demo.entity.Quiz;
import com.example.demo.entity.QuizQuestion;
import com.example.demo.repository.LessonRepository;
import com.example.demo.repository.QuizQuestionRepository;
import com.example.demo.repository.QuizRepository;

@Controller
public class QuizController {

    @Autowired
    private QuizRepository quizRepo;

    @Autowired
    private QuizQuestionRepository questionRepo;

    @Autowired
    private LessonRepository lessonRepo;

    /* ===============================
       ADD QUESTION
       =============================== */
    @GetMapping("/teacher/quiz/question/add")
    public String addQuestion(@RequestParam Integer lessonId) {

        Lesson lesson = lessonRepo.findById(lessonId).orElseThrow();

        Quiz quiz = quizRepo.findByLessonLessonId(lessonId);
        if (quiz == null) {
            quiz = new Quiz();
            quiz.setLesson(lesson);
            quiz.setTimeLimit(10);
            quiz = quizRepo.save(quiz);
        }

        QuizQuestion q = new QuizQuestion();
        q.setQuiz(quiz);
        q.setQuestionText("New Question");
        q.setOptionA("Option A");
        q.setOptionB("Option B");
        q.setOptionC("Option C");
        q.setOptionD("Option D");
        q.setCorrectOption("A");
        q.setMarks(1);
        q.setPosition((int) questionRepo.countByQuizQuizId(quiz.getQuizId()) + 1);

        questionRepo.save(q);

        return "redirect:/teacher-creates-course?courseId="
                + lesson.getModule().getCourse().getCourseId();
    }

    /* ===============================
       SAVE QUIZ (UPDATE ALL QUESTIONS)
       =============================== */
    @PostMapping("/teacher/quiz/question/update")
    public String updateQuiz(
            @RequestParam Integer lessonId,
            @RequestParam List<Integer> questionIds,
            @RequestParam List<String> questionTexts,
            @RequestParam List<String> optionAs,
            @RequestParam List<String> optionBs,
            @RequestParam List<String> optionCs,
            @RequestParam List<String> optionDs,
            @RequestParam List<String> correctOptions
    ) {

        for (int i = 0; i < questionIds.size(); i++) {
            QuizQuestion q = questionRepo.findById(questionIds.get(i)).orElseThrow();
            q.setQuestionText(questionTexts.get(i));
            q.setOptionA(optionAs.get(i));
            q.setOptionB(optionBs.get(i));
            q.setOptionC(optionCs.get(i));
            q.setOptionD(optionDs.get(i));
            q.setCorrectOption(correctOptions.get(i));
            questionRepo.save(q);
        }

        Lesson lesson = lessonRepo.findById(lessonId).orElseThrow();

        return "redirect:/teacher-creates-course?courseId="
                + lesson.getModule().getCourse().getCourseId();
    }

    /* ===============================
       DELETE QUESTION
       =============================== */
    @GetMapping("/teacher/quiz/question/delete")
    public String deleteQuestion(@RequestParam Integer questionId) {

        QuizQuestion q = questionRepo.findById(questionId).orElseThrow();
        Integer courseId = q.getQuiz().getLesson().getModule().getCourse().getCourseId();

        questionRepo.delete(q);

        return "redirect:/teacher-creates-course?courseId=" + courseId;
    }
}
