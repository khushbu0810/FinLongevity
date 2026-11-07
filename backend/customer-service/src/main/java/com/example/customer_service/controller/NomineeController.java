package com.example.customer_service.controller;

import com.example.customer_service.dto.nominee.NomineeRequest;
import com.example.customer_service.dto.nominee.NomineeResponse;
import com.example.customer_service.service.NomineeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers/me/nominees")
public class NomineeController {

    private final NomineeService nomineeService;

    public NomineeController(NomineeService nomineeService) {
        this.nomineeService = nomineeService;
    }

    @PostMapping
    public ResponseEntity<NomineeResponse> createNominee(
            @Valid @RequestBody NomineeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(nomineeService.createNominee(request));
    }

    @GetMapping
    public ResponseEntity<List<NomineeResponse>> getNominees() {
        return ResponseEntity.ok(nomineeService.getNominees());
    }

    @PutMapping("/{id}")
    public ResponseEntity<NomineeResponse> updateNominee(
            @PathVariable("id") UUID id,
            @Valid @RequestBody NomineeRequest request) {
        return ResponseEntity.ok(nomineeService.updateNominee(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNominee(@PathVariable("id") UUID id) {
        nomineeService.deleteNominee(id);
        return ResponseEntity.noContent().build();
    }
}
