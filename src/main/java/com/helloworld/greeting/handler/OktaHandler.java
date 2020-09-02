package com.helloworld.greeting.handler;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.networknt.config.Config;
import com.networknt.exception.ApiException;
import com.networknt.handler.Handler;
import com.networknt.handler.MiddlewareHandler;
import com.networknt.status.Status;
import com.networknt.utility.ModuleRegistry;
import com.okta.jwt.AccessTokenVerifier;
import com.okta.jwt.Jwt;
import com.okta.jwt.JwtVerifiers;

import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;

class OktaConfig {
   boolean enabled;

    @JsonIgnore
    String description;

    public OktaConfig() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

public class OktaHandler implements MiddlewareHandler{
    public static final String CONFIG_NAME = "okta";
    static final OktaConfig config =
            (OktaConfig)Config.getInstance().getJsonObjectConfig(CONFIG_NAME, OktaConfig.class);
	private volatile HttpHandler next;
	static final Logger logger = LoggerFactory.getLogger(OktaHandler.class);
	
	public OktaHandler() {
		
	}

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		// TODO Auto-generated method stub
		AccessTokenVerifier jwtVerifier = JwtVerifiers.accessTokenVerifierBuilder()
				.setIssuer("https://dev-496065.okta.com/oauth2/ausuxh5nfrLuPcyxb4x6")
				.setAudience("https://localhost:8443/home")
				.setConnectionTimeout(Duration.ofSeconds(1))
				.setReadTimeout(Duration.ofSeconds(1))
				.build();
		HeaderMap hm = exchange.getRequestHeaders();
		String token = hm.get("authorization").getLast().split(" ")[1];
		System.out.println(token);
		Jwt jwt = jwtVerifier.decode(token);
		if (jwt != null) {
			Handler.next(exchange, this.next);
		}
		else {
			throw new ApiException(new Status(401, "401", "Not authorized.", "Access token validation failed."));
		}
	}

	@Override
	public HttpHandler getNext() {
		// TODO Auto-generated method stub
		return this.next;
	}

	@Override
	public MiddlewareHandler setNext(HttpHandler next) {
		// TODO Auto-generated method stub
		Handlers.handlerNotNull(next);
		this.next = next;
		return this;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return config.isEnabled();
	}

	@Override
	public void register() {
		// TODO Auto-generated method stub
		ModuleRegistry.registerModule(OktaHandler.class.getName(), Config.getInstance().getJsonMapConfigNoCache(CONFIG_NAME), null);
	}

}
