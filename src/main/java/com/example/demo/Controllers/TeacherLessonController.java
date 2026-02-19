package com.example.demo.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.CourseModule;
import com.example.demo.entity.Lesson;
import com.example.demo.enums.LessonType;
import com.example.demo.repository.CourseModuleRepository;
import com.example.demo.repository.LessonRepository;

@Controller
public class TeacherLessonController {

    @Autowired
    private CourseModuleRepository moduleRepo;

    @Autowired
    private LessonRepository lessonRepo;

    // ✅ ADD LESSON
    @PostMapping("/teacher/lesson/add")
    public String addLesson(
            @RequestParam Integer moduleId,
            @RequestParam String title,
            @RequestParam LessonType type
    ) {
        CourseModule module = moduleRepo.findById(moduleId).orElse(null);
        if (module == null) {
            return "redirect:/teacher-course";
        }

        int position = lessonRepo.countByModuleModuleId(moduleId) + 1;

        Lesson lesson = new Lesson();
        lesson.setTitle(title);
        lesson.setType(type);
        lesson.setPosition(position);
        lesson.setModule(module);

        lessonRepo.save(lesson);

        return "redirect:/teacher-creates-course?courseId="
                + module.getCourse().getCourseId();
    }

    // ✅ UPDATE LESSON TITLE
    @PostMapping("/teacher/lesson/update")
    public String updateLesson(
            @RequestParam Integer lessonId,
            @RequestParam String title
    ) {
        Lesson lesson = lessonRepo.findById(lessonId).orElse(null);
        if (lesson == null) {
            return "redirect:/teacher-course";
        }

        lesson.setTitle(title);
        lessonRepo.save(lesson);

        return "redirect:/teacher-creates-course?courseId="
                + lesson.getModule().getCourse().getCourseId();
    }

    // ✅ DELETE LESSON
    @PostMapping("/teacher/lesson/delete")
    public String deleteLesson(
            @RequestParam Integer lessonId
    ) {
        Lesson lesson = lessonRepo.findById(lessonId).orElse(null);
        if (lesson == null) {
            return "redirect:/teacher-course";
        }

        Integer courseId = lesson.getModule().getCourse().getCourseId();
        lessonRepo.delete(lesson);

        return "redirect:/teacher-creates-course?courseId=" + courseId;
    }
}
