package de.xai.handwriting_labeling_app_backend.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain


@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                authorize("/api/ping", permitAll)
                authorize(HttpMethod.POST, "/api/addOne", hasRole("ADMIN"))
                authorize(HttpMethod.GET, "/api/users", hasRole("ADMIN"))
                authorize(HttpMethod.POST, "/api/users", hasRole("ADMIN"))
                authorize(HttpMethod.PUT, "/api/greeting", hasRole("USER"))
            }
            httpBasic { }
            csrf { disable() }
        }
        return http.build()
    }

    @Bean
    fun passwordEncoder(): BCryptPasswordEncoder {
        return BCryptPasswordEncoder()
    }
}