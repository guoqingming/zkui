package com.deem.zkui.vo;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @program: zkui1
 * @description:
 * @author: guoqingming
 * @create: 2018-08-04 16:49
 **/

@Data
public class HistoryVO {

    /**
     */
//    @ApiModelProperty("")
    private Long id;

    /**
     */
//    @ApiModelProperty("")
    private String changeUser;

    /**
     */
//    @ApiModelProperty("")
    private String changeDate;

    /**
     */
    private String changeSummary;

    /**
     */
    private String changeIp;
}
