package com.wxx.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wxx.domain.Dish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {

    /**
     * 查询已逻辑删除的同名菜品（自定义 SQL 绕过 @TableLogic 过滤）
     */
    @Select("SELECT * FROM dish WHERE name = #{name} AND is_deleted = 1")
    List<Dish> selectDeletedByName(@Param("name") String name);

    /**
     * 更新已逻辑删除的菜品名称（自定义 SQL 绕过 @TableLogic 过滤）
     */
    @Update("UPDATE dish SET name = #{newName} WHERE id = #{id} AND is_deleted = 1")
    int renameDeletedById(@Param("id") long id, @Param("newName") String newName);
}
