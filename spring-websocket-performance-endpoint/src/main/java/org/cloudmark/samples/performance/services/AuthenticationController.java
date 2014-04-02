package org.cloudmark.samples.performance.services;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresGuest;
import org.apache.shiro.subject.Subject;
import org.cloudmark.samples.security.token.SimpleAuthToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class AuthenticationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    /**
     * {@inheritDoc}
     */
    @RequiresGuest
    @RequestMapping(value = {"/login"}, method = RequestMethod.GET)
    public @ResponseBody Map<String, String> login(@RequestHeader("username") String username, @RequestHeader("password") String password) {
        logger.info("Attempting to login user : " + username);
        Subject currentUser = SecurityUtils.getSubject();
        SimpleAuthToken casinoEuroToken = new SimpleAuthToken(username, password);
        currentUser.login(casinoEuroToken);
        return new HashMap<String, String>(){{
            this.put("session", (String)SecurityUtils.getSubject().getSession().getId());
        }};
    }


    /**
     * {@inheritDoc}
     */
    @RequiresAuthentication
    @RequestMapping(value = {"/logout"}, method = RequestMethod.GET, consumes = {"application/json"}, produces = {"application/json"})
    public @ResponseBody Map<String, String> logout() {
        Subject currentUser = SecurityUtils.getSubject();
        logger.info("Attempting to logout user %s", currentUser);
        currentUser.logout();
        logger.info(String.format("Successfully logged out User %s", currentUser));
        return new HashMap<String, String>(){{
            this.put("status", "OK");
        }};
    }


}
