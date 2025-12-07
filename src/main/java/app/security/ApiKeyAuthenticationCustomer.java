package app.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Objects;

public class ApiKeyAuthenticationCustomer extends AbstractAuthenticationToken {

    private final String apiKey;

    public ApiKeyAuthenticationCustomer(String apiKey, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.apiKey = apiKey;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return apiKey;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (getClass() != other.getClass()) return false;
        return Objects.equals(apiKey, ((ApiKeyAuthenticationCustomer) other).apiKey);
    }

    @Override
    public int hashCode(){
        int result = 1;
        return 31 * result + ((apiKey == null)? 0: apiKey.hashCode());
    }

}
