package com.deem.zkui.mapper;

import com.deem.zkui.domain.Role;

public interface RoleMapper {
    int deleteById(Integer rid);

    int insert(Role record);

    int insertSelective(Role record);

    Role getById(Integer rid);

    int updateByIdSelective(Role record);

    int updateById(Role record);
}