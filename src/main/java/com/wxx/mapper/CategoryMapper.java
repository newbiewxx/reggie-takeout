package com.wxx.mapper; // Mapper 接口包

import com.baomidou.mybatisplus.core.mapper.BaseMapper; // MyBatis-Plus 基础 Mapper
import com.wxx.domain.Category; // 分类实体类

public interface CategoryMapper extends BaseMapper<Category> { // MyBatis-Plus 自动提供 CRUD 方法
}