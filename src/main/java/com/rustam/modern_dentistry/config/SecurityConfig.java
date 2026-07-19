package com.rustam.modern_dentistry.config;

import com.rustam.modern_dentistry.util.JwtAuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;
import java.util.function.Supplier;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthUtil jwtAuthFilter;
    private final SuperAdminBypassAuthorizationManager superAdminBypassAuthorizationManager;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(x -> {
                    x.requestMatchers(getPublicEndpoints()).permitAll();
                    // ✅ Specialization READ — public endpoint (token lazım deyil)
                    x.requestMatchers(HttpMethod.GET, "/api/v1/specialization/read").permitAll();
                    // ✅ Permission READ and Worker INFO - hər authenticated istifadəçi üçün
                    x.requestMatchers(HttpMethod.GET, "/api/v1/permission/read").authenticated();
                    x.requestMatchers(HttpMethod.GET, "/api/v1/add-worker/info/{id}").authenticated();
                    registerModulePermissions(x);
                    x.anyRequest().access(superAdminBypassAuthorizationManager);
                })
                .sessionManagement(x -> x.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(LogoutConfigurer::permitAll)
                .httpBasic(withDefaults())
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .cors(cors -> cors.configurationSource(request -> getCorsConfiguration()))
                .build();
    }

    private void registerModulePermissions(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry x) {
        List<String> modules = List.of(
                "patient", "appointment", "add-worker", "general-calendar",
                "patient-blacklist", "reservation", "technician",
                "workers-work-schedule", "specialization",
                "laboratory", "laboratory-payment",
                "patient-anamnesis", "patient-examinations", "patient-photos",
                "patient-recipe", "patient-report", "patient-treatment",
                "patient-videos", "patient-xray", "patient-insurance-balance",
                "patient-insurance", "patient-plans", "patient-plans-main",
                "appointment-type", "blacklist-result", "cabinet", "ceramic",
                "color", "examination", "garniture", "insurance-company",
                "metal", "price-category", "specialization-category",
                "teeth", "teeth-examination", "teeth-operation", "permission",
                "room-stock", "warehouse-entry", "warehouse-removal",
                "deletion-from-warehouse", "order-from-warehouse", "warehouse",
                "reports"
        );

        for (String module : modules) {
            String basePath = "/api/v1/" + module + "/**";
            x.requestMatchers(HttpMethod.GET, basePath).access((auth, ctx) -> decision(auth, basePath, "READ"));
            x.requestMatchers(HttpMethod.POST, basePath).access((auth, ctx) -> decision(auth, basePath, "CREATE"));
            x.requestMatchers(HttpMethod.PUT, basePath).access((auth, ctx) -> decision(auth, basePath, "UPDATE"));
            x.requestMatchers(HttpMethod.DELETE, basePath).access((auth, ctx) -> decision(auth, basePath, "DELETE"));
        }
    }

    private AuthorizationDecision decision(Supplier<Authentication> authSupplier, String path, String action) {
        Authentication auth = authSupplier.get();

        if (auth != null && auth.isAuthenticated()) {
            boolean isSuperAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("SUPER_ADMIN"));

            if (isSuperAdmin) return new AuthorizationDecision(true);

            String moduleName = getModuleNameFromPath(path);
            String requiredPermission = moduleName + ":" + action;
            boolean hasPermission = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals(requiredPermission));

            return new AuthorizationDecision(hasPermission);
        }

        return new AuthorizationDecision(false);
    }

    private String getModuleNameFromPath(String path) {
        if (path.contains("/patient-blacklist")) return "Qara siyahı səbəbləri";
        if (path.contains("/patient-anamnesis")) return "Anamnez";
        if (path.contains("/patient-examinations")) return "Müayinələri";
        if (path.contains("/patient-photos")) return "Fotolar";
        if (path.contains("/patient-recipe")) return "Reseptləri";
        if (path.contains("/patient-report")) return "Hesabat";
        if (path.contains("/reports")) return "Hesabat";
        if (path.contains("/patient-treatment")) return "Müalicələr";
        if (path.contains("/patient-videos")) return "Videolar";
        if (path.contains("/patient-xray")) return "Rentgen";
        if (path.contains("/patient-insurance")) return "Sığortaları";
        if (path.contains("/patient-plans")) return "Planları";
        if (path.contains("/patient")) return "Pasientlər";
        
        if (path.contains("/appointment-type")) return "Randevu tipləri";
        if (path.contains("/appointment")) return "Ümumi təqvim";
        if (path.contains("/general-calendar")) return "Ümumi təqvim";
        if (path.contains("/reservation")) return "Ümumi təqvim";
        if (path.contains("/add-worker")) return "Həkimlər";
        if (path.contains("/technician")) return "Texniklər";
        if (path.contains("/workers-work-schedule")) return "Həkimlərin iş qrafiki";
        if (path.contains("/specialization")) return "İxtisaslar";
        
        if (path.contains("/laboratory")) return "Gələn sifarişlər";
        
        if (path.contains("/cabinet")) return "Kabinetlər";
        if (path.contains("/color")) return "Rənglər";
        if (path.contains("/ceramic")) return "Digər";
        if (path.contains("/metal")) return "Digər";
        if (path.contains("/implant")) return "İmplantlar";
        if (path.contains("/garniture")) return "Qarnirlar";
        if (path.contains("/insurance-company")) return "Sığorta şirkətləri";
        if (path.contains("/price-category")) return "Qiymət kateqoriyaları";
        if (path.contains("/examination")) return "Müayinə siyahısı";
        if (path.contains("/blacklist-result")) return "Qara siyahı səbəbləri";
        if (path.contains("/teeth")) return "Digər";
        if (path.contains("/permission")) return "İcazələr";
        
        if (path.contains("/room-stock")) return "Kabinet/Obyekt stoku";
        if (path.contains("/warehouse-entry")) return "Anbardan daxilolmalar";
        if (path.contains("/warehouse-removal")) return "Anbardan maxaric";
        if (path.contains("/deletion-from-warehouse")) return "Anbardan silinmə";
        if (path.contains("/order-from-warehouse")) return "Anbara sifariş";
        if (path.contains("/warehouse")) return "Klinikanın stoku";
        
        return path;
    }

    private String[] getPublicEndpoints() {
        return new String[]{
                "/api/v1/auth/login",
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html/**"
        };
    }

    private CorsConfiguration getCorsConfiguration() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.addAllowedHeader("*");
        return corsConfiguration;
    }
}