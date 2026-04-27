package com.example.MobileAppBackend.controller;


import com.example.MobileAppBackend.dto.authentication.client.ClientRegisterRequestDto;
import com.example.MobileAppBackend.dto.authentication.client.ClientRegisterResponseDto;
import com.example.MobileAppBackend.dto.authentication.user.LoginRequest;
import com.example.MobileAppBackend.dto.authentication.user.RegisterRequest;
import com.example.MobileAppBackend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest registerRequest) {
        this.authService.register(registerRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/client/register")
    public ResponseEntity<ClientRegisterResponseDto> clientRegister(@RequestBody @Valid ClientRegisterRequestDto clientRegisterRequestDto) {
        ClientRegisterResponseDto responseDto = this.authService.clientRegister(clientRegisterRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }
}
