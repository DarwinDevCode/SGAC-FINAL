package backendsbvue.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequestDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
}
