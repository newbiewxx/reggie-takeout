package com.wxx.controller; // 控制器包

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // MyBatis-Plus 条件构造器
import com.baomidou.mybatisplus.extension.plugins.pagination.Page; // MyBatis-Plus 分页对象
import com.wxx.common.R; // 统一响应结果封装类
import com.wxx.domain.Category; // 分类实体类
import com.wxx.service.CategoryService; // 分类服务接口
import lombok.RequiredArgsConstructor; // Lombok：生成带 final 字段的构造器注入
import lombok.extern.slf4j.Slf4j; // Lombok：日志对象
import org.apache.commons.lang.StringUtils; // 字符串工具类
import org.springframework.web.bind.annotation.DeleteMapping; // DELETE 请求映射注解
import org.springframework.web.bind.annotation.GetMapping; // GET 请求映射注解
import org.springframework.web.bind.annotation.PostMapping; // POST 请求映射注解
import org.springframework.web.bind.annotation.PutMapping; // PUT 请求映射注解
import org.springframework.web.bind.annotation.RequestBody; // 请求体 JSON 绑定注解
import org.springframework.web.bind.annotation.RequestMapping; // 类级别请求映射
import org.springframework.web.bind.annotation.RequestParam; // 请求参数绑定注解
import org.springframework.web.bind.annotation.RestController; // REST 控制器注解

@RestController // 组合注解 = @Controller + @ResponseBody，返回 JSON
@Slf4j // 自动生成 log 日志对象
@RequestMapping("category") // 请求映射前缀：/category
@RequiredArgsConstructor // 为 final 字段生成构造器（Spring 自动注入）
public class CategoryController {

    private final CategoryService categoryService; // 分类服务（构造器注入）

    /**
     * 分类分页查询
     * @param page     当前页码
     * @param pageSize 每页条数
     * @param name     可选：按分类名称模糊筛选
     * @param type     可选：按类型筛选（1=菜品分类，2=套餐分类）
     * @return 统一响应结果（含分页数据）
     */
    @GetMapping("/page") // GET /category/page?page=1&pageSize=10&name=川&type=1
    public R<Page<Category>> page(@RequestParam int page, @RequestParam int pageSize, String name, Integer type) {
        log.info("分类分页查询 - page={}, pageSize={}, name={}, type={}", page, pageSize, name, type);

        // 1. 创建分页对象
        Page<Category> pageInfo = new Page<>(page, pageSize);

        // 2. 构建条件查询
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        // 如果 name 不为空，按分类名称模糊匹配
        queryWrapper.like(StringUtils.isNotEmpty(name), Category::getName, name);
        // 如果 type 不为空，按分类类型精确筛选
        queryWrapper.eq(type != null, Category::getType, type);
        // 按 sort 升序（值越小越靠前），相同 sort 的按更新时间降序排列
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        // 3. 执行分页查询
        categoryService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 新增分类
     * @param category 前端传来的分类信息（JSON，需包含 name、type、sort）
     * @return 统一响应结果
     */
    @PostMapping // POST /category
    public R<String> save(@RequestBody Category category) {
        log.info("新增分类 - name={}, type={}, sort={}", category.getName(), category.getType(), category.getSort());

        // 校验必填字段
        if (StringUtils.isEmpty(category.getName())) {
            return R.error("新增失败，分类名称不能为空");
        }
        if (category.getType() == null || (category.getType() != 1 && category.getType() != 2)) {
            return R.error("新增失败，分类类型无效（1=菜品分类，2=套餐分类）");
        }

        // 检查同类型下是否已存在同名分类
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getName, category.getName())
                    .eq(Category::getType, category.getType());
        if (categoryService.count(queryWrapper) > 0) {
            return R.error("新增失败，该类型下已存在同名分类");
        }

        // 保存到数据库（createTime、updateTime、createUser、updateUser 由 MetaObjectHandler 自动填充）
        categoryService.save(category);
        log.info("新增成功 - ID={}", category.getId());
        return R.success("新增分类成功");
    }

    /**
     * 修改分类
     * @param category 前端传来的分类信息（JSON，必须包含 id）
     * @return 统一响应结果
     */
    @PutMapping // PUT /category
    public R<String> update(@RequestBody Category category) {
        log.info("更新分类 - ID={}", category.getId());

        // 校验 ID 不能为空
        if (category.getId() == null) {
            return R.error("更新失败，分类ID不能为空");
        }

        // 如果修改了名称，检查同类型下是否已存在同名分类（排除自身）
        if (StringUtils.isNotEmpty(category.getName())) {
            LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Category::getName, category.getName())
                        .eq(category.getType() != null, Category::getType, category.getType())
                        .ne(Category::getId, category.getId());
            if (categoryService.count(queryWrapper) > 0) {
                return R.error("更新失败，该类型下已存在同名分类");
            }
        }

        // updateTime 和 updateUser 由 MetaObjectHandler 自动填充
        categoryService.updateById(category);
        log.info("更新成功 - ID={}", category.getId());
        return R.success("修改分类成功");
    }

    /**
     * 删除分类（业务校验由 CategoryService 处理）
     * @param ids 分类 ID
     * @return 统一响应结果
     */
    @DeleteMapping // DELETE /category?ids=xxx
    public R<String> delete(@RequestParam Long ids) {
        log.info("删除分类 - ID={}", ids);

        if (ids == null) {
            return R.error("删除失败，分类ID不能为空");
        }

        categoryService.removeWithCheck(ids);
        return R.success("删除分类成功");
    }
}
