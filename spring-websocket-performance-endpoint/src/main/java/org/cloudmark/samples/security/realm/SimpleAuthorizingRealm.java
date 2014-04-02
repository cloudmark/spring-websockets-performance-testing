package org.cloudmark.samples.security.realm;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.WebUtils;
import org.cloudmark.samples.security.token.SimpleAuthToken;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("simpleAuthorizingRealm")
public class SimpleAuthorizingRealm extends AuthorizingRealm {

    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        Subject subject = SecurityUtils.getSubject();
        if (!WebUtils.isHttp(subject)) throw new UnauthorizedException("Subject argument is not an HTTP-aware instance.  ");
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.addRole("NORMAL");
        return info;
    }


    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        Subject subject = SecurityUtils.getSubject();
        if (!WebUtils.isHttp(subject))
            throw new UnauthorizedException("Subject argument is not an HTTP-aware instance.  ");

        SimpleAuthToken casinoEuroToken = (SimpleAuthToken) token;
        if (!(casinoEuroToken.getUsername().equals(String.valueOf(casinoEuroToken.getPassword())))) {
            throw new UnauthorizedException("The Subject is Unauthorised");
        }

        return new SimpleAuthenticationInfo(casinoEuroToken.getUsername(), casinoEuroToken.getPassword(), getName());
    }

    public boolean supports(AuthenticationToken token) {
        return token != null && SimpleAuthToken.class.isAssignableFrom(token.getClass());
    }


}