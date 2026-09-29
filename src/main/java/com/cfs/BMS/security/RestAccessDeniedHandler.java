package com.cfs.BMS.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/** 403 in the same JSON shape as every other error. */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"timestamp\":\"" + Instant.now() + "\",\"status\":403,"
                + "\"error\":\"Forbidden\",\"message\":\"You do not have permission to perform this action\","
                + "\"path\":\"" + RestAuthenticationEntryPoint.escape(request.getRequestURI()) + "\",\"errors\":null}");
    }
}
