package com.example.customer_service.controller;

import com.example.customer_service.dto.household.HouseholdMemberRequest;
import com.example.customer_service.dto.household.HouseholdMemberResponse;
import com.example.customer_service.dto.household.HouseholdRequest;
import com.example.customer_service.dto.household.HouseholdResponse;
import com.example.customer_service.service.HouseholdService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers/me/household")
public class HouseholdController {

    private final HouseholdService householdService;

    public HouseholdController(HouseholdService householdService) {
        this.householdService = householdService;
    }

    @PostMapping
    public ResponseEntity<HouseholdResponse> createHousehold(
            @Valid @RequestBody HouseholdRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(householdService.createHousehold(request));
    }

    @GetMapping
    public ResponseEntity<HouseholdResponse> getMyHousehold() {
        return ResponseEntity.ok(householdService.getMyHousehold());
    }

    @PutMapping
    public ResponseEntity<HouseholdResponse> updateHousehold(
            @Valid @RequestBody HouseholdRequest request) {
        return ResponseEntity.ok(householdService.updateHousehold(request));
    }

    @PostMapping("/members")
    public ResponseEntity<HouseholdMemberResponse> addMember(
            @Valid @RequestBody HouseholdMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(householdService.addMember(request));
    }

    @GetMapping("/members")
    public ResponseEntity<List<HouseholdMemberResponse>> getMembers() {
        return ResponseEntity.ok(householdService.getMembers());
    }

    @PutMapping("/members/{memberId}")
    public ResponseEntity<HouseholdMemberResponse> updateMember(
            @Parameter(
                    name = "memberId",
                    description = "Enter the member UUID",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
            )
            @PathVariable UUID memberId,
            @Valid @RequestBody HouseholdMemberRequest request) {
        return ResponseEntity.ok(householdService.updateMember(memberId, request));
    }

    @DeleteMapping("/members/{memberId}")
    public ResponseEntity<Void> deleteMember(
            @Parameter(
                    name = "memberId",
                    description = "Enter the member UUID",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
            )
            @PathVariable UUID memberId) {
        householdService.deleteMember(memberId);
        return ResponseEntity.noContent().build();
    }
}
