package at.ac.tuwien.sepr.groupphase.backend.auth;

import at.ac.tuwien.sepr.groupphase.backend.service.AuthenticationService;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.json.JsonGeneratorImpl;
import io.swagger.v3.core.util.Json;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.json.JSONParser;
import org.h2.util.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;

public class AuthenticationInterceptor implements HandlerInterceptor {

    private final AuthenticationService authService;

    public AuthenticationInterceptor(AuthenticationService authService) {
        this.authService = authService;
    }

    ArrayList<String> urlExceptions = new ArrayList<>(Arrays.asList(
        "/api/v1/authentication/login",
        "/api/v1/authentication/logout"
    ));


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        boolean superClassAllows = HandlerInterceptor.super.preHandle(request, response, handler);

        /*
        // check URI
        UriComponents uriComponents = UriComponentsBuilder.fromUriString(request.getRequestURI()).build();
        if (urlExceptions.contains(uriComponents.getPath())) {
            // url is an exception, do not check further
            return superClassAllows;
        }

        // check authentication
        String authToken = request.getHeader("Authorization");
        if (authToken == null) {
            authToken = request.getHeader("authorization");
        }

        if (authToken == null) {
            // no auth token
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Resource access unauthorized, please log in");
            return false;
        } else {
            boolean isAuthenticated = authService.isAuthenticated(authToken);
            System.out.println(authToken);

            if (!isAuthenticated) {
                // invalid auth token
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write("Resource access unauthorized, session may be expired. Please log in again");
                return false;
            }*/

        return superClassAllows;
        //}
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
