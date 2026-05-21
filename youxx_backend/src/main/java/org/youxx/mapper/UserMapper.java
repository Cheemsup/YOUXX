package org.youxx.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.youxx.entity.User;

import java.util.List;

@Mapper
public interface UserMapper {

    List<User> selectPage(
            @Param("keyword") String keyword,
            @Param("role") String role,
            @Param("offset") int offset,
            @Param("size") int size);

    long count(
            @Param("keyword") String keyword,
            @Param("role") String role);

    User selectById(@Param("id") String id);

    User selectByUsername(@Param("username") String username);

    User selectByIdWithPassword(@Param("id") String id);

    int insert(User user);

    int update(User user);

    int updateProfile(User user);

    int updatePassword(@Param("id") String id, @Param("password") String password);

    int updateStatus(@Param("id") String id, @Param("status") String status);

    int deleteById(@Param("id") String id);

    String selectMaxIdByPrefix(@Param("prefix") String prefix);
}