package com.example.bookstore.config;

import com.example.bookstore.common.ErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        String schemeName = "bearerAuth";

        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(schemeName))
                .components(new Components().addSecuritySchemes(
                        schemeName,
                        new SecurityScheme()
                                .name(schemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                ));
    }

    /**
     * ErrorResponse 스키마/예시를 components에 명시적으로 등록
     * (컨트롤러 리턴 타입에 직접 등장하지 않아도 Swagger에 노출되도록)
     */
    @Bean
    public OpenApiCustomizer errorResponseSchemaCustomizer() {
        return openApi -> {
            if (openApi.getComponents() == null) openApi.setComponents(new Components());

            // ErrorResponse schema 등록
            ModelConverters.getInstance().read(ErrorResponse.class)
                    .forEach((name, schema) -> openApi.getComponents().addSchemas(name, schema));

            // 대표 예시(components/examples) 등록(옵션)
            openApi.getComponents().addExamples("ErrorBadRequest", example("BAD_REQUEST", 400,
                    "요청 형식이 올바르지 않습니다.", Map.of("reason", "invalid JSON")));
            openApi.getComponents().addExamples("ErrorUnauthorized", example("UNAUTHORIZED", 401,
                    "인증이 필요합니다.", null));
            openApi.getComponents().addExamples("ErrorForbidden", example("FORBIDDEN", 403,
                    "권한이 없습니다.", null));
            openApi.getComponents().addExamples("ErrorNotFound", example("NOT_FOUND", 404,
                    "리소스를 찾을 수 없습니다.", Map.of("id", "999")));
            openApi.getComponents().addExamples("ErrorValidation", example("VALIDATION_FAILED", 422,
                    "입력값이 올바르지 않습니다.", Map.of("title", "must not be blank")));
            openApi.getComponents().addExamples("ErrorTooManyRequests", example("TOO_MANY_REQUESTS", 429,
                    "요청 한도를 초과했습니다.", Map.of("retryAfterSec", 60)));
            openApi.getComponents().addExamples("ErrorInternal", example("INTERNAL_ERROR", 500,
                    "서버 오류가 발생했습니다.", null));
        };
    }

    /**
     * 모든 엔드포인트에 과제 요구사항(400/401/403/404/422/500) 에러 예시 응답을 자동 부착
     * (추가로 429도 같이 넣음)
     */
    @Bean
    public OpenApiCustomizer globalErrorResponsesCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) return;

            openApi.getPaths().forEach((path, pathItem) -> {
                pathItem.readOperations().forEach(operation -> {
                    ApiResponses responses = operation.getResponses();
                    if (responses == null) {
                        responses = new ApiResponses();
                        operation.setResponses(responses);
                    }

                    addErrorIfAbsent(responses, path, "400", "BAD_REQUEST", 400,
                            "요청 형식이 올바르지 않습니다.", Map.of("reason", "invalid parameter"));
                    addErrorIfAbsent(responses, path, "401", "UNAUTHORIZED", 401,
                            "인증이 필요합니다.", null);
                    addErrorIfAbsent(responses, path, "403", "FORBIDDEN", 403,
                            "권한이 없습니다.", null);
                    addErrorIfAbsent(responses, path, "404", "NOT_FOUND", 404,
                            "리소스를 찾을 수 없습니다.", Map.of("id", "999"));
                    addErrorIfAbsent(responses, path, "422", "VALIDATION_FAILED", 422,
                            "입력값이 올바르지 않습니다.", Map.of("field", "must not be blank"));
                    addErrorIfAbsent(responses, path, "429", "TOO_MANY_REQUESTS", 429,
                            "요청 한도를 초과했습니다.", Map.of("retryAfterSec", 60));
                    addErrorIfAbsent(responses, path, "500", "INTERNAL_ERROR", 500,
                            "서버 오류가 발생했습니다.", null);
                });
            });
        };
    }

    private void addErrorIfAbsent(ApiResponses responses,
                                  String path,
                                  String httpStatus,
                                  String code,
                                  int status,
                                  String message,
                                  Object details) {
        if (responses.containsKey(httpStatus)) return;

        Schema<?> schemaRef = new Schema<>().$ref("#/components/schemas/ErrorResponse");
        Map<String, Object> payload = errorPayload(path, code, status, message, details);

        MediaType mt = new MediaType().schema(schemaRef).example(payload);

        ApiResponse ar = new ApiResponse()
                .description(code)
                .content(new Content().addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE, mt));

        responses.addApiResponse(httpStatus, ar);
    }

    private Example example(String code, int status, String message, Object details) {
        Example ex = new Example();
        ex.setValue(errorPayload("/api/example", code, status, message, details));
        return ex;
    }

    private Map<String, Object> errorPayload(String path, String code, int status, String message, Object details) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("timestamp", "2025-03-05T12:34:56Z");
        m.put("path", path);
        m.put("status", status);
        m.put("code", code);
        m.put("message", message);
        if (details != null) m.put("details", details);
        return m;
    }
}
