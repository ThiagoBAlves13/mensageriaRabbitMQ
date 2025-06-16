package br.com.alurafood.pagamentos.model;

import java.util.List;

import lombok.Data;

@Data
public class Pedido {
    List<ItemDoPedido> itens;
}
