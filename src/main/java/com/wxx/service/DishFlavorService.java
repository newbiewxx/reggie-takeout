package com.wxx.service; // 服务接口包

import com.baomidou.mybatisplus.extension.service.IService; // MyBatis-Plus 基础 Service 接口
import com.wxx.domain.DishFlavor; // 菜品口味实体类

public interface DishFlavorService extends IService<DishFlavor> { // 继承 IService，自动提供 CRUD 方法
}
