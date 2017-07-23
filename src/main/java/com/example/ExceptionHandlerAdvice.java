package com.example;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExceptionHandlerAdvice {
    @Around("execution(* org.springframework.boot.CommandLineRunner+.run(..))")
    public void handleException(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            joinPoint.proceed();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}