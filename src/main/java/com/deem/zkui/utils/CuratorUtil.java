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
package com.deem.zkui.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.deem.zkui.vo.LeafBean;
import com.deem.zkui.vo.ZKNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;

public enum CuratorUtil {

    INSTANCE;
    public final static Integer MAX_CONNECT_ATTEMPT = 5;
    public final static String ZK_ROOT_NODE = "/";
    public final static String ZK_SYSTEM_NODE = "zookeeper"; // ZK internal folder (quota info, etc) - have to stay away from it
    public final static String ZK_HOSTS = "/appconfig/hosts";
    public final static String ROLE_USER = "USER";
    public final static String ROLE_ADMIN = "ADMIN";
    public final static String SOPA_PIPA = "SOPA/PIPA BLACKLISTED VALUE";


    private static CuratorFramework client = SpringUtils.getBean(CuratorFramework.class);


    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(CuratorUtil.class);



    private ArrayList<ACL> defaultAcl = ZooDefs.Ids.OPEN_ACL_UNSAFE;

    private ArrayList<ACL> defaultAcl() {
        return defaultAcl;
    }


    public static Set<LeafBean> searchTree(String searchString) throws Exception {
        //Export all nodes and then search.
        Set<LeafBean> searchResult = new TreeSet<>();
        Set<LeafBean> leaves = new TreeSet<>();
        exportTreeInternal(leaves, ZK_ROOT_NODE);
        for (LeafBean leaf : leaves) {
            String leafValue = ServletUtil.INSTANCE.externalizeNodeValue(leaf.getValue());
            if (leaf.getPath().contains(searchString) || leaf.getName().contains(searchString) || leafValue.contains(searchString)) {
                searchResult.add(leaf);
            }
        }
        return searchResult;

    }

    public static Set<LeafBean> exportTree(String zkPath) throws Exception {
        // 1. Collect nodes
        long startTime = System.currentTimeMillis();
        Set<LeafBean> leaves = new TreeSet<>();
        exportTreeInternal(leaves, zkPath);
        long estimatedTime = System.currentTimeMillis() - startTime;
        logger.trace("Elapsed Time in Secs for Export: " + estimatedTime / 1000);
        return leaves;
    }

    private static void exportTreeInternal(Set<LeafBean> entries, String path) throws Exception {
        // 1. List leaves
        entries.addAll(listLeaves( path));
        // 2. Process folders
        for (String folder : listFolders( path)) {
            exportTreeInternal(entries, getNodePath(path, folder));
        }
    }

    public static void importData(List<String> importFile,boolean overwrite) throws Exception {

        for (String line : importFile) {
            logger.debug("Importing line " + line);
            // Delete Operation
            if (line.startsWith("-")) {
                String nodeToDelete = line.substring(1);
                deleteNodeIfExists(nodeToDelete);
            } else {
                int firstEq = line.indexOf('=');
                int secEq = line.indexOf('=', firstEq + 1);

                String path = line.substring(0, firstEq);
                if ("/".equals(path)) {
                    path = "";
                }
                String name = line.substring(firstEq + 1, secEq);
                String value = readExternalizedNodeValue(line.substring(secEq + 1));
                String fullNodePath = path + "/" + name;

                // Skip import of system node
                if (fullNodePath.startsWith(ZK_SYSTEM_NODE)) {
                    logger.debug("Skipping System Node Import: " + fullNodePath);
                    continue;
                }
                boolean nodeExists = nodeExists(fullNodePath);

                if (!nodeExists) {
                    //If node doesnt exist then create it.
                    createPathAndNode(path, name, value.getBytes());
                } else {
                    //If node exists then update only if overwrite flag is set.
                    if (overwrite) {
                        setPropertyValue(path + "/", name, value);
                    } else {
                        logger.info("Skipping update for existing property " + path + "/" + name + " as overwrite is not enabled!");
                    }
                }

            }

        }
    }

    private static String readExternalizedNodeValue(String raw) {
        return raw.replaceAll("\\\\n", "\n");
    }

    private static void createPathAndNode(String path, String name, byte[] data) throws Exception {
        // 1. Create path nodes if necessary
        StringBuilder currPath = new StringBuilder();
        for (String folder : path.split("/")) {
            if (folder.length() == 0) {
                continue;
            }
            currPath.append('/');
            currPath.append(folder);
            Stat stat = client.checkExists().forPath(currPath.toString());
            if (stat==null) {
                createIfDoesntExist(currPath.toString(), new byte[0]);
            }
        }

        // 2. Create leaf node
        createIfDoesntExist(path + '/' + name, data);
    }

    private static void createIfDoesntExist(String path, byte[] data) throws Exception {
        client.create().forPath(path,data);
    }

    public static ZKNode listNodeEntries( String path) throws Exception {
        List<String> folders = new ArrayList<>();
        List<LeafBean> leaves = new ArrayList<>();

        List<String> children = client.getChildren().forPath(path);
        if (children != null) {
            for (String child : children) {
                if (!child.equals(ZK_SYSTEM_NODE)) {

                    List<String> subChildren = client.getChildren().forPath(path + ("/".equals(path) ? "" : "/") + child);
                    boolean isFolder = subChildren != null && !subChildren.isEmpty();
                    if (isFolder) {
                        folders.add(child);
                    } else {
                        String childPath = getNodePath(path, child);
                        leaves.add(getNodeValue( path, childPath, child));
                    }

                }

            }
        }

        Collections.sort(folders);
        Collections.sort(leaves, new Comparator<LeafBean>() {
            @Override
            public int compare(LeafBean o1, LeafBean o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        ZKNode zkNode = new ZKNode();
        zkNode.setLeafBeanLSt(leaves);
        zkNode.setNodeLst(folders);
        return zkNode;
    }

//    @Deprecated
    public static List<LeafBean> listLeaves( String path) throws Exception {
        List<LeafBean> leaves = new ArrayList<>();

        List<String> children = client.getChildren().forPath(path);
//        List<String> children = zk.getChildren(path, false);
        if (children != null) {
            for (String child : children) {
                String childPath = getNodePath(path, child);
                List<String> subChildren = Collections.emptyList();
                subChildren = client.getChildren().forPath(childPath);
                boolean isFolder = subChildren != null && !subChildren.isEmpty();
                if (!isFolder) {
                    leaves.add(getNodeValue( path, childPath, child));
                }
            }
        }

        Collections.sort(leaves, new Comparator<LeafBean>() {
            @Override
            public int compare(LeafBean o1, LeafBean o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        return leaves;
    }

//    @Deprecated
    public static List<String> listFolders(String path) throws Exception {
        List<String> folders = new ArrayList<>();
        List<String> children = client.getChildren().forPath(path);
        if (children != null) {
            for (String child : children) {
                if (!child.equals(ZK_SYSTEM_NODE)) {
//                    List<String> subChildren = zk.getChildren(path + ("/".equals(path) ? "" : "/") + child, false);
                    List<String> subChildren = client.getChildren().forPath(path + ("/".equals(path) ? "" : "/")+child);
                    boolean isFolder =CollectionUtil.isNotEmpty(subChildren);
                    if (isFolder) {
                        folders.add(child);
                    }
                }

            }
        }

        Collections.sort(folders);
        return folders;
    }

    public static String getNodePath(String path, String name) {
        return path + ("/".equals(path) ? "" : "/") + name;

    }

    public static LeafBean getNodeValue( String path, String childPath, String child) throws Exception {
        try {
            logger.trace("Lookup: path=" + path + ",childPath=" + childPath + ",child=" + child );
//            byte[] dataBytes = zk.getData(childPath, false, new Stat());
            byte[] dataBytes = client.getData().forPath(childPath);
            if (checkIfPwdField(child)) {
                    return (new LeafBean(path, child, SOPA_PIPA.getBytes()));
                } else {
                    return (new LeafBean(path, child, dataBytes));
                }
        } catch (KeeperException | InterruptedException ex) {
            logger.error(ex.getMessage());
        }
        return null;

    }

    public static Boolean checkIfPwdField(String property) {
        if (property.contains("PWD") || property.contains("pwd") || property.contains("PASSWORD") || property.contains("password") || property.contains("PASSWD") || property.contains("passwd")) {
            return true;
        } else {
            return false;
        }
    }

    public static void createNode(String path, String name, String value) throws Exception {
        String nodePath = path + name;
        logger.debug("Creating node " + nodePath + " with value " + value);
        client.create().forPath(nodePath,StringUtils.isBlank(value)?null : value.getBytes());

    }

    public static void createFolder(String folderPath, String propertyName, String propertyValue) throws Exception {

        logger.debug("Creating folder " + folderPath + " with property " + propertyName + " and value " + propertyValue);
        client.create().forPath(folderPath);
        client.create().forPath(folderPath + "/" + propertyName, StringUtils.isBlank(propertyValue) ? null : propertyValue.getBytes());

    }

    public static void setPropertyValue(String path, String name, String value) throws Exception {
        String nodePath = path + name;
        logger.debug("Setting property " + nodePath + " to " + value);
        client.setData().forPath(nodePath, value.getBytes());

    }

    public static boolean nodeExists(String nodeFullPath) throws Exception {
        logger.trace("Checking if exists: " + nodeFullPath);
        return client.checkExists().forPath(nodeFullPath) != null;
    }

    public static void deleteFolders(List<String> folderNames) throws Exception {

        for (String folderPath : folderNames) {
            deleteFolderInternal(folderPath);
        }

    }

    private static void deleteFolderInternal(String folderPath) throws Exception {

        logger.debug("Deleting folder " + folderPath);
        for (String child : client.getChildren().forPath(folderPath)) {
            deleteFolderInternal(getNodePath(folderPath, child));
        }
        client.delete().forPath(folderPath);
    }

    public static void deleteLeaves(List<String> leafNames) throws Exception {

        for (String leafPath : leafNames) {
            logger.debug("Deleting leaf " + leafPath);
            client.delete().forPath(leafPath);
        }
    }

    private static void deleteNodeIfExists(String path) throws Exception {
        client.delete().forPath(path);
    }

    public void closeZooKeeper(ZooKeeper zk) throws InterruptedException {
        logger.trace("Closing ZooKeeper");
        if (zk != null) {
            zk.close();
            logger.trace("Closed ZooKeeper");

        }
    }
}
