package org.uteq.sgacfinal.dto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    private String usuario;
    private String password;
}
