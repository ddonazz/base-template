package it.andrea.start.interceptor;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import it.andrea.start.constants.Language;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LanguageInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        String languageTag = request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        if (languageTag != null && !languageTag.isEmpty()) {
            LocaleContextHolder.setLocale(Language.fromTag(languageTag).orElse(Language.getDefault()).getLocale());
        } else {
            LocaleContextHolder.setLocale(Language.getDefault().getLocale());
        }

        return true;
    }

}
