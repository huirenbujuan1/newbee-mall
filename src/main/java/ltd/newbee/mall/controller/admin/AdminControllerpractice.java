///**
// * 严肃声明：
// * 开源版本请务必保留此注释头信息，若删除我方将保留所有法律责任追究！
// * 本系统已申请软件著作权，受国家版权局知识产权以及国家计算机软件著作权保护！
// * 可正常分享和学习源码，不得用于违法犯罪活动，违者必究！
// * Copyright (c) 2019-2020 十三 all rights reserved.
// * 版权所有，侵权必究！
// */
//package ltd.newbee.mall.controller.admin;
//
//import cn.hutool.captcha.ShearCaptcha;
//import com.sun.net.httpserver.Authenticator;
//import ltd.newbee.mall.common.ServiceResultEnum;
//import ltd.newbee.mall.entity.AdminUser;
//import ltd.newbee.mall.service.AdminUserService;
//
//import org.apache.ibatis.annotations.Param;
//import org.springframework.stereotype.Controller;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;
//import org.springframework.web.bind.annotation.*;
//
//import javax.annotation.Resource;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpSession;
//
///**
// * @author 13
// * @qq交流群 796794009
// * @email 2449207463@qq.com
// * @link https://github.com/newbee-ltd
// */
//@Controller
//@RequestMapping("/admin")
//public class AdminControllerpractice {
//
//    @Resource
//    private AdminUserService adminUserService;
//
//    @GetMapping({"/login"})
//    public String login() {
//        return "admin/login";
//    }
//
//    @GetMapping({"/test"})
//    public String test() {
//        return "admin/test";
//    }
//
//
//    @GetMapping({"", "/", "/index", "/index.html"})
//    public String index(HttpServletRequest request) {
//        request.setAttribute("path", "index");
//        return "admin/index";
//    }
//
//    //登录页面 → GET /admin/login 返回登录表单
//    @PostMapping("/admin/login")
//    public String login(@RequestParam("userName") String userName,
//                        @RequestParam("password") String password,
//                        @RequestParam("verifyCode") String verifyCode,HttpSession session) {
//        if(StringUtils.hasText(verifyCode)){
//            session.setAttribute("errormsg","验证码不能为空");
//            return "admin/login";
//        }
//
//        if(StringUtils.hasText(userName) || StringUtils.hasText(password)){
//            session.setAttribute("errorMsg","用户名或密码为空");
//            return "admin/login";
//        }
//
//        ShearCaptcha shearCaptcha = (ShearCaptcha) session.getAttribute("verifyCode");
//        if(shearCaptcha == null || !shearCaptcha.verify(verifyCode)){
//            session.setAttribute("errorMsg","验证码错误");
//            return "admin/login";
//        }
//        AdminUser adminUser = adminUserService.login(userName, password);
//        if(adminUser == null){
//            session.setAttribute("loginUser",adminUser.getNickName());
//            session.setAttribute("loginId",adminUser.getAdminUserId());
//            return "redirect:/admin/index";
//        } else{
//            session.setAttribute("errorMsg","登陆失败");
//            return "admin/login";
//        }
//    }
//
//    //提交登录 → POST /admin/login 验证账号密码，成功跳后台首页
////    方法 profile (请求对象 request)：
////    从 session 中取出 loginUserId，并转为整数
////    调用 adminUserService 的 getUserDetailById 方法，传入 loginUserId，得到 adminUser 对象
////
////    如果 adminUser 为 null：
////    返回视图 "admin/login"    // 用户不存在，跳转登录页
////
////    将 "path" 设为 "profile" 存入 request 属性
////    将 adminUser 的 loginUserName 存入 request 属性，键为 "loginUserName"
////    将 adminUser 的 nickName 存入 request 属性，键为 "nickName"
////
////    返回视图 "admin/profile"    // 渲染个人资料页
//
//    @PostMapping("profile")
//
//    public String profile(HttpServletRequest request){
//        Integer loginUserId = (int) request.getSession().getAttribute("loginUserId");
//        AdminUser adminUser = adminUserService.getUserDetailById(loginUserId);
//        if(adminUser == null){
//            return "admin/login";
//        }
//        request.setAttribute("path","profile");
//        request.setAttribute("loginUserName",adminUser.getLoginUserName());
//        request.setAttribute("nickName",adminUser.getNickName());
//
//        return "admin/profile";
//
//    }
//
//    //修改密码 → POST /admin/profile/password 异步更新
//    //request,originalPassowrd,newPassword
//    //判断新旧密码是否为空，为空返回参数不能为空
//    //获取loginUserId
//    //用adminUserService的updatePasword修改密码
//    //如果修改成功，清空session中的数据
//    //返回ServiceResultEnumsuccess
//    //失败则返回修改失败
//
//    @PostMapping("/profile/password")
//    @ResponseBody
//    public String passwordUpdate(HttpServletRequest request,
//                                 @RequestParam ("originalPassword,") String oriqinalPassword,
//                                 @RequestParam ("newPassword") String newPassword){
//        if(!StringUtils.hasText(oriqinalPassword) || !StringUtils.hasText(newPassword)){
//            return "参数不能为空";
//        }
//        Integer loginUserId = (int) request.getSession().getAttribute("loginUserId");
//        if (adminUserService.updatePassword(loginUserId,oriqinalPassword,newPassword)) {
//            request.getSession().removeAttribute("loginUserId");
//            request.getSession().removeAttribute("loginUser");
//            request.getSession().removeAttribute("erroeMsg");
//            return ServiceResultEnum.SUCCESS.getResult();
//        }else{
//            return "修改失败";
//        }
//    }
//    //修改昵称 → POST /admin/profile/name 异步更新
//    //nameUpdate request，loginUserName，nickName
//    //判断这俩是否为空，为空返回参数不能为空
//    //获取loginUserId
//    //adminUserService里面的方法，如果修改成功，返回success
//    //失败返回修改失败
//
//    @PostMapping("/profile/name")
//    @ResponseBody
//    public String nameUpdate(HttpServletRequest request,
//                             @RequestParam("loginUserName") String loginUserName,
//                             @RequestParam("nickName") String nickName){
//        if(!StringUtils.hasText(loginUserName) || !StringUtils.hasText(nickName)){
//            return "参数不能为空";
//        }
//        Integer  loginUserId= (int) request.getSession().getAttribute("loginUserId");
//        if(adminUserService.updateName(loginUserId,loginUserName,nickName)){
//            return ServiceResultEnum.SUCCESS.getResult();
//        }else{
//            return "修改失败";
//        }
//    }
//
//    @GetMapping("logout")
//    @ResponseBody
//    public String logout(HttpServletRequest request){
//        request.getSession().removeAttribute("loginUserId");
//        request.getSession().removeAttribute("loginUser");
//        request.getSession().removeAttribute("errorMsg");
//        return "/admin/login";
//    }
//
//}
//
//
//
//
//
//
//
//
