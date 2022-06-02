# Read Me First

### How to start

Voraussetzungen: 
* Java 17
* Maven

Um das Backend zu starten, reicht es aus die Umgebungsvariablen für Keycloak aus den application.properties zu setzen 
und die intellij run config zu nutzen oder mvn spring-boot:run auszuführen. 

### Some details

Bisher nutzen wir in vielen Fällen  die Keycloak spring boot starter dependency um unsere rest backends oder apis über
Keycloak abzusichern.

Dieses Code Beispiel zeigt eine alternative Implementierung über die spring oauth2-resource-server dependency. 
Diese muss zur pom hinzugefügt werden:

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>

Zur Validierung des Tokens werden folgende [application.properties](https://github.com/patrickhofmann0/spring-rest-oauth-example/blob/master/src/main/resources/application.properties) benötigt: 

    keycloak.url=${KEYCLOAK_URL}
    keycloak.realm=${KEYCLOAK_REALM}
    
    spring.security.oauth2.resourceserver.jwt.issuer-uri=${keycloak.url}/auth/realms/${keycloak.realm}
    spring.security.oauth2.resourceserver.jwt.jwk-set-uri=${keycloak.url}/auth/realms/${keycloak.realm}/protocol/openid-connect/certs

In der [WebSecurityConfig](https://github.com/patrickhofmann0/spring-rest-oauth-example/blob/master/src/main/java/de/ecclesia/example/springrestoauth/springrestoauthexample/WebSecurityConfig.java) (in der aktuellen Version von spring security ist der WebSecurityConfigurerAdapter deprecated) 
werden wie bisher die Zugriffsrechte definiert (in diesem Beispiel nur .authenticated()).

    http.authorizeRequests()
        .antMatchers("/hello")
        .authenticated()

        .anyRequest()
        .denyAll()

Um die Endpunkte nun über oauth abzusichern: 

        .and()
        .oauth2ResourceServer()
        .jwt(); // typ des tokens

Für die von Keycloak erstellten Tokens muss ein Konverter implementiert werden, um die Rollen aus dem Token zu konvertieren. 

        .jwtAuthenticationConverter(jwtAuthenticationConverter());

Je nach dem welche Rollen benötigt werden. Eine Kombination der beiden Konverter ist auch möglich.

    // für realm roles
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter = jwt -> {
        Map<String, Collection<String>> realmAccess = jwt.getClaim("realm_access");
        Collection<String> roles = realmAccess.get("roles");
        return roles.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
        };
        var jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setPrincipalClaimName("preferred_username");
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
		return jwtAuthenticationConverter;
	}

    // resouce roles
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter = jwt -> {
            Map<String, Object> resourceAccessMap = jwt.getClaimAsMap("resource_access");
            JSONObject resourceBackend = (JSONObject) resourceAccessMap.get(this.clientId);
            Collection<String> roles = (Collection<String>) resourceBackend.get("roles");
                return roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
                };
            var jwtAuthenticationConverter = new JwtAuthenticationConverter();
            jwtAuthenticationConverter.setPrincipalClaimName("preferred_username");
            jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

### Spring Doku: 

https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html 
