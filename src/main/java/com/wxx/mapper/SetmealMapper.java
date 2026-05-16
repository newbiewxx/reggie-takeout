package com.wxx.mapper; // Mapper 接口包

import com.baomidou.mybatisplus.core.mapper.BaseMapper; // MyBatis-Plus 基础 Mapper
import com.wxx.domain.Setmeal; // 套餐实体类
import org.apache.ibatis.annotations.Mapper; // MyBatis Mapper 注解

@Mapper // Spring 自动扫描为 Mapper Bean
public interface SetmealMapper extends BaseMapper<Setmeal> { // 继承 BaseMapper，自动提供 CRUD 方法
}
