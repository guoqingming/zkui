package com.deem.zkui.service;

import com.deem.zkui.domain.History;
import com.deem.zkui.mapper.HistoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @program: zkui1
 * @description:
 * @author: guoqingming
 * @create: 2018-08-03 23:00
 **/

@Service
public class HistoriesService {

    @Autowired
    private HistoryMapper historyMapper;


    public void insertHistory(String user, String ipAddress, String summary) {
            //To avoid errors due to truncation.
            if (summary.length() >= 500) {
                summary = summary.substring(0, 500);
            }
            History history = new History();
            history.setChangeUser(user);
            history.setChangeIp(ipAddress);
            history.setChangeSummary(summary);
            history.setChangeDate(new Date());
            historyMapper.insertSelective(history);

    }

    public List<History> fetchHistoryRecords() {
        return historyMapper.findAll(null);

    }

    public List<History> fetchHistoryRecordsByNode(String historyNode) {
        return historyMapper.findAll(historyNode);
    }
}
