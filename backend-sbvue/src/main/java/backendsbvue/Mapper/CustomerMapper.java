package backendsbvue.Mapper;

import backendsbvue.dto.request.CustomerRequestDTO;
import backendsbvue.dto.response.CustomerResponseDTO;
import backendsbvue.entity.Customer;

public class CustomerMapper {

    public static Customer toEntity(CustomerRequestDTO dto) {
        return Customer.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .build();
    }

    public static CustomerResponseDTO toDTO(Customer customer) {
        return CustomerResponseDTO.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .build();
    }
}
