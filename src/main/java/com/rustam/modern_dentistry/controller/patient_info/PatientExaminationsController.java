package com.rustam.modern_dentistry.controller.patient_info;

import com.rustam.modern_dentistry.dto.request.create.PatientExaminationsCreateRequest;
import com.rustam.modern_dentistry.dto.request.create.PatientExaminationsUpdateRequest;
import com.rustam.modern_dentistry.dto.request.read.RequestToSeeTheExaminations;
import com.rustam.modern_dentistry.dto.response.create.PatientExaminationsCreateResponse;
import com.rustam.modern_dentistry.dto.response.read.ExaminationResponse;
import com.rustam.modern_dentistry.dto.response.read.PatientExaminationsResponse;
import com.rustam.modern_dentistry.dto.response.read.TeethResponse;
import com.rustam.modern_dentistry.service.patient_info.PatientExaminationsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/patient-examinations")
@RequiredArgsConstructor
public class PatientExaminationsController {

    private final PatientExaminationsService patientExaminationsService;

    @PostMapping(path = "/create")
    public ResponseEntity<PatientExaminationsCreateResponse> create(@RequestBody PatientExaminationsCreateRequest request) {
        return new ResponseEntity<>(patientExaminationsService.create(request), HttpStatus.CREATED);
    }

    @GetMapping(path = "/patient/{patientId}")
    public ResponseEntity<List<PatientExaminationsResponse>> readByPatientId(@PathVariable Long patientId) {
        return ResponseEntity.ok(patientExaminationsService.readByPatientId(patientId));
    }

    @PostMapping(path = "/see-historical-election-dental-examinations")
    public ResponseEntity<List<PatientExaminationsResponse>> seeHistoricalElectionDentalExaminations(@RequestBody RequestToSeeTheExaminations requestToSeeTheExaminations) {
        return new ResponseEntity<>(patientExaminationsService.seeHistoricalElectionDentalExaminations(requestToSeeTheExaminations), HttpStatus.OK);
    }

    @GetMapping(path = "/read-examinations")
    public ResponseEntity<List<ExaminationResponse>> readExaminations() {
        return new ResponseEntity<>(patientExaminationsService.readExaminations(), HttpStatus.OK);
    }

    @PutMapping(path = "/update")
    public ResponseEntity<PatientExaminationsCreateResponse> update(@RequestBody PatientExaminationsUpdateRequest patientExaminationsUpdateRequest) {
        return new ResponseEntity<>(patientExaminationsService.update(patientExaminationsUpdateRequest), HttpStatus.OK);
    }

//    @GetMapping(path = "/read")
//    public ResponseEntity<List<PatientExaminationsResponse>> read() {
//        return new ResponseEntity<>(patientExaminationsService.read(), HttpStatus.OK);
//    }

    @DeleteMapping(path = "/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        patientExaminationsService.delete(id);
        return ResponseEntity.ok().build();
    }
}
