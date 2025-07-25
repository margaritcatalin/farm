package ro.piata.localmarket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import ro.piata.localmarket.core.security.AuthoritiesConstants;
import ro.piata.localmarket.web.filter.SpaWebFilter;

import static org.springframework.security.config.Customizer.withDefaults;


@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

    private final Environment env;

    private final LocalMarketProperties localMarketProperties;

    public SecurityConfiguration(Environment env, LocalMarketProperties localMarketProperties) {
        this.env = env;
        this.localMarketProperties = localMarketProperties;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(csrf -> csrf.disable())
                .addFilterAfter(new SpaWebFilter(), BasicAuthenticationFilter.class)
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(localMarketProperties.getSecurity().getContentSecurityPolicy()))
                        .frameOptions(FrameOptionsConfig::sameOrigin)
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .permissionsPolicyHeader(permissions -> permissions.policy(
                                "camera=(), fullscreen=(self), geolocation=(), gyroscope=(), magnetometer=(), microphone=(), midi=(), payment=(), sync-xhr=()"
                        ))
                )
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/index.html", "/*.js", "/*.txt", "/*.json", "/*.map", "/*.css").permitAll()
                        .requestMatchers("/*.ico", "/*.png", "/*.svg", "/*.webapp").permitAll()
                        .requestMatchers("/app/**", "/i18n/**", "/content/**", "/swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/authenticate").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/authenticate").permitAll()
                        .requestMatchers("/api/register", "/api/activate",
                                "/api/account/reset-password/init", "/api/account/reset-password/finish").permitAll()
                        .requestMatchers("/api/admin/**").hasAuthority(AuthoritiesConstants.ADMIN)
                        .requestMatchers("/api/**").authenticated()
                        .requestMatchers("/v3/api-docs/**").hasAuthority(AuthoritiesConstants.ADMIN)
                        .requestMatchers("/management/health", "/management/health/**", "/management/info", "/management/prometheus").permitAll()
                        .requestMatchers("/management/**").hasAuthority(AuthoritiesConstants.ADMIN)
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                        .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()));

        if (env.acceptsProfiles(Profiles.of(ConfigConstants.SPRING_PROFILE_DEVELOPMENT))) {
            http.authorizeHttpRequests(authz -> authz
                    .requestMatchers("/h2-console/**").permitAll()
            );
        }

        return http.build();
    }

}
