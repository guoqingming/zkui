package com.deem.zkui.domain;

import java.io.Serializable;
import lombok.Data;

/**
 *  
 */
@Data
public class Role implements Serializable {
    /**
     */
//    @ApiModelProperty("")
    private Integer rid;

    /**
     */
    private String rname;

    /**
     */
    private static final long serialVersionUID = 1L;

}