package com.wxx.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wxx.common.BaseContext;
import com.wxx.common.R;
import com.wxx.domain.User;
import com.wxx.service.UserService;
import com.wxx.utils.ValidateCodeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 发送手机验证码
     * @param map 包含 phone 字段的 JSON 请求体
     * @return 统一响应结果
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody Map<String, String> map, HttpSession session) {
        String phone = map.get("phone");
        log.info("发送验证码 - phone={}", phone);

        if (phone == null || phone.isEmpty()) {
            return R.error("手机号不能为空");
        }

        // 生成 4 位数字验证码
        Integer code = ValidateCodeUtils.generateValidateCode(4);
        log.info("验证码 - phone={}, code={}", phone, code);

        // 存入 Session，登录时校验
        session.setAttribute(phone, code);

        return R.success("验证码发送成功");
    }

    /**
     * 用户登录
     * @param map 包含 phone 和 code 字段的 JSON 请求体
     * @return 统一响应结果（含用户信息）
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map<String, String> map, HttpSession session) {
        String phone = map.get("phone");
        String code = map.get("code");
        log.info("用户登录 - phone={}, code={}", phone, code);

        if (phone == null || phone.isEmpty()) {
            return R.error("手机号不能为空");
        }
        if (code == null || code.isEmpty()) {
            return R.error("验证码不能为空");
        }

        // 从 Session 获取已发送的验证码
        String sessionCode = String.valueOf(session.getAttribute(phone));
        log.info("已发送的验证码 - phone={}, code={}", phone, sessionCode);
        if (!code.equals(sessionCode)) {
            return R.error("验证码错误");
        }

        // 根据手机号查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, phone);
        User user = userService.getOne(queryWrapper);

        // 新用户自动注册
        if (user == null) {
            user = new User();
            user.setPhone(phone);
            user.setStatus(1);
            userService.save(user);
            log.info("新用户注册 - ID={}, phone={}", user.getId(), phone);
        }

        // 存入 Session
        BaseContext.setCurrentUserId(user.getId());
        log.info("用户登录成功 - ID={}, phone={}", user.getId(), phone);

        return R.success(user);
    }
}
