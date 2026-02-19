package com.example.demo.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.CourseModule;
import com.example.demo.entity.Lesson;
import com.example.demo.entity.Quiz;
import com.example.demo.enums.LessonType;
import com.example.demo.repository.CourseModuleRepository;
import com.example.demo.repository.LessonRepository;
import com.example.demo.repository.QuizRepository;

@Controller
public class LessonController {

    @Autowired
    private LessonRepository lessonRepo;

    @Autowired
    private CourseModuleRepository moduleRepo;
    
    @Autowired
    private QuizRepository quizRepo;

    /* ===============================
       ADD LESSON
       =============================== */
    @PostMapping("/teacher/lesson/add")
    public String addLesson(
            @RequestParam Integer moduleId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) LessonType type
    ) {
        CourseModule module = moduleRepo.findById(moduleId).orElseThrow();

        Lesson lesson = new Lesson();
        lesson.setModule(module);
        lesson.setTitle(title != null ? title : "New Lesson");
        lesson.setType(type != null ? type : LessonType.VIDEO);
        lesson.setFreePreview(false);

        int position = (int) lessonRepo.countByModuleModuleId(moduleId) + 1;
        lesson.setPosition(position);

        lessonRepo.save(lesson);

        // ✅ CREATE QUIZ IF TYPE = QUIZ
        if (lesson.getType() == LessonType.QUIZ) {
            Quiz quiz = new Quiz();
            quiz.setLesson(lesson);
            quiz.setTimeLimit(10); // default
            quizRepo.save(quiz);
        }

        return "redirect:/teacher-creates-course?courseId="
                + module.getCourse().getCourseId();
    }



    /* ===============================
       UPDATE LESSON TITLE
       =============================== */
    @PostMapping("/teacher/lesson/update")
    public String updateLesson(
            @RequestParam Integer lessonId,
            @RequestParam String title,
            @RequestParam(required = false) LessonType type,
            @RequestParam(required = false) Boolean freePreview
    ) {
        Lesson lesson = lessonRepo.findById(lessonId).orElseThrow();

        lesson.setTitle(title);

        if (type != null) {
            lesson.setType(type);
        }

        lesson.setFreePreview(freePreview != null);

        lessonRepo.save(lesson);

        return "redirect:/teacher-creates-course?courseId="
                + lesson.getModule().getCourse().getCourseId();
    }


    /* ===============================
       DELETE LESSON
       =============================== */
    @PostMapping("/teacher/lesson/delete")
    public String deleteLesson(@RequestParam Integer lessonId) {

        Lesson lesson = lessonRepo.findById(lessonId).orElseThrow();
        Integer courseId = lesson.getModule().getCourse().getCourseId();

        lessonRepo.delete(lesson);

        return "redirect:/teacher-creates-course?courseId=" + courseId;
    }
}
