package com.example.demo.Controllers;
import java.io.IOException;
import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.CertificateTemplate;
import com.example.demo.repository.CertificateTemplateRepository;

@Controller
public class AdminDesignController {

    private final CertificateTemplateRepository templateRepo;

    AdminDesignController(CertificateTemplateRepository templateRepo) {
        this.templateRepo = templateRepo;
    }
    private static final String UPLOAD_BASE =
            System.getProperty("user.dir") + java.io.File.separator + "uploads";

    private String saveFile(MultipartFile file, String folderName) throws IOException {

        if (file == null || file.isEmpty()) {
            return null;
        }

        String folderPath = UPLOAD_BASE + java.io.File.separator + folderName;

        java.io.File dir = new java.io.File(folderPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = System.currentTimeMillis()
                + "_" + file.getOriginalFilename();

        java.io.File destination = new java.io.File(dir, fileName);
        file.transferTo(destination);

        // store relative path in DB
        return "/uploads/" + folderName + "/" + fileName;
    }

	@GetMapping("/admin-design")
	public String designTemplate() {
		return "admin-design";
	}
	
	@PostMapping("/certitifcate-template/save")
	public String saveTemplate(@RequestParam String name,
		        @RequestParam MultipartFile backgroundImage,
		        @RequestParam(required = false) MultipartFile signatureImage,
		        @RequestParam String fontFamily,
		        @RequestParam String fontColor,
			Model model)throws IOException {
		
		
		CertificateTemplate template = new CertificateTemplate();
		template.setName(name);
		
		template.setBackgroundImage(
				saveFile(backgroundImage, "certificate-templates"));
		
		 if (!signatureImage.isEmpty()) {
		        template.setSignatureImage(
		            saveFile(signatureImage, "cert-sign")
		        );
		    }

		    template.setFontFamily(fontFamily);
		    template.setFontColor(fontColor);
		    template.setCreatedAt(LocalDate.now());
		    template.setActive(false);
		templateRepo.save(template);
		return "redirect:/admin-design";
	}
	
	@PostMapping("/certificate-template/activate/{id}")
	public String activateTemplate(@PathVariable Integer id) {

	    templateRepo.findByActiveTrue()
	        .ifPresent(t -> {
	            t.setActive(false);
	            templateRepo.save(t);
	        });

	    CertificateTemplate newActive =
	        templateRepo.findById(id).orElseThrow();

	    newActive.setActive(true);
	    templateRepo.save(newActive);

	    return "redirect:/admin-design";
	}
}
