package com.example.demo.Services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.Entities.JournalArrosage;
import com.example.demo.Repositories.JournalRepo;

@Service
public class JournalService {

    @Autowired
    private JournalRepo journalRepository;

    public List<JournalArrosage> getAllJournals() {
        return journalRepository.findAll();
    }

    public JournalArrosage getJournalById(Long id) {
        return journalRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Journal non trouvé avec l'id: " + id));
    }

    public List<JournalArrosage> getJournalsByProgramme(Long programmeId) {
        return journalRepository.findByProgrammeId(programmeId);
    }

    public List<JournalArrosage> getJournalsByPeriod(LocalDateTime start, LocalDateTime end) {
        return journalRepository.findByDateExecutionBetween(start, end);
    }

    public JournalArrosage saveJournal(JournalArrosage journal) {
        return journalRepository.save(journal);
    }
}