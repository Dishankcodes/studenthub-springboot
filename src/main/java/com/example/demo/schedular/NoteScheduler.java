package com.example.demo.schedular;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.demo.repository.TeacherNotesRepository;

@Component
public class NoteScheduler {

    @Autowired
    private TeacherNotesRepository noteRepo;

    // 🔥 Runs every 1 minute
    @Scheduled(fixedRate = 60000)
    public void autoApproveNotes() {

        LocalDateTime time = LocalDateTime.now().minusMinutes(5);

        // ✅ STEP 1: Check if any eligible notes exist
        if (!noteRepo.existsPendingBefore(time)) {
            return; // 🚀 STOP → no query, no logs
        }

        // ✅ STEP 2: Bulk update (FASTEST WAY)
        int updated = noteRepo.autoApproveBulk(time, LocalDateTime.now());

        // ✅ STEP 3: Log only if something changed
        if (updated > 0) {
            System.out.println("✅ Auto approved notes: " + updated);
        }
    }
}