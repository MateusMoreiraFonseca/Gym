package com.app.gym.application;

import com.app.gym.domain.ContaUsuario;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServicosAuxiliaresTest {

    @Test
    void deveCriptografarEValidarSenhaDelegandoAoEncoder() {
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        ServicoCriptografiaSenha servico = new ServicoCriptografiaSenha(encoder);
        when(encoder.encode("senha")).thenReturn("hash");
        when(encoder.matches("senha", "hash")).thenReturn(true);

        assertEquals("hash", servico.criptografar("senha"));
        assertTrue(servico.descriptografar("senha", "hash"));
        verify(encoder).encode("senha");
        verify(encoder).matches("senha", "hash");
    }

    @Test
    void deveListarTodosOsUsuariosDoRepositorio() {
        RepositorioContaUsuario repositorio = mock(RepositorioContaUsuario.class);
        ServicoListarUsuarios servico = new ServicoListarUsuarios(repositorio);
        List<ContaUsuario> usuarios = List.of(new ContaUsuario("ana", "hash", false));
        when(repositorio.findAll()).thenReturn(usuarios);

        assertSame(usuarios, servico.listarTodosUsuarios());
    }

    @Test
    void deveCriarAdministradorQuandoEleAindaNaoExiste() {
        RepositorioContaUsuario repositorio = mock(RepositorioContaUsuario.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        ServiçoSementeAdministrador servico = semente(repositorio, encoder);
        when(repositorio.findByUsername("admin")).thenReturn(Optional.empty());
        when(encoder.encode("senha-admin")).thenReturn("hash-admin");

        servico.run(new DefaultApplicationArguments());

        var captor = org.mockito.ArgumentCaptor.forClass(ContaUsuario.class);
        verify(repositorio).save(captor.capture());
        assertEquals("admin", captor.getValue().getUsername());
        assertEquals("hash-admin", captor.getValue().getPassword());
        assertTrue(captor.getValue().isAdmin());
    }

    @Test
    void naoDeveCriarAdministradorQuandoEleJaExiste() {
        RepositorioContaUsuario repositorio = mock(RepositorioContaUsuario.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        ServiçoSementeAdministrador servico = semente(repositorio, encoder);
        when(repositorio.findByUsername("admin")).thenReturn(Optional.of(new ContaUsuario("admin", "hash", true)));

        servico.run(new DefaultApplicationArguments());

        verify(repositorio, never()).save(any());
        verifyNoInteractions(encoder);
    }

    private ServiçoSementeAdministrador semente(RepositorioContaUsuario repositorio, PasswordEncoder encoder) {
        ServiçoSementeAdministrador servico = new ServiçoSementeAdministrador(repositorio, encoder);
        ReflectionTestUtils.setField(servico, "adminUsername", "admin");
        ReflectionTestUtils.setField(servico, "adminPassword", "senha-admin");
        return servico;
    }
}
