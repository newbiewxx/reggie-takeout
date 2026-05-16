package com.wxx.service; // 服务接口包

import com.baomidou.mybatisplus.extension.service.IService; // MyBatis-Plus 基础 Service 接口
import com.wxx.domain.Category; // 分类实体类

public interface CategoryService extends IService<Category> { // 继承 IService，自动提供 CRUD 方法

    /**
     * 删除分类（含业务校验：检查是否关联了菜品或套餐）
     * @param id 分类 ID
     */
    void removeWithCheck(Long id);
}
