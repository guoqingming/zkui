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

import java.io.PrintWriter;
import java.util.Date;
import java.util.Set;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.deem.zkui.utils.CuratorUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.KeeperException;
import com.deem.zkui.utils.ServletUtil;
import com.deem.zkui.utils.Z1ooKeeperUtil;
import com.deem.zkui.vo.LeafBean;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Export extends HttpServlet {

    private final static Logger logger = LoggerFactory.getLogger(Export.class);


    @Autowired
    private CuratorFramework client;

    @GetMapping("/export")
    protected void export(HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.debug("Export Get Action!");
        try {

            String zkPath = request.getParameter("zkPath");
            StringBuilder output = new StringBuilder();
            output.append("#App Config Dashboard (ACD) dump created on :").append(new Date()).append("\n");
            Set<LeafBean> leaves = CuratorUtil.exportTree(zkPath);
            for (LeafBean leaf : leaves) {
                output.append(leaf.getPath()).append('=').append(leaf.getName()).append('=').append(ServletUtil.INSTANCE.externalizeNodeValue(leaf.getValue())).append('\n');
            }// for all leaves
            response.setContentType("text/plain;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.write(output.toString());
            }

        } catch (InterruptedException | KeeperException ex) {
            logger.error(Arrays.toString(ex.getStackTrace()));
            ServletUtil.INSTANCE.renderError(request, response, ex.getMessage());
        }
    }
}
