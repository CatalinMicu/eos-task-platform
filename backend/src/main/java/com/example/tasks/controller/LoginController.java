package com.example.tasks.controller;

import com.example.tasks.dto.CredentialsDTO;
import com.example.tasks.service.LoginRegisterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jose4j.lang.JoseException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class LoginController {

    private final LoginRegisterService loginRegisterService;

    @PostMapping
    public ResponseEntity<String> login(@Valid @RequestBody CredentialsDTO credentialsDTO) throws JoseException {
        return loginRegisterService.login(credentialsDTO);
    }
}
