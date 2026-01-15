package kektor.innowise.gallery.image.controller.argument;

import kektor.innowise.gallery.image.dto.KeySetScrollRequest;
import kektor.innowise.gallery.image.exception.InvalidCursorParameter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static java.util.stream.Collectors.toMap;


@Component
@RequiredArgsConstructor
public class KeySetScrollPositionArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String CURSOR_PARAM_PREFIX = "cursor-last-";
    private static final int DEFAULT_SIZE = 10;
    public static final Map<String, Converter<String, ?>> ALLOWED_SORTING_PARAMETERS =
            Map.ofEntries(
                    entry("cursor-last-uploadedAt", Instant::parse),
                    entry("cursor-last-likesCount", Integer::parseInt),
                    entry("cursor-last-id", Long::parseLong)
            );


    private final SortHandlerMethodArgumentResolver sortResolver;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return KeySetScrollRequest.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {

        Sort sort = extractSort(parameter, webRequest);
        Limit limit = extractLimit(webRequest);
        Instant tillDate = extractTillDate(webRequest);
        Map<String, ?> cursorAttributes = extractCursorParams(webRequest);

        KeysetScrollPosition scrollPosition = !cursorAttributes.isEmpty() ?
                ScrollPosition.forward(cursorAttributes) :
                ScrollPosition.keyset();

        return KeySetScrollRequest.builder()
                .sort(sort)
                .limit(limit)
                .tillDate(tillDate)
                .scrollPosition(scrollPosition)
                .build();
    }

    private Map<String, ?> extractCursorParams(NativeWebRequest webRequest) {
        return webRequest.getParameterMap()
                .keySet()
                .stream()
                .filter(paramName -> paramName.startsWith(CURSOR_PARAM_PREFIX)
                        && webRequest.getParameter(paramName) != null)
                .filter(ALLOWED_SORTING_PARAMETERS::containsKey)
                .collect(toMap(
                        this::convertToModelAttributeName,
                        cursorParameter -> {
                            String strCursorValue = webRequest.getParameter(cursorParameter);
                            return convertToTargetModelType(cursorParameter, strCursorValue);
                        }));
    }

    private Limit extractLimit(NativeWebRequest webRequest) {
        return Limit.of(
                Optional.ofNullable(webRequest.getParameter("size"))
                        .map(Integer::valueOf)
                        .orElse(DEFAULT_SIZE)
        );
    }

    private Instant extractTillDate(NativeWebRequest webRequest) {
        return
                Optional.ofNullable(webRequest.getParameter("tillDate"))
                        .map(Instant::parse)
                        .orElse(null);
    }

    private Sort extractSort(MethodParameter parameter,
                             NativeWebRequest webRequest) {
        return sortResolver.resolveArgument(
                parameter, null, webRequest, null);
    }

    private String convertToModelAttributeName(String cursorParameter) {
        return cursorParameter.substring(CURSOR_PARAM_PREFIX.length());
    }

    private Object convertToTargetModelType(String cursorParameter, String cursorValue) {
        try {
            return ALLOWED_SORTING_PARAMETERS.get(cursorParameter)
                    .convert(cursorValue);
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new InvalidCursorParameter(cursorParameter, cursorValue);
        }
    }

}
