package com.rustam.modern_dentistry.controller.settings.teeth;

import com.rustam.modern_dentistry.dto.request.create.CreateTeethRequest;
import com.rustam.modern_dentistry.dto.request.read.TeethRequest;
import com.rustam.modern_dentistry.dto.request.update.UpdateTeethRequest;
import com.rustam.modern_dentistry.dto.response.read.ExaminationResponse;
import com.rustam.modern_dentistry.dto.response.read.TeethResponse;
import com.rustam.modern_dentistry.dto.response.update.TeethUpdateResponse;
import com.rustam.modern_dentistry.service.settings.teeth.TeethService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/teeth")
@RequiredArgsConstructor
public class TeethController {

    private final TeethService teethService;

    @PostMapping(path = "/create")
    public ResponseEntity<TeethResponse> create(@Valid @RequestBody CreateTeethRequest createTeethRequest){
        return new ResponseEntity<>(teethService.create(createTeethRequest), HttpStatus.CREATED);
    }

    @GetMapping(path = "/read")
    public ResponseEntity<List<TeethResponse>> read(){
        return new ResponseEntity<>(teethService.read(), HttpStatus.OK);
    }

    @GetMapping(path = "/read-all-by-tooth-no/{toothNo}")
    public ResponseEntity<List<ExaminationResponse>> readAllByToothNo(@PathVariable Long toothNo){
        return new ResponseEntity<>(teethService.readAllByToothNo(toothNo),HttpStatus.OK);
    }

    @PostMapping(path = "/search")
    public ResponseEntity<List<TeethResponse>> search(@RequestBody TeethRequest teethRequest){
        return new ResponseEntity<>(teethService.search(teethRequest),HttpStatus.OK);
    }

    @PutMapping(path = "/update")
    public ResponseEntity<TeethUpdateResponse> update(@Valid @RequestBody UpdateTeethRequest updateTeethRequest){
        return new ResponseEntity<>(teethService.update(updateTeethRequest),HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        teethService.delete(id);
        return ResponseEntity.ok().build();
    }

}
