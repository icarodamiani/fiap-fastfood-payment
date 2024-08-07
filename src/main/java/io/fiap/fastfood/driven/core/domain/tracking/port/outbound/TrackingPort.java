package io.fiap.fastfood.driven.core.domain.tracking.port.outbound;

import io.fiap.fastfood.driven.core.domain.model.Tracking;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

public interface TrackingPort {

    Mono<SendMessageResponse> create(Tracking tracking);

}
