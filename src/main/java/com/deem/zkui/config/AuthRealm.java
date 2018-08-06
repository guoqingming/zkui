package com.deem.zkui.config;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import sun.net.www.protocol.http.AuthenticationInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: zkui1
 * @description:
 * @author: guoqingming
 * @create: 2018-08-04 18:18
 **/
//public class AuthRealm extends AuthorizingRealm {
//    @Autowired
//    private UserService userService;
//
//    //认证.登录
//    @Override
//    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
//        UsernamePasswordToken utoken=(UsernamePasswordToken) token;//获取用户输入的token
//        String username = utoken.getUsername();
//        User user = userService.findUserByUserName(username);
//        return new SimpleAuthenticationInfo(user, user.getPassword(),this.getClass().getName());//放入shiro.调用CredentialsMatcher检验密码
//    }
//    //授权
//    @Override
//    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principal) {
//        User user=(User) principal.fromRealm(this.getClass().getName()).iterator().next();//获取session中的用户
//        List<String> permissions=new ArrayList<>();
//        Set<Role> roles = user.getRoles();
//        if(roles.size()>0) {
//            for(Role role : roles) {
//                Set<Module> modules = role.getModules();
//                if(modules.size()>0) {
//                    for(Module module : modules) {
//                        permissions.add(module.getMname());
//                    }
//                }
//            }
//        }
//        SimpleAuthorizationInfo info=new SimpleAuthorizationInfo();
//        info.addStringPermissions(permissions);//将权限放入shiro中.
//        return info;
//    }

//}