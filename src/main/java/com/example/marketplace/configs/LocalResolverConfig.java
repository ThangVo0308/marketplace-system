package com.example.marketplace.configs;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;
 //(multi-language support)
@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocalResolverConfig extends AcceptHeaderLocaleResolver implements WebMvcConfigurer {

    List<Locale> LOCALES = List.of(
            Locale.of("en"),
            Locale.of("vi")
    );

    @Override
    public @NonNull Locale resolveLocale(@NonNull HttpServletRequest request) {
        String headerLang = request.getHeader("Accept-Language");
        return StringUtils.hasLength(headerLang)
                ? Locale.lookup(Locale.LanguageRange.parse(headerLang), LOCALES)
                : Locale.getDefault();
    }

    @Bean
    public MessageSource messageSource(
            @Value("${spring.messages.basename}") String basename,
            @Value("${spring.messages.encoding}") String encoding,
            @Value("${spring.messages.default-locale}") String defaultLocale,
            @Value("${spring.messages.cache-duration}") int cacheSeconds
    ) {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename(basename);
        messageSource.setDefaultEncoding(encoding);
        messageSource.setDefaultLocale(Locale.of(defaultLocale));
        messageSource.setCacheSeconds(cacheSeconds);
        return messageSource;
    }

}
