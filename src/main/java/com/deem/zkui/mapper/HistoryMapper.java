package com.deem.zkui.mapper;

import com.deem.zkui.domain.History;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface HistoryMapper {
    int deleteById(Long id);

    int insert(History record);

    int insertSelective(History record);

    History getById(Long id);

    int updateByIdSelective(History record);

    int updateById(History record);

    List<History> findAll(@Param("historyNode") String historyNode);
}