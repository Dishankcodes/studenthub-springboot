package com.example.demo.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
            @RequestParam(required = false) LessonType type,
            RedirectAttributes ra
    ) {
        CourseModule module = moduleRepo.findById(moduleId).orElseThrow();

        Lesson lesson = new Lesson();
        lesson.setModule(module);
        lesson.setTitle(title != null ? title : "");
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
        
        ra.addFlashAttribute("success", "Lesson added");
        
        return "redirect:/teacher-creates-course?courseId="
        + module.getCourse().getCourseId()
        + "&openModule=" + module.getModuleId()
        + "#module-" + module.getModuleId();    }



    /* ===============================
       UPDATE LESSON TITLE
       =============================== */
    @PostMapping("/teacher/lesson/update")
    public String updateLesson(
            @RequestParam Integer lessonId,
            @RequestParam String title,
            @RequestParam(required = false) LessonType type,
            RedirectAttributes ra
    ) {
        Lesson lesson = lessonRepo.findById(lessonId).orElseThrow();

        lesson.setTitle(title);

        if (type != null) {
            lesson.setType(type);
        }

      

        lessonRepo.save(lesson);

        ra.addFlashAttribute("success", "Lesson updated");
        
        return "redirect:/teacher-creates-course?courseId="
        + lesson.getModule().getCourse().getCourseId()
        + "&openModule=" + lesson.getModule().getModuleId()
        + "&openLesson=" + lesson.getLessonId()
        + "#lesson-" + lesson.getLessonId();
        
    }


    /* ===============================
       DELETE LESSON
       =============================== */
    @PostMapping("/teacher/lesson/delete")
    public String deleteLesson(@RequestParam Integer lessonId,
    		RedirectAttributes ra) {

        Lesson lesson = lessonRepo.findById(lessonId).orElseThrow();

        Integer courseId = lesson.getModule().getCourse().getCourseId();
        Integer moduleId = lesson.getModule().getModuleId(); // 👈 capture BEFORE delete

        lessonRepo.delete(lesson);

        
        ra.addFlashAttribute("success", "Lesson deleted");
        
        
        return "redirect:/teacher-creates-course?courseId="
                + courseId
                + "&openModule=" + moduleId
                + "#module-" + moduleId;
    }
}
