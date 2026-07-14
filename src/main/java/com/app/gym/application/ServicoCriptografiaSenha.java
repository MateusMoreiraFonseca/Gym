package com.app.gym.application;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ServicoCriptografiaSenha {

    private final PasswordEncoder passwordEncoder;

    public ServicoCriptografiaSenha(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Criptografa (hash) a senha em formato seguro.
     */
    public String criptografar(String senhaEmTextoClaro) {
        return passwordEncoder.encode(senhaEmTextoClaro);
    }

    /**
     * "Descriptografa" na prática: valida se a senha em texto claro corresponde ao hash armazenado.
     *
     * BCrypt não é reversível, então a verificação é feita por matches.
     */
    public boolean descriptografar(String senhaEmTextoClaro, String hashArmazenado) {
        return passwordEncoder.matches(senhaEmTextoClaro, hashArmazenado);
    }
}

