package org.uteq.sgacfinal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Data
@AllArgsConstructor
public class LoginResultadoDTO {
    private String username;
    private Collection<? extends GrantedAuthority> roles;
}
