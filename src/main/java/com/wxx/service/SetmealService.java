package com.wxx.service; // 服务接口包

import com.baomidou.mybatisplus.extension.service.IService; // MyBatis-Plus 基础 Service 接口
import com.wxx.domain.Setmeal; // 套餐实体类

public interface SetmealService extends IService<Setmeal> { // 继承 IService，自动提供 CRUD 方法
}
