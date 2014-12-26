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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Ignore;
import org.junit.Test;
import org.postgresql.util.PGobject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test user-defined types and methods. TODO: flesh out these tests.
 * 
 * @author bgiles@coyotesong.com
 */
public class ComplexDatabaseTest extends AbstractDatabaseTest {

    /**
     * Get value.
     * 
     * @throws SQLException
     */
    public Complex getComplexValue(ResultSet rs, int idx) throws SQLException {
        return Complex.parse(((PGobject) rs.getObject(idx)).getValue());
    }

    /**
     * This test demonstrates that an implicit cast from int to complex throws a
     * SQLException when it is the first attempt to insert a complex value.
     * 
     * N.B., this test is highly position-dependent since it will cause all
     * other implicit casts to fail AND any insertion with an implicit cast from
     * string to complex causes int to complex tests to succeed.
     * 
     * @throws SQLException
     */
    @Test
    @Ignore
    public void testCreateImplicitCastProblem() throws SQLException {
        Connection conn = ds.getConnection();
        Statement stmt = conn.createStatement();
        stmt.execute("create temp table if not exists complex_test(id int primary key, c invariantproperties.complex)");

        try {
            stmt.execute("insert into complex_test values (1, 1)");
            fail("implicit cast did not throw expected exception");
        } catch (SQLException e) {

        }

        stmt.execute("drop table complex_test");
        stmt.close();
        conn.close();
    }

    /**
     * Test implicit casts. See notes above for problem when the first insertion
     * is an implicit cast from int to complex.
     * 
     * @throws SQLException
     */
    @Test
    public void testImplicitCast() throws SQLException {
        Connection conn = ds.getConnection();
        Statement stmt = conn.createStatement();
        stmt.execute("create temp table if not exists complex_test(id int primary key, c invariantproperties.complex)");
        stmt.execute("insert into complex_test values (1, '(1, 0)')");
        stmt.execute("insert into complex_test values (2, '(0, 1)')");
        stmt.execute("insert into complex_test values (3, 3.0)");
        stmt.execute("insert into complex_test values (4, 4)");

        ResultSet rs = stmt
                .executeQuery("select c from complex_test where id=1");
        rs.next();
        Complex c = getComplexValue(rs, 1);
        assertEquals(1, c.Re(), EPSILON);
        assertEquals(0, c.Im(), EPSILON);
        rs.close();

        rs = stmt.executeQuery("select c from complex_test where id=2");
        rs.next();
        c = getComplexValue(rs, 1);
        assertEquals(0, c.Re(), EPSILON);
        assertEquals(1, c.Im(), EPSILON);
        rs.close();

        rs = stmt.executeQuery("select c from complex_test where id=3");
        rs.next();
        c = getComplexValue(rs, 1);
        assertEquals(3, c.Re(), EPSILON);
        assertEquals(0, c.Im(), EPSILON);
        rs.close();

        rs = stmt.executeQuery("select c from complex_test where id=4");
        rs.next();
        c = getComplexValue(rs, 1);
        assertEquals(4, c.Re(), EPSILON);
        assertEquals(0, c.Im(), EPSILON);
        rs.close();

        stmt.execute("drop table complex_test");
        stmt.close();
        conn.close();
    }

    /**
     * Test arithmetic operations.
     * 
     * @throws SQLException
     */
    @Test
    public void testArithmetic() throws SQLException {
        Connection conn = ds.getConnection();
        Statement stmt = conn.createStatement();
        stmt.execute("create temp table if not exists complex_test(id int primary key, p invariantproperties.complex, q invariantproperties.complex)");
        stmt.execute("insert into complex_test values (1, '(3, 0)', '(0, 4)')");

        // test basic operations
        // TODO: add division
        ResultSet rs = stmt
                .executeQuery("select -p, -q, p + q, p - q, q - p, p * q from complex_test where id=1");
        assertTrue(rs.next());
        Complex c = getComplexValue(rs, 1);
        assertEquals(-3, c.Re(), EPSILON);
        assertEquals(0, c.Im(), EPSILON);
        c = getComplexValue(rs, 2);
        assertEquals(0, c.Re(), EPSILON);
        assertEquals(4, c.Im(), EPSILON);
        c = getComplexValue(rs, 3);
        assertEquals(3, c.Re(), EPSILON);
        assertEquals(4, c.Im(), EPSILON);
        c = getComplexValue(rs, 4);
        assertEquals(3, c.Re(), EPSILON);
        assertEquals(-4, c.Im(), EPSILON);
        c = getComplexValue(rs, 5);
        assertEquals(-3, c.Re(), EPSILON);
        assertEquals(4, c.Im(), EPSILON);
        c = getComplexValue(rs, 6);
        assertEquals(0, c.Re(), EPSILON);
        assertEquals(12, c.Im(), EPSILON);
        rs.close();

        rs = stmt
                .executeQuery("select invariantproperties.magnitude(p), invariantproperties.magnitude(q), invariantproperties.magnitude(p + q) from complex_test where id=1");
        assertTrue(rs.next());
        assertEquals(3, rs.getDouble(1), EPSILON);
        assertEquals(4, rs.getDouble(2), EPSILON);
        assertEquals(5, rs.getDouble(3), EPSILON);
        rs.close();

        rs = stmt
                .executeQuery("select invariantproperties.conjugate(p), invariantproperties.conjugate(q) from complex_test where id=1");
        assertTrue(rs.next());
        c = getComplexValue(rs, 1);
        assertEquals(3, c.Re(), EPSILON);
        assertEquals(0, c.Im(), EPSILON);
        c = getComplexValue(rs, 2);
        assertEquals(0, c.Re(), EPSILON);
        assertEquals(-4, c.Im(), EPSILON);
        rs.close();

        // test commutable scalar operations
        // rs =
        // stmt.executeQuery("select p + 2, 2 + p, q + 2, q - 2, p * 2, 2 * p, q * 2, 2 * q from complex_test where id=1");
        rs = stmt
                .executeQuery("select p + 2, q + 2, p * 2, q * 2, 2 + p, 2 + q, 2 * p, 2 * q from complex_test where id=1");
        assertTrue(rs.next());
        c = getComplexValue(rs, 1);
        assertEquals(5, c.Re(), EPSILON);
        assertEquals(0, c.Im(), EPSILON);
        c = getComplexValue(rs, 2);
        assertEquals(2, c.Re(), EPSILON);
        assertEquals(4, c.Im(), EPSILON);
        c = getComplexValue(rs, 3);
        assertEquals(6, c.Re(), EPSILON);
        assertEquals(0, c.Im(), EPSILON);
        c = getComplexValue(rs, 4);
        assertEquals(0, c.Re(), EPSILON);
        assertEquals(8, c.Im(), EPSILON);
        c = getComplexValue(rs, 5);
        assertEquals(5, c.Re(), EPSILON);
        assertEquals(0, c.Im(), EPSILON);
        c = getComplexValue(rs, 6);
        assertEquals(2, c.Re(), EPSILON);
        assertEquals(4, c.Im(), EPSILON);
        c = getComplexValue(rs, 7);
        assertEquals(6, c.Re(), EPSILON);
        assertEquals(0, c.Im(), EPSILON);
        c = getComplexValue(rs, 8);
        assertEquals(0, c.Re(), EPSILON);
        assertEquals(8, c.Im(), EPSILON);

        rs = stmt
                .executeQuery("select p + 2.0, q + 2.0, p * 2.0, q * 2.0, 2.0 + p, 2.0 + q, 2.0 * p, 2.0 * q from complex_test where id=1");
        assertTrue(rs.next());
        c = getComplexValue(rs, 1);
        assertEquals(5, c.Re(), EPSILON);
        assertEquals(0, c.Im(), EPSILON);
        c = getComplexValue(rs, 2);
        assertEquals(2, c.Re(), EPSILON);
        assertEquals(4, c.Im(), EPSILON);
        c = getComplexValue(rs, 3);
        assertEquals(6, c.Re(), EPSILON);
        assertEquals(0, c.Im(), EPSILON);
        c = getComplexValue(rs, 4);
        assertEquals(0, c.Re(), EPSILON);
        assertEquals(8, c.Im(), EPSILON);
        c = getComplexValue(rs, 5);
        assertEquals(5, c.Re(), EPSILON);
        assertEquals(0, c.Im(), EPSILON);
        c = getComplexValue(rs, 6);
        assertEquals(2, c.Re(), EPSILON);
        assertEquals(4, c.Im(), EPSILON);
        c = getComplexValue(rs, 7);
        assertEquals(6, c.Re(), EPSILON);
        assertEquals(0, c.Im(), EPSILON);
        c = getComplexValue(rs, 8);
        assertEquals(0, c.Re(), EPSILON);
        assertEquals(8, c.Im(), EPSILON);

        stmt.execute("drop table complex_test");
        stmt.close();
        conn.close();
    }
}
