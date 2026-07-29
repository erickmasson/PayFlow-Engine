package com.payflow_engine.infra.aop;

import com.payflow_engine.api.annotations.Idempotent;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.tomcat.util.http.fileupload.RequestContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;

@Aspect
@Component
public class IdempotencyAspect {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String IDEMPOTENCY_HEADER = "X-Idempotency-Key";

    public IdempotencyAspect(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Around("@annotation(idempotent)")
    public Object processIdempotency(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable{

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String idempotencyKey = request.getHeader(IDEMPOTENCY_HEADER);

        if(idempotencyKey == null || idempotencyKey.isBlank()){
            throw new IllegalArgumentException("O cabeçalho " + idempotencyKey + " é obrigatório nesta operação.");
        }

        String cacheKey = "idempotency:" + idempotencyKey;

        // Buscar no Redis
        Object cachedResponse = redisTemplate.opsForValue().get(cacheKey);

        if(cachedResponse != null){
            return ResponseEntity.status(201).body(cachedResponse);
        }

        // Se não exixstir, deixar o fluxo do controller seguir
        Object result = joinPoint.proceed();

        // Salvar o corpo da resposta no redis
        if(result instanceof ResponseEntity<?> responseEntity){
            Object body = responseEntity.getBody();

            redisTemplate.opsForValue().set(cacheKey, body, Duration.ofMinutes(15));
        }

        return result;
    }

}
