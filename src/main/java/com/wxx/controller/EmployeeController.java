package com.wxx.controller; // 控制器包

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // MyBatis-Plus 条件构造器
import com.baomidou.mybatisplus.extension.plugins.pagination.Page; // MyBatis-Plus 分页对象
import com.wxx.common.BaseContext; // 当前用户上下文工具类
import com.wxx.common.R; // 统一响应结果封装类
import com.wxx.domain.Employee; // 员工实体类
import com.wxx.service.EmployeeService; // 员工服务接口
import lombok.RequiredArgsConstructor; // Lombok：生成带 final 字段的构造器注入
import lombok.extern.slf4j.Slf4j; // Lombok：日志对象
import org.apache.commons.lang.StringUtils; // 字符串工具类
import org.springframework.util.DigestUtils; // MD5 加密工具
import org.springframework.web.bind.annotation.GetMapping; // GET 请求映射注解
import org.springframework.web.bind.annotation.PathVariable; // URL 路径变量绑定注解
import org.springframework.web.bind.annotation.PostMapping; // POST 请求映射注解
import org.springframework.web.bind.annotation.PutMapping; // PUT 请求映射注解
import org.springframework.web.bind.annotation.RequestBody; // 请求体 JSON 绑定注解
import org.springframework.web.bind.annotation.RequestMapping; // 类级别请求映射
import org.springframework.web.bind.annotation.RequestParam; // 请求参数绑定注解
import org.springframework.web.bind.annotation.RestController; // REST 控制器注解

@RestController // 组合注解 = @Controller + @ResponseBody，返回 JSON
@Slf4j // 自动生成 log 日志对象
@RequestMapping("employee") // 请求映射前缀：/employee
@RequiredArgsConstructor // 为 final 字段生成构造器（Spring 自动注入）
public class EmployeeController { // 员工管理控制器

    private final EmployeeService employeeService; // 员工服务（构造器注入）

    /**
     * 员工登录
     * @param employee 前端传来的登录信息（username, password）
     * @return 统一响应结果
     */
    @PostMapping("/login") // POST /employee/login
    public R<Employee> login(@RequestBody Employee employee) {
        // 校验用户名和密码不能为空
        if (StringUtils.isEmpty(employee.getUsername()) || StringUtils.isEmpty(employee.getPassword())) {
            return R.error("登录失败，用户名或密码不能为空");
        }

        // 将明文密码进行 MD5 加密，与数据库密文比对
        String password = DigestUtils.md5DigestAsHex(employee.getPassword().getBytes());

        // 根据用户名查询员工
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        // 用户不存在
        if (emp == null) {
            return R.error("登录失败，用户不存在");
        }

        // 密码错误
        if (!emp.getPassword().equals(password)) {
            return R.error("登录失败，密码错误");
        }

        // 账号被禁用
        if (emp.getStatus() == 0) {
            return R.error("登录失败，账号已禁用");
        }

        // 登录成功，员工 ID 存入 Session
        BaseContext.setCurrentEmployeeId(emp.getId());

        // 返回前清空密码，避免泄露
        emp.setPassword(null);
        return R.success(emp);
    }

    /**
     * 员工退出
     * @return 统一响应结果
     */
    @PostMapping("/logout") // POST /employee/logout
    public R<String> logout() {
        log.info("员工退出登录");
        // 清除 Session 中保存的员工 ID
        BaseContext.removeCurrentEmployeeId();
        return R.success("退出成功");
    }

    /**
     * 新增员工
     * @param employee 前端传来的员工信息（JSON）
     * @return 统一响应结果
     */
    @PostMapping // POST /employee（没有子路径）
    public R<String> save(@RequestBody Employee employee) {
        log.info("新增员工 - username={}", employee.getUsername());

        // 检查用户名是否已存在
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        if (employeeService.count(queryWrapper) > 0) {
            return R.error("新增失败，用户名已存在");
        }

        // 设置默认密码 123456（MD5 加密后存入数据库）
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        // 设置默认状态为启用
        employee.setStatus(1);

        // 保存到数据库（createTime、updateTime、createUser、updateUser 由 MetaObjectHandler 自动填充）
        employeeService.save(employee);
        log.info("新增成功 - ID={}", employee.getId());
        return R.success("新增员工成功");
    }

    /**
     * 员工分页查询
     * @param page     当前页码
     * @param pageSize 每页条数
     * @param name     可选：按姓名模糊筛选
     * @return 统一响应结果（含分页数据）
     */
    @GetMapping("/page") // GET /employee/page?page=1&pageSize=10&name=张
    public R<Page<Employee>> page(@RequestParam int page, @RequestParam int pageSize, String name) {
        log.info("分页查询 - page={}, pageSize={}, name={}", page, pageSize, name);

        // 1. 创建分页对象
        Page<Employee> pageInfo = new Page<>(page, pageSize);

        // 2. 构建条件查询
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        // 如果 name 不为空，按姓名模糊匹配
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        // 按更新时间降序排列
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        // 3. 执行分页查询
        employeeService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 根据 ID 查询员工（用于编辑回显）
     * @param id 员工 ID
     * @return 统一响应结果
     */
    @GetMapping("/{id}") // GET /employee/{id}
    public R<Employee> getById(@PathVariable Long id) {
        log.info("查询员工 - ID={}", id);
        Employee employee = employeeService.getById(id);
        if (employee == null) {
            return R.error("员工不存在");
        }
        // 返回前清空密码，避免泄露
        employee.setPassword(null);
        return R.success(employee);
    }

    /**
     * 更新员工信息（编辑保存）
     * @param employee 前端传来的员工信息（JSON，必须包含 ID）
     * @return 统一响应结果
     */
    @PutMapping // PUT /employee
    public R<String> update(@RequestBody Employee employee) {
        log.info("更新员工 - ID={}", employee.getId());

        // 校验 ID 不能为空
        if (employee.getId() == null) {
            return R.error("更新失败，员工ID不能为空");
        }

        // 不允许通过此接口修改用户名（用户名唯一，应保持不可变）
        employee.setUsername(null);

        // 禁止禁用自己
        if (employee.getStatus() != null && employee.getStatus() == 0
                && employee.getId().equals(BaseContext.getCurrentEmployeeId())) {
            return R.error("不能禁用自己");
        }

        // updateTime 和 updateUser 由 MetaObjectHandler 自动填充
        employeeService.updateById(employee);
        log.info("更新成功 - ID={}", employee.getId());
        return R.success("员工信息更新成功");
    }
}
