package cn.ak.gc.commen.utils;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SecurityAspect {
    @Pointcut("within(cn.ak.gc.*)")
    public void matchType() {

    }

    @Before("matchType()")
    public void before() {

    }
}
