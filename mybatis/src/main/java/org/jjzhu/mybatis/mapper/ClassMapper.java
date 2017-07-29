/**
 * @(#)ClassMapper.java, 2017/7/25.
 * <p/>
 * Copyright 2017 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.jjzhu.mybatis.mapper;


import org.apache.ibatis.annotations.Param;

public interface ClassMapper {

    public int updateClassName(@Param("name") String className, @Param("id") int id);
}