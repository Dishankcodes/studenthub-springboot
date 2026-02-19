package com.example.demo.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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

    // ✅ ADD MODULE
    @PostMapping("/teacher/module/add")
    public String addModule(
            @RequestParam Integer courseId,
            @RequestParam String title
    ) {
        Course course = courseRepo.findById(courseId).orElseThrow();

        CourseModule module = new CourseModule();
        module.setTitle(title);
        module.setCourse(course);

        // ✅ SET POSITION
        int position = course.getModules().size() + 1;
        module.setPosition(position);

        moduleRepo.save(module);

        return "redirect:/teacher-creates-course?courseId=" + courseId;
    }


    // ✅ UPDATE MODULE
    @PostMapping("/teacher/module/update")
    public String updateModule(
            @RequestParam Integer moduleId,
            @RequestParam String title
    ) {
        CourseModule module = moduleRepo.findById(moduleId).orElse(null);
        if (module == null) {
            return "redirect:/teacher-course";
        }

        module.setTitle(title);
        moduleRepo.save(module);

        return "redirect:/teacher-creates-course?courseId="
                + module.getCourse().getCourseId();
    }

    // ✅ DELETE MODULE
    @PostMapping("/teacher/module/delete")
    public String deleteModule(
            @RequestParam Integer moduleId
    ) {
        CourseModule module = moduleRepo.findById(moduleId).orElse(null);
        if (module == null) {
            return "redirect:/teacher-course";
        }

        Integer courseId = module.getCourse().getCourseId();
        moduleRepo.delete(module);

        return "redirect:/teacher-creates-course?courseId=" + courseId;
    }
}
