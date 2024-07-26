package de.xai.handwriting_labeling_app_backend.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


@Configuration
@EnableWebSecurity
class SecurityConfig (val env: Environment) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                authorize("/ping", permitAll)
                authorize("/files/**", authenticated)
                authorize(HttpMethod.POST, "/addOne", hasRole("ADMIN"))
                authorize("/users/", hasRole("ADMIN"))
                authorize(HttpMethod.PUT, "/greeting", hasRole("USER"))
                authorize(HttpMethod.POST, "/users/login", authenticated)
            }
            httpBasic { }
            csrf { disable() }
            cors { }
        }
        return http.build()
    }

    @Bean
    fun passwordEncoder(): BCryptPasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = mutableListOf(env.getProperty("app.url.root"))
        configuration.allowedHeaders = (listOf("*"))
        configuration.allowedMethods = mutableListOf("GET", "POST", "PUT", "DELETE")

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}