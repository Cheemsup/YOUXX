package org.youxx.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.youxx.entity.UserAddress;

import java.util.List;

@Mapper
public interface UserAddressMapper {

    List<UserAddress> selectByUserId(@Param("userId") String userId);

    UserAddress selectById(@Param("id") Long id);

    int insert(UserAddress address);

    int update(UserAddress address);

    int clearDefaultByUserId(@Param("userId") String userId);

    int setDefault(@Param("id") Long id);

    int deleteById(@Param("id") Long id);
}