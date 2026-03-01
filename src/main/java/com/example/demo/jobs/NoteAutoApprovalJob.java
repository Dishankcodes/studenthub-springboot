package com.example.demo.jobs;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.demo.entity.TeacherNotes;
import com.example.demo.enums.NoteStatus;
import com.example.demo.repository.TeacherNotesRepository;

@Component
public class NoteAutoApprovalJob {

    @Autowired
    private TeacherNotesRepository teacherNoteRepo;

    // runs every 1 minute
    @Scheduled(fixedRate = 60000)
    public void autoApproveNotes() {

        LocalDateTime fiveMinutesAgo =
                LocalDateTime.now().minusMinutes(5);

        List<TeacherNotes> pendingNotes =
                teacherNoteRepo.findPendingBefore(fiveMinutesAgo);

        for (TeacherNotes note : pendingNotes) {
            note.setStatus(NoteStatus.APPROVED);
            note.setApproved(true);
            note.setApprovedAt(LocalDateTime.now());
        }

        teacherNoteRepo.saveAll(pendingNotes);
    }
}