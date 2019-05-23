package com.cxxy.qiu.gradudes.web.local;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cxxy.qiu.gradudes.dto.LocalAuthExecution;
import com.cxxy.qiu.gradudes.entity.LocalAuth;
import com.cxxy.qiu.gradudes.entity.PersonInfo;
import com.cxxy.qiu.gradudes.enums.LocalAuthStateEnum;
import com.cxxy.qiu.gradudes.service.LocalAuthService;
import com.cxxy.qiu.gradudes.util.CodeUtil;
import com.cxxy.qiu.gradudes.util.HttpServletRequestUtil;

@Controller
@RequestMapping(value = "/local", method = { RequestMethod.GET,
		RequestMethod.POST })
public class LocalAuthController {

	@Autowired
	private LocalAuthService localAuthService;

	/*
	 * 绑定帐号
	 */
	@RequestMapping(value = "/bindlocalauth", method = RequestMethod.POST)
	@ResponseBody
	private Map<String, Object> bindLocalAuth(HttpServletRequest request) {
		Map<String, Object> modelMap = new HashMap<String, Object>();
		// 验证码校验
		if (!CodeUtil.checkVerifyCode(request)) {
			modelMap.put("success", false);
			modelMap.put("errMsg", "输入了错误的验证码");
			return modelMap;
		}
		// 获取输入的帐号
		String userName = HttpServletRequestUtil.getString(request, "userName");
		// 获取输入的密码
		String password = HttpServletRequestUtil.getString(request, "password");
		// 从session中获取当前用户信息（用户一旦通过微信登录之后，便能获取到用户信息）
		PersonInfo user = (PersonInfo) request.getSession()
				.getAttribute("user");
		// 非空判断，要求帐号密码以及当前的用户session非空
		if (userName != null && password != null && user != null
				&& user.getUserId() != null) {
			// 创建LocalAuth对象并赋值
			LocalAuth localAuth = new LocalAuth();
			localAuth.setUserName(userName);
			localAuth.setPassword(password);
			localAuth.setPersonInfo(user);

			// 绑定帐号
			LocalAuthExecution lae = localAuthService.bindLocalAuth(localAuth);
			if (lae.getState() == LocalAuthStateEnum.SUCCESS.getState()) {
				modelMap.put("success", true);
			} else {
				modelMap.put("success", false);
				modelMap.put("errMsg", lae.getStateInfo());
			}

		} else {
			modelMap.put("success", false);
			modelMap.put("errMsg", "用户名和密码均不能为空");
		}
		return modelMap;
	}

	@RequestMapping(value = "/changelocalpwd", method = RequestMethod.POST)
	@ResponseBody
	private Map<String, Object> changeLocalPwd(HttpServletRequest request) {
		Map<String, Object> modelMap = new HashMap<String, Object>();
		// 验证码校验
		if (!CodeUtil.checkVerifyCode(request)) {
			modelMap.put("success", false);
			modelMap.put("errMsg", "输入了错误的验证码");
			return modelMap;
		}
		// 获取帐号
		String userName = HttpServletRequestUtil.getString(request, "userName");
		// 获取原密码
		String password = HttpServletRequestUtil.getString(request, "password");
		// 获取新密码
		String newPassword = HttpServletRequestUtil.getString(request, "newPassword");
		// 从session中获取当前用户信息（用户一旦通过微信登录之后，便能获取到用户信息）
		PersonInfo user = (PersonInfo) request.getSession().getAttribute("user");
				// 非空判断，要求帐号密码以及当前的用户session非空
				if (userName != null && password != null && user != null
						&& user.getUserId() != null&&!password.equals(newPassword)) {
					try{
						//查看原先帐号，看看与输入的帐号是否一致，不一致则认为是非法操作
						LocalAuth localAuth=localAuthService.getLocalAuthByUserId(user.getUserId());
						if(localAuth==null||!localAuth.getUserName().equals(userName)){
							//不一致则直接退出
							modelMap.put("success", false);
							modelMap.put("errMsg", "输入的帐号非本次登录的帐号");
							return modelMap;
						}
						//修改平台帐号的用户密码
						LocalAuthExecution lae=localAuthService.modifyLocalAuth(user.getUserId(), userName, password, newPassword);
						if(lae.getState()==LocalAuthStateEnum.SUCCESS.getState()){
							modelMap.put("success", true);
						}else{
							modelMap.put("success", false);
							modelMap.put("errMsg", lae.getStateInfo());
						}			
					}catch(Exception e){
						modelMap.put("success", false);
						modelMap.put("errMsg", e.toString());
						return modelMap;
					}
				}else{
					modelMap.put("success", false);
					modelMap.put("errMsg", "请输入密码");
					
				}
		return modelMap;
	}
	@RequestMapping(value = "/logincheck", method = RequestMethod.POST)
	@ResponseBody
	private Map<String, Object> logincheck(HttpServletRequest request) {
		Map<String, Object> modelMap = new HashMap<String, Object>();
	    //获取是否需要进行验证码校验的标识
		boolean needVerify=HttpServletRequestUtil.getBoolean(request, "needVerify");
		if (needVerify&&!CodeUtil.checkVerifyCode(request)) {
			modelMap.put("success", false);
			modelMap.put("errMsg", "输入了错误的验证码");
			return modelMap;
		}
		        // 获取输入的帐号
				String userName = HttpServletRequestUtil.getString(request, "userName");
				// 获取输入的密码
				String password = HttpServletRequestUtil.getString(request, "password");
			    //非空校验
				if(userName!=null&&password!=null){
					//传入帐号和密码获取平台帐号信息
					LocalAuth localAuth=localAuthService.getLocalAuthByUserNameAndPwd(userName, password);
					if(localAuth!=null){
					//若能取到帐号信息则登录成功
						modelMap.put("success", true);
					//同时在session里设置用户信息
						request.getSession().setAttribute("user", localAuth.getPersonInfo());			
					}else{
						modelMap.put("success", false);
						modelMap.put("errMsg", "用户名和密码均错误");
					}					
				}else{
					modelMap.put("success", false);
					modelMap.put("errMsg", "用户名和密码均错误");
				}
		return modelMap;	
	}
	
	/*
	 * 当用户点击登出按钮的时候注销session
	 * */
	@RequestMapping(value = "/loginout", method = RequestMethod.POST)
	@ResponseBody
	private Map<String, Object> loginout(HttpServletRequest request) {
		Map<String, Object> modelMap = new HashMap<String, Object>();
		//将用户session置为空
		request.getSession().setAttribute("user", null);
		modelMap.put("success", true);
		return modelMap;
	}
	
}
