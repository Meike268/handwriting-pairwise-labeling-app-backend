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

                authorize(HttpMethod.GET, "/matrix", hasRole("ADMIN"))

                authorize(HttpMethod.GET, "/matrix/{username}", authenticated)
                authorize(HttpMethod.POST, "/matrix/{username}/comparison", authenticated)
                authorize(HttpMethod.POST, "/matrix/{username}/save", authenticated)

                authorize(HttpMethod.GET, "/userBatchLog", hasRole("ADMIN"))
                authorize(HttpMethod.GET, "/userBatchLog/{username}", authenticated)


                authorize("/users", hasRole("ADMIN"))
                authorize(HttpMethod.POST, "/addOne", hasRole("ADMIN"))
                authorize(HttpMethod.POST, "/users/login", authenticated)

                authorize(HttpMethod.GET, "/batch", authenticated)

                authorize(HttpMethod.POST, "/answers", authenticated)
                authorize(HttpMethod.PUT, "/answers", authenticated)
                authorize(HttpMethod.GET, "/answers", hasRole("ADMIN"))
                authorize(HttpMethod.DELETE, "/answers/ofsample/{id}", hasRole("ADMIN"))

                authorize(HttpMethod.GET, "/reports", hasRole("ADMIN"))
                authorize(HttpMethod.POST, "/reports", authenticated)

                authorize(HttpMethod.GET, "/samples", hasRole("ADMIN"))

                authorize(HttpMethod.GET, "/config", hasRole("ADMIN"))
                authorize(HttpMethod.POST, "/config", hasRole("ADMIN"))

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
        val it = env.getProperty("app.url.roots")?.split(",")
        configuration.allowedOrigins = it
        configuration.allowedHeaders = listOf("*")
        configuration.allowedMethods = mutableListOf("GET", "POST", "PUT", "DELETE")

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}