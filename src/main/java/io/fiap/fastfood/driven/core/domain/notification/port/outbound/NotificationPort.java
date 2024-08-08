package io.fiap.fastfood.driven.core.domain.notification.port.outbound;

import io.fiap.fastfood.driven.core.domain.model.Notification;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

public interface NotificationPort {

    Mono<SendMessageResponse> sendNotification(Notification notification);

}
