package com.domus.demo.controller;

import com.domus.demo.entity.Visit;
import com.domus.demo.service.VisitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visits")
public class VisitController {

    private final VisitService service;

    public VisitController(VisitService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Visit> create(@RequestBody Visit visit) {
        return ResponseEntity.ok(service.create(visit));
    }

    @GetMapping
    public ResponseEntity<List<Visit>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }
}