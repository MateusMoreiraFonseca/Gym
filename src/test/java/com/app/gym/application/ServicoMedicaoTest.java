package com.app.gym.application;

import com.app.gym.domain.ContaUsuario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class ServicoMedicaoTest {
    @Test
    void deveCalcularImc() {
        ServicoMedicao servico = new ServicoMedicao(mock(RepositorioMedicao.class));

        assertEquals(24.69, servico.calcularImc(80, 1.8), 0.01);
    }

    @Test
    void deveClassificarImc() {
        ServicoMedicao servico = new ServicoMedicao(mock(RepositorioMedicao.class));

        assertEquals("Abaixo do peso", servico.classificacaoImc(18.4));
        assertEquals("Peso normal", servico.classificacaoImc(24.9));
        assertEquals("Sobrepeso", servico.classificacaoImc(29.9));
        assertEquals("Obesidade", servico.classificacaoImc(30));
    }

    @Test
    void deveRejeitarMedidasInvalidas() {
        ServicoMedicao servico = new ServicoMedicao(mock(RepositorioMedicao.class));

        assertThrows(IllegalArgumentException.class, () -> servico.calcularImc(0, 1.8));
        assertThrows(IllegalArgumentException.class, () -> servico.calcularImc(80, 0));
    }
}
