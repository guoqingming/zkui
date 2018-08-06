package com.deem.zkui.domain;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 *  
 */
@Data
public class History implements Serializable {
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
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date changeDate;

    /**
     */
    private String changeSummary;

    /**
     */
    private String changeIp;

    /**
     */
    private static final long serialVersionUID = 1L;

}