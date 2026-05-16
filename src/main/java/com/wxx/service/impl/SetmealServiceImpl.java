package com.wxx.service.impl; // Service 实现包

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl; // MyBatis-Plus 基础实现类
import com.wxx.domain.Setmeal; // 套餐实体类
import com.wxx.mapper.SetmealMapper; // 套餐 Mapper
import com.wxx.service.SetmealService; // 套餐 Service 接口
import org.springframework.stereotype.Service; // Spring 服务注解

@Service // 声明为 Spring Bean，自动扫描注入
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    // 继承 ServiceImpl 已自动实现 IService 的所有 CRUD 方法，无需额外代码
}
