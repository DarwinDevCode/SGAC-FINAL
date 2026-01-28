package com.sgac.controller;

import com.sgac.dto.DecanoDTO;
import com.sgac.dto.DecanoRequest;
import com.sgac.service.DecanoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/decanos")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class DecanoController {

    private final DecanoService decanoService;

    @GetMapping
    public ResponseEntity<List<DecanoDTO>> findAll() {
        return ResponseEntity.ok(decanoService.findAll());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<DecanoDTO>> findByActivo() {
        return ResponseEntity.ok(decanoService.findByActivo(true));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DecanoDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(decanoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<DecanoDTO> create(@Valid @RequestBody DecanoRequest request) {
        return new ResponseEntity<>(decanoService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DecanoDTO> update(@PathVariable Integer id, @Valid @RequestBody DecanoRequest request) {
        return ResponseEntity.ok(decanoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        decanoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
