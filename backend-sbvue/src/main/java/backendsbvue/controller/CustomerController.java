package backendsbvue.controller;

import backendsbvue.dto.request.CustomerRequestDTO;
import backendsbvue.dto.response.CustomerResponseDTO;
import backendsbvue.service.ICustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "http://localhost:5173")
public class CustomerController {

    private final ICustomerService customerService;

    public CustomerController(ICustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping()
    public ResponseEntity<CustomerResponseDTO> createCustomer(@RequestBody CustomerRequestDTO customerRequestDTO) {
        CustomerResponseDTO createdCustomer = customerService.createCustomer(customerRequestDTO);
        return new ResponseEntity<>(createdCustomer, HttpStatus.CREATED);
    }

    @PutMapping()
    public ResponseEntity<CustomerResponseDTO> updateCustomer(@RequestBody CustomerRequestDTO customerRequestDTO) {
        CustomerResponseDTO updatedCustomer = customerService.updateCustomer(customerRequestDTO);
        return ResponseEntity.ok(updatedCustomer);
    }

    @GetMapping()
    public ResponseEntity<List<CustomerResponseDTO>> getAllCustomers() {
        return ResponseEntity.ok(customerService.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}