package org.uteq.sgacfinal.dto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {
    private String usuario;
    private String password;
}
