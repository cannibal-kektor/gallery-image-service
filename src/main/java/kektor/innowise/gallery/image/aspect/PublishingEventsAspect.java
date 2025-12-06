package kektor.innowise.gallery.image.aspect;


import kektor.innowise.gallery.image.dto.ImageDto;
import kektor.innowise.gallery.image.mapper.ImageMapper;
import kektor.innowise.gallery.image.msg.LikeEventMessage;
import kektor.innowise.gallery.image.service.SecurityService;
import kektor.innowise.gallery.security.UserPrincipal;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;

@Aspect
@Order(LOWEST_PRECEDENCE)
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublishingEventsAspect {

    ApplicationEventPublisher eventPublisher;
    SecurityService securityService;
    ImageMapper mapper;

    @Pointcut("within(kektor.innowise.gallery.image.service.*)")
    public void inService() {
    }

    @Pointcut("inService() && @annotation(kektor.innowise.gallery.image.aspect.PublishLikeEvent)")
    public void likeEventServiceTriggerMethod() {
    }

    @AfterReturning(
            pointcut = "likeEventServiceTriggerMethod()",
            returning = "image",
            argNames = "image"
    )
    public ImageDto likeEventAdvice(ImageDto image) {
        var eventType = image.isLiked() ?
                LikeEventMessage.EventType.LIKE :
                LikeEventMessage.EventType.REMOVE_LIKE;
        UserPrincipal user = securityService.currentUser();
        LikeEventMessage eventMessage = mapper.toEvent(image, user,  eventType);
        eventPublisher.publishEvent(eventMessage);
        return image;
    }

}
