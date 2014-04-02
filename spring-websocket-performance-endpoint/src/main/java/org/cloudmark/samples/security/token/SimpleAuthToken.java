package org.cloudmark.samples.security.token;

import lombok.Data;
import org.apache.shiro.authc.UsernamePasswordToken;

@Data
public class SimpleAuthToken extends UsernamePasswordToken {

    public SimpleAuthToken(final String username, final String password) {
        super(username, password, false, null);
    }
}
