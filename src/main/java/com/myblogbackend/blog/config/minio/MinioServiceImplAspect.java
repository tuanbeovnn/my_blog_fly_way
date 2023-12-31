package com.myblogbackend.blog.config.minio;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MinioServiceImplAspect {

    @Before(value="execution(* com.myblogbackend.blog.services.MinioService.*(..))")
    public void beforeAdvice(final JoinPoint joinPoint){
        System.out.println("MinioServiceImplAspect | Before MinioService method got called");
    }

    @After(value="execution(* com.myblogbackend.blog.services.MinioService.*(..))")
    public void afterAdvice(final JoinPoint joinPoint){
        System.out.println("MinioServiceImplAspect | After MinioService method got called");
    }

    @AfterReturning(value="execution(* com.myblogbackend.blog.services.MinioService.*(..))")
    public void afterReturningAdvice(final JoinPoint joinPoint){
        System.out.println("MinioServiceImplAspect | AfterReturning MinioService method got called");
    }
}
