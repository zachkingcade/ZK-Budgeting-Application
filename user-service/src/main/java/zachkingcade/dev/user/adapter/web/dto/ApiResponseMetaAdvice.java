package zachkingcade.dev.user.adapter.web.dto;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class ApiResponseMetaAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(
            MethodParameter returnType,
            Class<? extends HttpMessageConverter<?>> converterType
    ) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response
    ) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return body;
        }

        HttpServletRequest httpServletRequest = servletRequest.getServletRequest();
        Object startTimeObj = httpServletRequest.getAttribute(RequestTimingFilter.REQUEST_START_TIME);

        Long durationMs = null;
        if (startTimeObj instanceof Long startTime) {
            durationMs = (System.nanoTime() - startTime) / 1_000_000;
        }

        response.getHeaders().add("X-Response-Time", durationMs + "ms");

        if (body instanceof ApiResponse<?> apiResponse) {
            MetaData metaData = apiResponse.getMetaData();
            if (metaData == null) {
                metaData = new MetaData();
                apiResponse.setMetaData(metaData);
            }

            metaData.setExecutionTimeMs(durationMs);
            return apiResponse;
        }

        return body;
    }
}