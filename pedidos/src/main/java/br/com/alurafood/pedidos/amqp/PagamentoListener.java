package br.com.alurafood.pedidos.amqp;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PagamentoListener {

    @RabbitListener(queues= "pagamento.concluido")
    public void recebeMensagem(Message message){
        System.out.println("Recebido mensagem: " + message.toString());
    }

}
