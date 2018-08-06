/**
 *
 * Copyright (c) 2014, Deem Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.deem.zkui.controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.deem.zkui.utils.Z1ooKeeperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class Login  {

    private final static Logger logger = LoggerFactory.getLogger(Login.class);

    @GetMapping("/login")
    protected String doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        return "login";
    }

//    @PostMapping("/login")
//    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        logger.debug("Login Post Action!");
//            HttpSession session = request.getSession(true);
//            String username = request.getParameter("username");
//            String password = request.getParameter("password");
//           if("admin".equals(username) && "admin".equals(password)){
//
//               session.setAttribute("authName", username);
//               session.setAttribute("authRole", Z1ooKeeperUtil.ROLE_ADMIN);
//               response.sendRedirect("/home");
//           }else {
//               response.sendRedirect("/login");
//           }
//
//
//
//    }
}
