package com.wxx.service.impl; // Service 实现包

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl; // MyBatis-Plus 基础实现类
import com.wxx.domain.DishFlavor; // 菜品口味实体类
import com.wxx.mapper.DishFlavorMapper; // 菜品口味 Mapper
import com.wxx.service.DishFlavorService; // 菜品口味 Service 接口
import org.springframework.stereotype.Service; // Spring 服务注解

@Service // 声明为 Spring Bean，自动扫描注入
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
    // 继承 ServiceImpl 已自动实现 IService 的所有 CRUD 方法，无需额外代码
}
