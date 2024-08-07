package io.fiap.fastfood.driven.core.domain.notification.port.outbound;

import io.fiap.fastfood.driven.core.domain.model.Notification;
import reactor.core.publisher.Mono;

public interface NotificationPort {

    Mono<Void> sendNotification(Notification notification);

}
