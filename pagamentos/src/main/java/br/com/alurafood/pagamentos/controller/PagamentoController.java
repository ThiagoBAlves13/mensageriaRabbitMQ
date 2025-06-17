package br.com.alurafood.pagamentos.controller;

import java.net.URI;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.alurafood.pagamentos.dto.PagamentoDto;
import br.com.alurafood.pagamentos.service.PagamentoService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;


@RestController
@RequestMapping("/pagamentos")
public class PagamentoController {

    @Autowired
    PagamentoService pagamentoService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @GetMapping
    public ResponseEntity<?> listar(@PageableDefault(size = 10) Pageable paginacao){
        return ResponseEntity.ok(pagamentoService.obterTodos(paginacao));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detalhar(@PathVariable @NotNull Long id){
        PagamentoDto pagamentoDto = pagamentoService.obterPorId(id);

        return ResponseEntity.ok(pagamentoDto);
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody @Valid PagamentoDto dto, UriComponentsBuilder uriBuilder) {
        PagamentoDto pagamento = pagamentoService.criarPagamento(dto);

        URI endereco = uriBuilder.path("/pagamentos/{id}").buildAndExpand(pagamento.getId()).toUri();
        
        var message = new Message(("Pagamento concluído com id " + pagamento.getId()).getBytes());
        rabbitTemplate.send("pagamento.concluido", message);
        return ResponseEntity.created(endereco).body(pagamento);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable @NotNull Long id, @RequestBody @Valid PagamentoDto dto){
        PagamentoDto pagamentoAtualizado = pagamentoService.atualizarPagamento(id, dto);

        return ResponseEntity.ok(pagamentoAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable @NotNull Long id){
        pagamentoService.excluirPagamento(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/confirmar")
    @CircuitBreaker(name= "atualizaPagamento", fallbackMethod= "pagamentoAutorizadoIntegracaoPendente")
    public void confirmarPagamento(@PathVariable @NotNull Long id){
        pagamentoService.confirmarPagamento(id);
    }

    public void pagamentoAutorizadoIntegracaoPendente(Long id, Exception e){
        pagamentoService.alteraStatus(id);
    }
    
}
