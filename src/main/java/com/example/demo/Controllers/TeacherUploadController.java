package com.example.demo.Controllers;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.Lesson;
import com.example.demo.repository.LessonRepository;

@Controller
public class TeacherUploadController {

	@Autowired
	private LessonRepository lessonRepo;
	
	@PostMapping("/teacher/lesson/video")
	public String uploadLessonVideo(
	        @RequestParam Integer lessonId,
	        @RequestParam("videoFile") MultipartFile videoFile,
	        @RequestParam(required = false) boolean freePreview
	) throws IOException {

	    Lesson lesson = lessonRepo.findById(lessonId).orElseThrow();

	    // ❌ No file selected
	    if (videoFile == null || videoFile.isEmpty()) {
	        return "redirect:/teacher-creates-course?courseId="
	                + lesson.getModule().getCourse().getCourseId()
	                + "&openLesson=" + lessonId
	                + "&msg=video_error";
	    }

	    // ✅ check if replacing
	    boolean alreadyExists = lesson.getContentUrl() != null;

	    String basePath = System.getProperty("user.dir")
	            + "/uploads/lesson-videos/"
	            + lessonId;

	    File dir = new File(basePath);
	    if (!dir.exists()) dir.mkdirs();

	    String fileName = System.currentTimeMillis()
	            + "_" + videoFile.getOriginalFilename();

	    File destination = new File(dir, fileName);
	    videoFile.transferTo(destination);

	    lesson.setContentUrl(
	        "/uploads/lesson-videos/" + lessonId + "/" + fileName
	    );
	    lesson.setFreePreview(freePreview);

	    lessonRepo.save(lesson);

	    return "redirect:/teacher-creates-course?courseId="
	    + lesson.getModule().getCourse().getCourseId()
	    + "&openModule=" + lesson.getModule().getModuleId()
	    + "&openLesson=" + lessonId
	    + "&msg=video_uploaded"
	    + "#module-" + lesson.getModule().getModuleId();
	    }
	
	@PostMapping("/teacher/lesson/notes")
	public String uploadLessonNotes(
	        @RequestParam Integer lessonId,
	        @RequestParam MultipartFile notesFile,
	        @RequestParam(required = false, defaultValue = "false") boolean freePreview
	) throws IOException {

	    Lesson lesson = lessonRepo.findById(lessonId).orElseThrow();

	    boolean alreadyExists = lesson.getContentUrl() != null;

	    String basePath = System.getProperty("user.dir")
	            + "/uploads/lesson-notes/"
	            + lessonId;

	    File dir = new File(basePath);
	    if (!dir.exists()) dir.mkdirs();

	    String fileName = System.currentTimeMillis()
	            + "_" + notesFile.getOriginalFilename();

	    File destination = new File(dir, fileName);
	    notesFile.transferTo(destination);

	    lesson.setContentUrl(
	        "/uploads/lesson-notes/" + lessonId + "/" + fileName
	    );
	    lesson.setFreePreview(freePreview);

	    lessonRepo.save(lesson);

	    return "redirect:/teacher-creates-course?courseId="
	    + lesson.getModule().getCourse().getCourseId()
	    + "&openModule=" + lesson.getModule().getModuleId()
	    + "&openLesson=" + lessonId
	    + "&msg=" + (alreadyExists ? "notes_updated" : "notes_uploaded")
	    + "#module-" + lesson.getModule().getModuleId();
	}
}
