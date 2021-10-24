package cn.org.chenxh.annotation;

import cn.org.chenxh.entity.User;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @description 登录切面
 * @author chenxh
 * @date 2019-1-21
 */
@Aspect
@Component
public class LoginAspect {

    @Pointcut("execution(public * cn.org.chenxh.controller.*.*(..)) && @annotation(cn.org.chenxh.annotation.Login)")
    public void pointcut(){}

    @Around("pointcut()")
    public Object Interceptor(ProceedingJoinPoint pjp) throws Throwable {
        // 获取请求/响应对象
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        String path = request.getContextPath();
        // 登录认证
        User loginUser = (User) request.getSession().getAttribute( "LOGIN_USER" );
        if (loginUser == null) {
            response.sendRedirect( path+"/login.html" );
        }
        try {
            return pjp.proceed();
        } catch (Throwable throwable) {
            throw throwable;
        }
    }
}