package backendsbvue.service;

import backendsbvue.dto.request.CustomerRequestDTO;
import backendsbvue.dto.response.CustomerResponseDTO;
import backendsbvue.entity.Customer;

import java.util.List;

public interface ICustomerService {
    CustomerResponseDTO createCustomer(CustomerRequestDTO customerRequestDTO);
    CustomerResponseDTO updateCustomer(CustomerRequestDTO customerRequestDTO);
    void deleteCustomer(Long id);
    List<CustomerResponseDTO> findAll();
}
