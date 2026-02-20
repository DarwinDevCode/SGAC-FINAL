package org.uteq.sgacfinal.controller;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/whoami")
    public String whoAmI() {
        return (String) entityManager.createNativeQuery("SELECT current_user").getSingleResult();
    }
}