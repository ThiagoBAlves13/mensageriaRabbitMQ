package br.com.alurafood.pedidos.dto;

import java.math.BigDecimal;
import java.util.List;

import br.com.alurafood.pedidos.model.ItemDoPedido;
import lombok.Data;

@Data
public class PagamentoDto {

    private Long id;
    private BigDecimal valor;
    private String nome;
    private String numero;
    private String expiracao;
    private String codigo;
    private StatusPagamento status;
    private Long pedidoId;
    private Long formaDePagamentoId;
    private List<ItemDoPedido> itens;
    
}
