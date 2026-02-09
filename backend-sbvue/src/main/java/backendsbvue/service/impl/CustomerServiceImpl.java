package backendsbvue.service.impl;

import backendsbvue.Mapper.CustomerMapper;
import backendsbvue.dto.request.CustomerRequestDTO;
import backendsbvue.dto.response.CustomerResponseDTO;
import backendsbvue.entity.Customer;
import backendsbvue.repository.ICustomerRepository;
import backendsbvue.service.ICustomerService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements ICustomerService {

    private final ICustomerRepository customerRepository;

    public CustomerServiceImpl(ICustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public CustomerResponseDTO createCustomer(CustomerRequestDTO customerRequestDTO) {
        return CustomerMapper.toDTO(customerRepository.save(CustomerMapper.toEntity(customerRequestDTO)));
    }

    @Override
    public CustomerResponseDTO updateCustomer(CustomerRequestDTO customerRequestDTO) {
        if (customerRequestDTO.getId() == null)
            throw new IllegalArgumentException("El ID del cliente no puede ser nulo");

        Customer customer = customerRepository.findById(customerRequestDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + customerRequestDTO.getId()));

        customer.setFirstName(customerRequestDTO.getFirstName());
        customer.setLastName(customerRequestDTO.getLastName());
        customer.setEmail(customerRequestDTO.getEmail());
        Customer updatedCustomer = customerRepository.save(customer);
        return CustomerMapper.toDTO(updatedCustomer);
    }

    @Override
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id))
            throw new EntityNotFoundException("Customer not found with id: " + id);
        customerRepository.deleteById(id);
    }

    @Override
    public List<CustomerResponseDTO> findAll() {
        List<Customer> customers = customerRepository.findAll();

        return customers.stream()
                .map(CustomerMapper::toDTO)
                .collect(Collectors.toList());
    }
}