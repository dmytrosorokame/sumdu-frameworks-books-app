package sumdu.edu.ua.web.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Aspect for measuring execution time of all service layer methods.
 */
@Aspect
@Component
public class ServiceMethodLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(ServiceMethodLoggingAspect.class);

    @Around("execution(* sumdu.edu.ua.core.service..*(..))")
    public Object logServiceMethodExecution(ProceedingJoinPoint pjp) throws Throwable {
        String className = pjp.getTarget().getClass().getSimpleName();
        String methodName = pjp.getSignature().getName();
        long start = System.currentTimeMillis();

        log.debug("Entering {}.{}()", className, methodName);

        try {
            Object result = pjp.proceed();
            long time = System.currentTimeMillis() - start;
            log.info("{}.{}() executed in {} ms", className, methodName, time);
            return result;
        } catch (Exception ex) {
            long time = System.currentTimeMillis() - start;
            log.warn("{}.{}() failed in {} ms: {}", className, methodName, time, ex.getMessage());
            throw ex;
        }
    }
}


