package kektor.innowise.gallery.image.dto;

import lombok.Builder;
import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;

import java.time.Instant;

@Builder
public record KeySetScrollRequest(
        Sort sort,
        Limit limit,
        Instant tillDate,
        KeysetScrollPosition scrollPosition
) {
}