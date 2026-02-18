package com.example.demo.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.Course;
import com.example.demo.entity.CourseModule;
import com.example.demo.repository.CourseModuleRepository;
import com.example.demo.repository.CourseRepository;
@Controller
public class TeacherModuleController {

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
    private CourseModuleRepository moduleRepo;

    // ADD MODULE
    @PostMapping("/teacher-course/{courseId}/module-add")
    public String addModule(@PathVariable Integer courseId,
                            @RequestParam String title) {

        Integer teacherId = 2;

        Course course = courseRepo.findById(courseId).orElse(null);
        if (course == null ||
            !course.getTeacher().getTeacherId().equals(teacherId)) {
            return "redirect:/teacher-course";
        }

        int pos = moduleRepo.countByCourseCourseId(courseId) + 1;

        CourseModule module = new CourseModule();
        module.setTitle(title);
        module.setCourse(course);
        module.setPosition(pos);

        moduleRepo.save(module);

        return "redirect:/teacher-course/edit/" + courseId;
    }

    // UPDATE MODULE
    @PostMapping("/teacher-course/module-update")
    public String editModule(@RequestParam Integer moduleId,
                             @RequestParam String title) {

        CourseModule module = moduleRepo.findById(moduleId).orElse(null);
        if (module == null) {
            return "redirect:/teacher-course";
        }

        module.setTitle(title);
        moduleRepo.save(module);

        return "redirect:/teacher-course/edit/" +
                module.getCourse().getCourseId();
    }

    // DELETE MODULE
    @PostMapping("/teacher-course/module-delete/{moduleId}")
    public String deleteModule(@PathVariable Integer moduleId) {

        CourseModule module = moduleRepo.findById(moduleId).orElse(null);
        if (module == null) {
            return "redirect:/teacher-course";
        }

        Integer courseId = module.getCourse().getCourseId();
        moduleRepo.delete(module);

        return "redirect:/teacher-course/edit/" + courseId;
    }
}
