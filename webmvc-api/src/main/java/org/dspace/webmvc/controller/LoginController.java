/**
 * $Id: $
 * $URL: $
 * *************************************************************************
 * Copyright (c) 2002-2009, DuraSpace.  All rights reserved
 * Licensed under the DuraSpace License.
 *
 * A copy of the DuraSpace License has been included in this
 * distribution and is available at: http://scm.dspace.org/svn/repo/licenses/LICENSE.txt
 */

package org.dspace.webmvc.controller;

import org.apache.commons.lang.StringUtils;
import org.dspace.authenticate.AuthenticationManager;
import org.dspace.authenticate.AuthenticationMethod;
import org.dspace.core.Context;
import org.dspace.webmvc.bind.annotation.RequestAttribute;
import org.dspace.webmvc.model.login.HttpLoginService;
import org.dspace.webmvc.model.login.LoginService;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Controller
//@RequestMapping("/login")
public class LoginController {
    @ModelAttribute("loginForm")
    public LoginForm createForm() {
        return new LoginForm();
    }

    @ModelAttribute("loginService")
    public LoginService createService(HttpServletRequest request) {
        return new HttpLoginService(request);
    }

    @RequestMapping
    public String showForm(LoginForm loginForm) {
        return "pages/login";
    }

    /**
     * Method to authenticate the user credentials supplied in loginForm.
     * <p/>
     * Note that the order of parameters is important - the BindingResult must immediately follow the model attribute
     * being validated.
     *
     * @param context
     * @param loginService
     * @param loginForm
     * @param bindingResult
     * @return
     */
    @RequestMapping(params = "submit")
    public String processForm(@RequestAttribute Context context, LoginService loginService, @Valid LoginForm loginForm, BindingResult bindingResult) {
        if (!bindingResult.hasErrors()) {
            int status = AuthenticationManager.authenticate(context, loginForm.getEmail(), loginForm.getPassword(), null, null /*request*/);
            if (status == AuthenticationMethod.SUCCESS) {
                loginService.createUserSession(context, context.getCurrentUser());
                String redirectUrl = loginService.getInterruptedRequestURL();
                return "redirect:" + (StringUtils.isEmpty(redirectUrl) ? "/" : redirectUrl);
            } else {
                bindingResult.addError(new ObjectError("loginForm", new String[] {"InvalidPassword.loginForm", "InvalidPassword"}, null /* arguments */, "default message"));
            }
        }

        return "pages/login";
    }

    @RequestMapping("/logout/**")
    protected String logOut(@RequestAttribute Context context, LoginService loginService) {
        loginService.removeUserSession(context);

        return "forward:/";
    }

    public static class LoginForm {
        @NotEmpty
        @Email
        private String email;

        @NotEmpty
        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
