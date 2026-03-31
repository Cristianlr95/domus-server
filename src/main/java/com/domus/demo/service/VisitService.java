package com.domus.demo.service;

import com.domus.demo.entity.Visit;
import com.domus.demo.entity.VisitStatus;
import com.domus.demo.repository.VisitRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VisitService {

    private final VisitRepository repository;

    public VisitService(VisitRepository repository) {
        this.repository = repository;
    }

    public Visit create(Visit visit) {
        visit.setEntryTime(LocalDateTime.now());
        visit.setStatus(VisitStatus.PENDING);
        return repository.save(visit);
    }

    public List<Visit> findAll() {
        return repository.findAll();
    }
}