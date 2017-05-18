package com.uptolearn.wss.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.uptolearn.wss.web.model.User;
import com.uptolearn.wss.web.security.PermissionSign;
import com.uptolearn.wss.web.security.RoleSign;
import com.uptolearn.wss.web.service.UserService;

/**
 * 用户控制器
 * 
 * @author StarZou
 * @since 2014年5月28日 下午3:54:00
 **/
@Controller
@RequestMapping(value = "/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户登录
     * 
     * @param user
     * @param result
     * @return */
    
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public JSON login(@Valid User user, BindingResult result, Model model, HttpServletRequest request) {
    	Map<String,Object> login_result = new HashMap<String,Object>(); 
    	try {
            Subject subject = SecurityUtils.getSubject();
            // 已登陆则跳到首页
            if (subject.isAuthenticated()) {
            	login_result.put("rspcod","LOGSUC");
            	login_result.put("rspmsg","已登录");
            	login_result.put("href","rest/page/index");
            	return (JSON)JSON.toJSON(login_result);
            }
            
            if (result.hasErrors()) {
            	login_result.put("rspcod","ERROR");
            	login_result.put("rspmsg","参数错误");
            	return (JSON)JSON.toJSON(login_result);
            }
            
            // 身份验证
            subject.login(new UsernamePasswordToken(user.getUsername(), user.getPassword()));
            // 验证成功在Session中保存用户信息
            final User authUserInfo = userService.selectByUsername(user.getUsername());
            request.getSession().setAttribute("userInfo", authUserInfo);
        } catch (AuthenticationException e) {
            // 身份验证失败
        	login_result.put("rspcod","ERROR");
        	login_result.put("rspmsg","用户名或密码错误 ！");
        	return (JSON)JSON.toJSON(login_result);
        }
        
    	
    	login_result.put("rspcod","LOGSUC");
    	login_result.put("rspmsg","已登录");
    	login_result.put("href","rest/page/index");
    	return (JSON)JSON.toJSON(login_result);
        
    }
    
    /**
     * 用户登录json
     * 
     * @param user
     * @param result
     * @return
    
    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String login(@Valid User user, BindingResult result, Model model, HttpServletRequest request,HttpServletResponse response) {
    	System.out.println(user);
    	return "{\"rspcod\":\"000000\",\"rspmsg\":\"登录成功\"}";
    } */

    /**
     * 用户登出
     * 
     * @param session
     * @return
     */
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpSession session) {
        session.removeAttribute("userInfo");
        // 登出操作
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        return "login";
    }

    /**
     * 基于角色 标识的权限控制案例
     */
    @RequestMapping(value = "/admin")
    @ResponseBody
    @RequiresRoles(value = RoleSign.ADMIN)
    public String admin() {
        return "拥有admin角色,能访问";
    }

    /**
     * 基于权限标识的权限控制案例
     */
    @RequestMapping(value = "/create")
    @ResponseBody
    @RequiresPermissions(value = PermissionSign.USER_CREATE)
    public String create() {
        return "拥有user:create权限,能访问";
    }
}
