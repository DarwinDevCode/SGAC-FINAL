package org.uteq.sgacfinal.dto.Request;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {
    private String usuario;
    private String password;
}
