package kektor.innowise.gallery.image.msg;


import lombok.Builder;

import java.time.Instant;

@Builder
public record LikeEventMessage(EventType eventType,
                               Long imageId,
                               Long userId,
                               String username,
                               Instant instant,
                               Long imageOwnerId,
                               Integer likesCount) {

    public enum EventType {
        LIKE,
        REMOVE_LIKE
    }

}
