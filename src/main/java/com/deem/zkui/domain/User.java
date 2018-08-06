package com.deem.zkui.domain;

import java.io.Serializable;
import lombok.Data;

/**
 *  
 */
@Data
public class User implements Serializable {
    /**
     */
    private Integer uid;

    /**
     */
    private String username;

    /**
     */
    private String password;

    /**
     */
    private static final long serialVersionUID = 1L;
}