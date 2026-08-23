package com.rustam.modern_dentistry.controller.warehouse_operations;

import com.rustam.modern_dentistry.dto.request.create.DeletionFromWarehouseCreateRequest;
import com.rustam.modern_dentistry.dto.request.read.DeletionFromWarehouseSearchRequest;
import com.rustam.modern_dentistry.dto.request.update.DeletionFromWarehouseUpdateRequest;
import com.rustam.modern_dentistry.dto.response.read.DeletionFromWarehouseProductResponse;
import com.rustam.modern_dentistry.dto.response.read.DeletionFromWarehouseReadResponse;
import com.rustam.modern_dentistry.dto.response.read.DeletionFromWarehouseResponse;
import com.rustam.modern_dentistry.service.warehouse_operations.DeletionFromWarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/deletion-from-warehouse")
@RequiredArgsConstructor
public class DeletionFromWarehouseController {

    private final DeletionFromWarehouseService deletionFromWarehouseService;

    @PostMapping(path = "/create")
    public ResponseEntity<DeletionFromWarehouseResponse> create(@RequestBody DeletionFromWarehouseCreateRequest deletionFromWarehouseCreateRequest){
        return new ResponseEntity<>(deletionFromWarehouseService.create(deletionFromWarehouseCreateRequest), HttpStatus.CREATED);
    }

    @GetMapping(path = "/read")
    public ResponseEntity<List<DeletionFromWarehouseResponse>> read(){
        return new ResponseEntity<>(deletionFromWarehouseService.read(),HttpStatus.OK);
    }

    @PostMapping(path = "/search")
    public ResponseEntity<List<DeletionFromWarehouseResponse>> search(@RequestBody DeletionFromWarehouseSearchRequest deletionFromWarehouseSearchRequest){
        return new ResponseEntity<>(deletionFromWarehouseService.search(deletionFromWarehouseSearchRequest),HttpStatus.OK);
    }

    @PutMapping(path = {"/update", "/update/{id}"})
    public ResponseEntity<DeletionFromWarehouseReadResponse> update(
            @PathVariable(required = false) Long id,
            @RequestBody DeletionFromWarehouseUpdateRequest deletionFromWarehouseUpdateRequest){
        if (id != null && deletionFromWarehouseUpdateRequest.getDeletionFromWarehouseId() == null) {
            deletionFromWarehouseUpdateRequest.setDeletionFromWarehouseId(id);
        }
        return new ResponseEntity<>(deletionFromWarehouseService.update(deletionFromWarehouseUpdateRequest),HttpStatus.OK);
    }

    @GetMapping(path = "/info/{id}")
    public ResponseEntity<DeletionFromWarehouseReadResponse> info(@PathVariable Long id){
        return new ResponseEntity<>(deletionFromWarehouseService.info(id),HttpStatus.OK);
    }

    @DeleteMapping(path = "/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        deletionFromWarehouseService.delete(id);
        return ResponseEntity.ok().build();
    }

}
