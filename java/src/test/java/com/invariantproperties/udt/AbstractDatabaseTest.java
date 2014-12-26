/*
 * This code was written by Bear Giles <bgiles@coyotesong.com>and he
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Any contributions made by others are licensed to this project under
 * one or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 * Copyright (c) 2012 Bear Giles <bgiles@coyotesong.com>
 */
package com.invariantproperties.udt;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Test user-defined types and methods.
 * 
 * @author bgiles@coyotesong.com
 */
public abstract class AbstractDatabaseTest {
    private static final ResourceBundle bundle = ResourceBundle
            .getBundle("database");
    protected static final double EPSILON = 1e-10;
    protected static BasicDataSource ds;

    static {
        ds = new BasicDataSource();
        ds.setUsername(bundle.getString("username"));
        ds.setPassword(bundle.getString("password"));
        ds.setUrl(bundle.getString("url"));
        ds.setDriverClassName(bundle.getString("driverClassName"));
        ds.setValidationQuery(bundle.getString("validationQuery"));
    }

    /**
     * Load jar file containing our user-defined types.
     * 
     * @throws SQLException
     */
    @BeforeClass
    public static void loadJarFile() throws SQLException {
        File file = new File("userdefinedtypes.jar");
        Connection conn = ds.getConnection();
        Statement stmt = conn.createStatement();
        stmt.execute("create schema invariantproperties");
        stmt.execute(String
                .format("select sqlj.install_jar('file://%s', 'ip_udt', true)",
                        file.getAbsolutePath()));
        stmt.execute("select sqlj.set_classpath('invariantproperties', 'ip_udt')");
        stmt.close();
        conn.close();
    }

    /**
     * Unload jar file containing our user-defined types.
     * 
     * @throws SQLException
     */
    @AfterClass
    public static void unloadJarFile() throws SQLException {
        Connection conn = ds.getConnection();
        Statement stmt = conn.createStatement();
        stmt.execute("select sqlj.remove_jar('ip_udt', true)");
        stmt.execute("drop schema invariantproperties");
        stmt.close();
        conn.close();
    }
}
