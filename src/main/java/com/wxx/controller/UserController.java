package com.wxx.controller;

import com.wxx.common.R;
import com.wxx.utils.ValidateCodeUtils;
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
public class UserController {

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

        // 实际项目中此处应调用短信服务商 API 发送验证码
        // 开发阶段直接打印日志

        return R.success("验证码发送成功");
    }
}
