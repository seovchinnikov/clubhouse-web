package edu.clubhouseapi.filter;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = new HashMap<>();
        Throwable error = getError(request);
        MergedAnnotation<ResponseStatus> responseStatusAnnotation =
                MergedAnnotations.from(error.getClass(), MergedAnnotations.SearchStrategy.TYPE_HIERARCHY)
                        .get(ResponseStatus.class);
        HttpStatus errorStatus = determineHttpStatus(error, responseStatusAnnotation);
        errorAttributes.put("success", false);
        errorAttributes.put("error_message", determineErrorMessage(errorStatus, error));
        errorAttributes.put("status", errorStatus.value());
        return errorAttributes;
    }

    private String determineErrorMessage(HttpStatus errorStatus, Throwable error) {
        String text = errorStatus.value() + " " + errorStatus.getReasonPhrase();
        if (error instanceof WebClientResponseException) {
            text += " (" + ((WebClientResponseException) error).getResponseBodyAsString() + " )";
        }

        return text;
    }

    private HttpStatus determineHttpStatus(Throwable error, MergedAnnotation<ResponseStatus> responseStatusAnnotation) {
        if (error instanceof WebClientResponseException) {
            WebClientResponseException exc = (WebClientResponseException) error;
            if (exc.getResponseBodyAsString().contains("'Invalid token")) {
                return HttpStatus.UNAUTHORIZED;
            }
            return ((WebClientResponseException) error).getStatusCode();
        }
        if (error instanceof ResponseStatusException) {
            return ((ResponseStatusException) error).getStatus();
        }
        return responseStatusAnnotation.getValue("code", HttpStatus.class).orElse(HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
