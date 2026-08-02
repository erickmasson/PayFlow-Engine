package com.payflow_engine.domain.services;

import com.payflow_engine.api.dtos.NotificationEventDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    @RabbitListener(queues = "notification.queue")
    public void processNotification(NotificationEventDTO eventDTO) throws InterruptedException{
        System.out.println("==================================================");
        System.out.println("INICIANDO PROCESSAMENTO ASSÍNCRONO DE NOTIFICAÇÃO");
        System.out.println("Transação ID: " + eventDTO.transactionId());

        Thread.sleep(3000);

        // Se quisermos simular um erro para ver a mensagem indo para a DLQ,
        // basta lançar uma exceção aqui.
        // if (event.amount().doubleValue() > 1000) {
        //     throw new RuntimeException("Erro ao contactar o servidor de e-mail!");
        // }

        System.out.println("✅ E-mail enviado para Pagador: " + eventDTO.payerEmail());
        System.out.println("✅ Push Notification enviado para Lojista: " + eventDTO.payeeEmail());
        System.out.println("==================================================");
    }
}
