package com.lin.controller.portal;


import com.lin.common.Constant;
import com.lin.common.ResponseCodeEnum;
import com.lin.common.ServerResponse;
import com.lin.pojo.User;
import com.lin.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/user/")
public class UserController {

    @Autowired
    private IUserService iUserService;

    /**
     * 思路：
     * 1、 Controller ---》 service ---》 serviceImpl ---》dao ---》 xml entity
     * 2、封装一个高复用的统一返回对象类ServerResponse
     * 3、对应的复用ServerResponse返回对象相关的ResponseCode枚举类，用于统一化处理返回状态码
     *
     */
    /**
     *  用户登录 ok
     * @param username
     * @param password
     * @param session(登录传入session？)
     * @return
     */
    @RequestMapping(value = "login.do",method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody//将响应对象自动序列化成json
    public ServerResponse<User> login(String username, String password, HttpSession session){
        ServerResponse<User> response =  iUserService.login(username,password);
        if(response.isSuccess()){
            //只用将用户信息存入session
            session.setAttribute(Constant.CURRENT_USER,response.getData());
            //session.setAttribute("user",response.getData());//这里可以使用常量来作为key使用
        }
        return response;
    }

    /**
     * 登出 ok
     * @param session
     * @return
     */
    @RequestMapping(value="logout.do", method = {RequestMethod.POST})
    @ResponseBody
    public ServerResponse<String> logOut(HttpSession session){
        session.removeAttribute(Constant.CURRENT_USER);
        return ServerResponse.createBySuccess();
    }

    /**
     * 注册 ok
     * @param user
     * @return
     */
    @RequestMapping(value = "register.do", method = {RequestMethod.POST})
    @ResponseBody
    public ServerResponse<String> register(User user){
        return iUserService.register(user);
    }

    /**
     * 实时调用验证用户名或者邮箱接口 ok
     * @param str 验证的内容
     * @param type 验证的类型
     * @return
     */
    @RequestMapping(value = "checkValid.do",method = {RequestMethod.POST})
    @ResponseBody
    public ServerResponse<String> checkValid(String str ,String type){
        return iUserService.checkValid(str,type);
    }

    /**
     * 获取用户信息接口 ok
     * @param session
     * @return
     */
    @RequestMapping(value = "getUserInfo.do",method = {RequestMethod.POST})
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session){
        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByError(ResponseCodeEnum.NEED_LOGIN.getCode(),"用户未登录，无法获取用户信息");
        }
        return ServerResponse.createBySuccess(user);
    }

    /**用户忘记密码，选择问题方式重置密码(后面可以修改一下随机获取一个问题，返回到前端进行验证)
     * 获取用户忘记密码的问题接口 ok
     * @param username
     * @return
     */
    @RequestMapping(value = "getQuestion.do",method = {RequestMethod.POST})
    @ResponseBody
    public ServerResponse<String> getQuestion(String username){
        return  iUserService.getQuestion(username);
    }

    /**
     * 校验问题答案（校验成功之后，使用guava缓存存储token） ok
     * @param username
     * @param question
     * @param answer
     * @return 如果问题回答正确，返回一个带有时效性的token
     */
    @RequestMapping(value = "checkForgetAnswer.do",method = {RequestMethod.POST})
    @ResponseBody
    public ServerResponse<String> checkForgetAnswer(String username,String question,String answer){
        return  iUserService.checkForgetAnswer(username,question,answer);
    }

    /**
     * 忘记密码中的重置密码 ok
     * @param username 重置的用户名
     * @param newPassword 重置的新密码
     * @param forgetToken 回答问题之后返回到前端的token 钥匙
     * @return
     */
    @RequestMapping(value = "resetPassword.do",method = {RequestMethod.POST})
    @ResponseBody
    public ServerResponse<String> resetPassword(String username, String newPassword ,String forgetToken){
        //return iUserService.resetPassword1(username, newPassword, forgetToken);//重置密码思路1
        return iUserService.resetPassword2(username, newPassword, forgetToken);//重置密码思路2
    }

    /**
     * 登录状态下的修改密码
     * @param session session 中获取用户
     * @param passwordOld  passwordOld密码和用户做匹配，防止横向越权
     * @param passwordNew  新密码
     * @return
     */
    @RequestMapping(value = "updatePassword.do",method = {RequestMethod.POST})
    @ResponseBody
    public ServerResponse<String>  updatePassword(HttpSession session, String passwordOld, String passwordNew){
        User user = (User) session.getAttribute(Constant.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByError(ResponseCodeEnum.NEED_LOGIN.getCode(),ResponseCodeEnum.NEED_LOGIN.getProductDesc());
        }
        return iUserService.updatePassword(user, passwordOld, passwordNew);
    }

    /**
     * 修改用户信息
     * @param session
     * @param user
     * @return
     */
    @RequestMapping(value = "updateUserMassage.do",method = {RequestMethod.POST})
    @ResponseBody
    public ServerResponse<User> updateUserMassage(HttpSession session, User user){
        //1、判断用户是否登录
       User currentUser = (User) session.getAttribute(Constant.CURRENT_USER);
       if(currentUser == null){
           return ServerResponse.createByError(ResponseCodeEnum.NEED_LOGIN.getCode(),ResponseCodeEnum.NEED_LOGIN.getProductDesc());
       }
       user.setId(currentUser.getId());
       ServerResponse<User> response =   iUserService.updateUser(user);
       if(response.isSuccess()){
           session.setAttribute(Constant.CURRENT_USER,response.getData());
       }
        return response;
    }

}
