package com.app.gym.infrastructure;

import com.app.gym.application.RepositorioContaUsuario;
import com.app.gym.application.ServicoCriptografiaSenha;
import com.app.gym.application.ServicoListarUsuarios;
import com.app.gym.domain.ContaUsuario;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.ExtendedModelMap;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ControladoresPerfilEAdminTest {

    @Test
    void deveExibirPerfilDoUsuarioAutenticado() {
        RepositorioContaUsuario repositorio = mock(RepositorioContaUsuario.class);
        ControladorPerfil controlador = new ControladorPerfil(repositorio, mock(ServicoCriptografiaSenha.class));
        when(repositorio.findByUsername("ana")).thenReturn(Optional.of(new ContaUsuario("ana", "hash", false)));
        ExtendedModelMap modelo = new ExtendedModelMap();

        assertEquals("editar-perfil", controlador.paginaEditarPerfil(autenticacao("ana", "ROLE_USER"), modelo));
        assertEquals("ana", modelo.getAttribute("username"));
        assertInstanceOf(ControladorPerfil.FormularioEditarPerfil.class, modelo.getAttribute("formulario"));
    }

    @Test
    void deveRejeitarPerfilQuandoUsuarioNaoExiste() {
        RepositorioContaUsuario repositorio = mock(RepositorioContaUsuario.class);
        when(repositorio.findByUsername("ausente")).thenReturn(Optional.empty());
        ControladorPerfil controlador = new ControladorPerfil(repositorio, mock(ServicoCriptografiaSenha.class));

        assertThrows(IllegalStateException.class, () -> controlador.paginaEditarPerfil(autenticacao("ausente", "ROLE_USER"), new ExtendedModelMap()));
    }

    @Test
    void deveValidarSenhasDoPerfilAntesDePersistir() {
        RepositorioContaUsuario repositorio = mock(RepositorioContaUsuario.class);
        ServicoCriptografiaSenha criptografia = mock(ServicoCriptografiaSenha.class);
        ContaUsuario conta = contaComId(7L, "ana", "hash", false);
        when(repositorio.findByUsername("ana")).thenReturn(Optional.of(conta));
        ControladorPerfil controlador = new ControladorPerfil(repositorio, criptografia);
        ControladorPerfil.FormularioEditarPerfil formulario = formulario("nova", "outra");
        ExtendedModelMap modelo = new ExtendedModelMap();

        assertEquals("editar-perfil", controlador.editarPerfil(autenticacao("ana", "ROLE_USER"), formulario, modelo));
        assertEquals("As senhas não conferem.", modelo.getAttribute("errorMessage"));
        verifyNoInteractions(criptografia);
        verify(repositorio, never()).save(any());
    }

    @Test
    void deveAtualizarSenhaDoPerfilQuandoFormularioEhValido() {
        RepositorioContaUsuario repositorio = mock(RepositorioContaUsuario.class);
        ServicoCriptografiaSenha criptografia = mock(ServicoCriptografiaSenha.class);
        ContaUsuario conta = contaComId(7L, "ana", "hash", false);
        when(repositorio.findByUsername("ana")).thenReturn(Optional.of(conta));
        when(criptografia.criptografar("nova")).thenReturn("novo-hash");
        ControladorPerfil controlador = new ControladorPerfil(repositorio, criptografia);
        ExtendedModelMap modelo = new ExtendedModelMap();

        assertEquals("editar-perfil", controlador.editarPerfil(autenticacao("ana", "ROLE_USER"), formulario("nova", "nova"), modelo));
        assertEquals("Senha atualizada com sucesso.", modelo.getAttribute("successMessage"));
        var captor = org.mockito.ArgumentCaptor.forClass(ContaUsuario.class);
        verify(repositorio).save(captor.capture());
        assertEquals(7L, captor.getValue().getId());
        assertEquals("novo-hash", captor.getValue().getPassword());
    }

    @Test
    void deveRejeitarNovaSenhaVaziaNoPerfil() {
        RepositorioContaUsuario repositorio = mock(RepositorioContaUsuario.class);
        ContaUsuario conta = contaComId(7L, "ana", "hash", false);
        when(repositorio.findByUsername("ana")).thenReturn(Optional.of(conta));
        ControladorPerfil controlador = new ControladorPerfil(repositorio, mock(ServicoCriptografiaSenha.class));
        ExtendedModelMap modelo = new ExtendedModelMap();

        assertEquals("editar-perfil", controlador.editarPerfil(autenticacao("ana", "ROLE_USER"), formulario(" ", " "), modelo));
        assertEquals("Informe uma nova senha.", modelo.getAttribute("errorMessage"));
        verify(repositorio, never()).save(any());
    }

    @Test
    void deveRecusarExclusaoPorUsuarioNaoAdmin() {
        ServicoListarUsuarios listar = mock(ServicoListarUsuarios.class);
        when(listar.listarTodosUsuarios()).thenReturn(List.of());
        ControladorUsuariosAdmin controlador = new ControladorUsuariosAdmin(listar, mock(RepositorioContaUsuario.class));
        ExtendedModelMap modelo = new ExtendedModelMap();

        assertEquals("admin-usuarios", controlador.deletarUsuario(1L, modelo, autenticacao("ana", "ROLE_USER")));
        assertEquals("Acesso negado.", modelo.getAttribute("errorMessage"));
    }

    @Test
    void deveExcluirSomenteUsuarioComumSelecionadoPorAdmin() {
        ServicoListarUsuarios listar = mock(ServicoListarUsuarios.class);
        RepositorioContaUsuario repositorio = mock(RepositorioContaUsuario.class);
        ContaUsuario usuario = contaComId(8L, "ana", "hash", false);
        when(listar.listarTodosUsuarios()).thenReturn(List.of(usuario));
        ControladorUsuariosAdmin controlador = new ControladorUsuariosAdmin(listar, repositorio);
        ExtendedModelMap modelo = new ExtendedModelMap();

        assertEquals("admin-usuarios", controlador.deletarUsuario(8L, modelo, autenticacao("admin", "ROLE_ADMIN")));
        verify(repositorio).deleteById(8L);
        assertEquals("Usuário deletado com sucesso.", modelo.getAttribute("successMessage"));
    }

    @Test
    void deveInformarQuandoUsuarioParaExcluirNaoExiste() {
        ServicoListarUsuarios listar = mock(ServicoListarUsuarios.class);
        when(listar.listarTodosUsuarios()).thenReturn(List.of());
        RepositorioContaUsuario repositorio = mock(RepositorioContaUsuario.class);
        ControladorUsuariosAdmin controlador = new ControladorUsuariosAdmin(listar, repositorio);
        ExtendedModelMap modelo = new ExtendedModelMap();

        assertEquals("admin-usuarios", controlador.deletarUsuario(99L, modelo, autenticacao("admin", "ROLE_ADMIN")));
        assertEquals("Usuário não encontrado.", modelo.getAttribute("errorMessage"));
        verifyNoInteractions(repositorio);
    }

    @Test
    void deveImpedirExclusaoDeOutroAdministrador() {
        ServicoListarUsuarios listar = mock(ServicoListarUsuarios.class);
        RepositorioContaUsuario repositorio = mock(RepositorioContaUsuario.class);
        when(listar.listarTodosUsuarios()).thenReturn(List.of(contaComId(10L, "outro-admin", "hash", true)));
        ControladorUsuariosAdmin controlador = new ControladorUsuariosAdmin(listar, repositorio);
        ExtendedModelMap modelo = new ExtendedModelMap();

        assertEquals("admin-usuarios", controlador.deletarUsuario(10L, modelo, autenticacao("admin", "ROLE_ADMIN")));
        assertEquals("Não é permitido deletar um usuário ADMIN.", modelo.getAttribute("errorMessage"));
        verifyNoInteractions(repositorio);
    }

    private ControladorPerfil.FormularioEditarPerfil formulario(String novaSenha, String confirmacao) {
        ControladorPerfil.FormularioEditarPerfil formulario = new ControladorPerfil.FormularioEditarPerfil();
        formulario.setNovaSenha(novaSenha);
        formulario.setConfirmacaoNovaSenha(confirmacao);
        return formulario;
    }

    private ContaUsuario contaComId(long id, String usuario, String senha, boolean admin) {
        try {
            ContaUsuario conta = new ContaUsuario(usuario, senha, admin);
            Field campo = ContaUsuario.class.getDeclaredField("id");
            campo.setAccessible(true);
            campo.set(conta, id);
            return conta;
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }

    private UsernamePasswordAuthenticationToken autenticacao(String usuario, String role) {
        return new UsernamePasswordAuthenticationToken(usuario, null, List.of(new SimpleGrantedAuthority(role)));
    }
}
