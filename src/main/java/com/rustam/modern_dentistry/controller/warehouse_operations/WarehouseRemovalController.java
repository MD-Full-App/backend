package com.rustam.modern_dentistry.controller.warehouse_operations;

import com.rustam.modern_dentistry.dao.entity.warehouse_operations.WarehouseRemoval;
import com.rustam.modern_dentistry.dao.entity.warehouse_operations.WarehouseRemovalProduct;
import com.rustam.modern_dentistry.dto.request.create.WarehouseRemovalCreateRequest;
import com.rustam.modern_dentistry.dto.request.read.WarehouseRemovalProductSearchRequest;
import com.rustam.modern_dentistry.dto.response.create.WarehouseRemovalCreateResponse;
import com.rustam.modern_dentistry.dto.response.read.WarehouseRemovalReadResponse;
import com.rustam.modern_dentistry.service.warehouse_operations.WarehouseRemovalService;
import jdk.dynalink.linker.LinkerServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/warehouse-removal")
@RequiredArgsConstructor
public class WarehouseRemovalController {

    private final WarehouseRemovalService warehouseRemovalService;

    @GetMapping(path = "/read")
    public ResponseEntity<List<WarehouseRemovalReadResponse>> read(){
        return new ResponseEntity<>(warehouseRemovalService.read(),HttpStatus.OK);
    }

    @PostMapping(path = "/search")
    public ResponseEntity<List<WarehouseRemovalReadResponse>> search(@RequestBody WarehouseRemovalProductSearchRequest warehouseRemovalSearchRequest){
        return new ResponseEntity<>(warehouseRemovalService.search(warehouseRemovalSearchRequest),HttpStatus.OK);
    }

    @GetMapping(path = "/info/{id}")
    public ResponseEntity<WarehouseRemovalReadResponse> info(@PathVariable Long id){
        return new ResponseEntity<>(warehouseRemovalService.info(id),HttpStatus.OK);
    }

    @PutMapping(path = "/update/{id}")
    public ResponseEntity<WarehouseRemovalReadResponse> update(@PathVariable Long id, @RequestBody com.rustam.modern_dentistry.dto.request.update.WarehouseRemovalProductUpdateRequest request){
        return new ResponseEntity<>(warehouseRemovalService.update(id, request), HttpStatus.OK);
    }

    @DeleteMapping(path = "/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        warehouseRemovalService.delete(id);
        return ResponseEntity.ok().build();
    }
}
