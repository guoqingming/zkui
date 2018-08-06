/**
 * Copyright (c) 2014, Deem Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.deem.zkui.controller;

import com.deem.zkui.service.HistoriesService;
import com.deem.zkui.utils.CuratorUtil;
import com.deem.zkui.utils.ServletUtil;
import com.deem.zkui.vo.LeafBean;
import com.deem.zkui.vo.ZKNode;
import freemarker.template.TemplateException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class Home {

    private final static Logger logger = LoggerFactory.getLogger(Home.class);

    @Autowired
    private HistoriesService historiesService;

    @Autowired
    private CuratorFramework client;

    @GetMapping("/home")
    protected String homeGet(HttpServletRequest request, Model model) throws Exception {
        logger.debug("Home Get Action!");

        Map<String, Object> templateParam = new HashMap<>();
        String zkPath = request.getParameter("zkPath");
        String navigate = request.getParameter("navigate");
        ZooKeeper zk = client.getZookeeperClient().getZooKeeper();
        List<String> nodeLst;
        List<LeafBean> leafLst;
        String currentPath, parentPath, displayPath;



        if (zkPath == null || zkPath.equals("/")) {
            templateParam.put("zkpath", "/");
            ZKNode zkNode = CuratorUtil.listNodeEntries( "/");
            nodeLst = zkNode.getNodeLst();
            leafLst = zkNode.getLeafBeanLSt();
            currentPath = "/";
            displayPath = "/";
            parentPath = "/";
        } else {
            templateParam.put("zkPath", zkPath);
            ZKNode zkNode = CuratorUtil.listNodeEntries( zkPath);
            nodeLst = zkNode.getNodeLst();
            leafLst = zkNode.getLeafBeanLSt();
            currentPath = zkPath + "/";
            displayPath = zkPath;
            parentPath = zkPath.substring(0, zkPath.lastIndexOf("/"));
            if (parentPath.equals("")) {
                parentPath = "/";
            }
        }

        templateParam.put("displayPath", displayPath);
        templateParam.put("parentPath", parentPath);
        templateParam.put("currentPath", currentPath);
        templateParam.put("nodeLst", nodeLst);
        templateParam.put("leafLst", leafLst);
        templateParam.put("breadCrumbLst", displayPath.split("/"));
        templateParam.put("navigate", navigate);

        model.addAllAttributes(templateParam);
        return "home";


    }

    @PostMapping("/home")
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.debug("Home Post Action!");
        try {

            Map<String, Object> templateParam = new HashMap<>();
            String action = request.getParameter("action");
            String currentPath = request.getParameter("currentPath");
            String displayPath = request.getParameter("displayPath");
            String newProperty = request.getParameter("newProperty");
            String newValue = request.getParameter("newValue");
            String newNode = request.getParameter("newNode");

            String[] nodeChkGroup = request.getParameterValues("nodeChkGroup");
            String[] propChkGroup = request.getParameterValues("propChkGroup");

            String searchStr = request.getParameter("searchStr").trim();
            String authRole = (String) request.getSession().getAttribute("authRole");

            switch (action) {
                case "Save Node":
                    if (!newNode.equals("") && !currentPath.equals("")) {
                        //Save the new node.
                        CuratorUtil.createFolder(currentPath + newNode, "foo", "bar");
                        request.getSession().setAttribute("flashMsg", "Node created!");
                        historiesService.insertHistory((String) request.getSession().getAttribute("authName"), request.getRemoteAddr(), "Creating node: " + currentPath + newNode);
                    }
                    response.sendRedirect("/home?zkPath=" + displayPath);
                    break;
                case "Save Property":
                    if (!newProperty.equals("") && !currentPath.equals("")) {
                        //Save the new node.
                        CuratorUtil.createNode(currentPath, newProperty, newValue);
                        request.getSession().setAttribute("flashMsg", "Property Saved!");
                        if (CuratorUtil.checkIfPwdField(newProperty)) {
                            newValue = CuratorUtil.INSTANCE.SOPA_PIPA;
                        }
                        historiesService.insertHistory((String) request.getSession().getAttribute("authName"), request.getRemoteAddr(), "Saving Property: " + currentPath + "," + newProperty + "=" + newValue);
                    }
                    response.sendRedirect("/home?zkPath=" + displayPath);
                    break;
                case "Update Property":
                    if (!newProperty.equals("") && !currentPath.equals("") ) {
                        //Save the new node.
                        CuratorUtil.setPropertyValue(currentPath, newProperty, newValue);
                        request.getSession().setAttribute("flashMsg", "Property Updated!");
                        if (CuratorUtil.checkIfPwdField(newProperty)) {
                            newValue = CuratorUtil.INSTANCE.SOPA_PIPA;
                        }
                        historiesService.insertHistory((String) request.getSession().getAttribute("authName"), request.getRemoteAddr(), "Updating Property: " + currentPath + "," + newProperty + "=" + newValue);
                    }
                    response.sendRedirect("/home?zkPath=" + displayPath);
                    break;
                case "Search":
                    Set<LeafBean> searchResult = CuratorUtil.searchTree(searchStr);
                    templateParam.put("searchResult", searchResult);

                    ServletUtil.INSTANCE.renderHtml(request, response, templateParam, "search.ftl.html");
                    break;
                case "Delete":
                    if (propChkGroup != null) {
                        for (String prop : propChkGroup) {
                            List delPropLst = Arrays.asList(prop);
                            CuratorUtil.deleteLeaves(delPropLst);
                            request.getSession().setAttribute("flashMsg", "Delete Completed!");
                            historiesService.insertHistory((String) request.getSession().getAttribute("authName"), request.getRemoteAddr(), "Deleting Property: " + delPropLst.toString());
                        }
                    }
                    if (nodeChkGroup != null) {
                        for (String node : nodeChkGroup) {
                            List delNodeLst = Arrays.asList(node);
                            CuratorUtil.deleteFolders(delNodeLst);
                            request.getSession().setAttribute("flashMsg", "Delete Completed!");
                            historiesService.insertHistory((String) request.getSession().getAttribute("authName"), request.getRemoteAddr(), "Deleting Nodes: " + delNodeLst.toString());
                        }
                    }

                    response.sendRedirect("/home?zkPath=" + displayPath);
                    break;
                default:
                    response.sendRedirect("/home");
            }

        } catch (InterruptedException | TemplateException | KeeperException ex) {
            logger.error(Arrays.toString(ex.getStackTrace()));
            ServletUtil.INSTANCE.renderError(request, response, ex.getMessage());
        }
    }
}
