package com.csctracker.securitycore.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
@EnableResourceServer
public class ResourceConfig extends ResourceServerConfigurerAdapter {
	private static final String RESOURCE_ID = "core";

	private static String nomeModulo;
	
	public static String getResourceId() {
		return nomeModulo;
	}
	
	@Value("${auth.ip:#{\"127.0.0.1\"}}")
	private String ipAuth;
	
	@Value("${auth.port:${server.port}}")
	private String portAuth;
	
	@Override
	public void configure(HttpSecurity http) throws Exception {
		http
				.authorizeRequests()
				.antMatchers(HttpMethod.OPTIONS).permitAll()
				.antMatchers("/actuator/**").permitAll()
				.antMatchers("/favicon.*").permitAll()
				.anyRequest().authenticated()
				.and().exceptionHandling().accessDeniedHandler(accessDeniedHandler());
	}
	
	public RemoteTokenServices remoteTokenServices(String ipAuth, String portAuth) throws InvalidTokenException {
		
		return new CustomRemoteToken("OAUTH", "OAUTH", "http://" + ipAuth + ":" + portAuth + "/oauth/check_token");
		
	}
	
	@Bean
	public RemoteTokenServices remoteTokenServices() {
		
		return remoteTokenServices(ipAuth, portAuth);
		
	}
	
	@Bean
	public AuthenticationEntryPoint customAuthEntryPoint() {
		return new AccessDenied();
	}
	
	@Bean
	public BCryptPasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public AccessDeniedHandler accessDeniedHandler() {
		return new AccessDenied();
	}
	
	public static String getNomeModulo() {
		return nomeModulo;
	}
	
	public static void setNomeModulo(String nomeModulo) {
		ResourceConfig.nomeModulo = nomeModulo;
	}
	
	@Override
	public void configure(ResourceServerSecurityConfigurer resources) {
		resources.resourceId(RESOURCE_ID).stateless(false);
		resources.authenticationEntryPoint(customAuthEntryPoint());
	}
}
