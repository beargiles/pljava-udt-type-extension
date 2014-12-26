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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test user-defined types and methods. TODO: flesh out these tests.
 * 
 * @author bgiles@coyotesong.com
 */
public class RationalDatabaseTest extends AbstractDatabaseTest {

    /**
     * Get value.
     * 
     * @throws SQLException
     */
    public Rational getRationalValue(ResultSet rs, int idx) throws SQLException {
        return Rational.parse(((PGobject) rs.getObject(idx)).getValue());
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
        stmt.execute("create temp table if not exists rational_test(id int primary key, r invariantproperties.rational)");

        try {
            stmt.execute("insert into rational_test values (1, 1)");
            fail("implicit cast did not throw expected exception");
        } catch (SQLException e) {

        }

        stmt.execute("drop table rational_test");
        stmt.close();
        conn.close();
    }

    /**
     * Test implicit casts. See notes above for problem when the first insertion
     * is an implicit cast from int to rational.
     * 
     * @throws SQLException
     */
    @Test
    public void testImplicitCast() throws SQLException {
        Connection conn = ds.getConnection();
        Statement stmt = conn.createStatement();
        stmt.execute("create temp table if not exists rational_test(id int primary key, r invariantproperties.rational)");
        stmt.execute("insert into rational_test values (1, '1')");
        stmt.execute("insert into rational_test values (2, '1/2')");
        stmt.execute("insert into rational_test values (3, '4/2')");
        stmt.execute("insert into rational_test values (4, 4)");

        ResultSet rs = stmt
                .executeQuery("select r from rational_test where id=1");
        rs.next();
        Rational r = getRationalValue(rs, 1);
        assertEquals(1, r.getNumerator());
        assertEquals(1, r.getDenominator());
        rs.close();

        rs = stmt.executeQuery("select r from rational_test where id=2");
        rs.next();
        r = getRationalValue(rs, 1);
        assertEquals(1, r.getNumerator());
        assertEquals(2, r.getDenominator());
        rs.close();

        rs = stmt.executeQuery("select r from rational_test where id=3");
        rs.next();
        r = getRationalValue(rs, 1);
        assertEquals(2, r.getNumerator());
        assertEquals(1, r.getDenominator());
        rs.close();

        rs = stmt.executeQuery("select r from rational_test where id=4");
        rs.next();
        r = getRationalValue(rs, 1);
        assertEquals(4, r.getNumerator());
        assertEquals(1, r.getDenominator());
        rs.close();

        stmt.execute("drop table rational_test");
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
        stmt.execute("create temp table if not exists rational_test(id int primary key, p invariantproperties.rational, q invariantproperties.rational, a invariantproperties.rational, b invariantproperties.rational)");
        stmt.execute("insert into rational_test values (1, '2/3', '5/7', '-2/3', '2/-3')");

        // test basic operations
        ResultSet rs = stmt
                .executeQuery("select a, b, -p, p + q, p - q, q - p, p * q, p / q from rational_test where id=1");
        assertTrue(rs.next());
        Rational r = getRationalValue(rs, 1);
        assertEquals(-2, r.getNumerator());
        assertEquals(3, r.getDenominator());
        r = getRationalValue(rs, 2);
        assertEquals(-2, r.getNumerator());
        assertEquals(3, r.getDenominator());
        r = getRationalValue(rs, 3);
        assertEquals(-2, r.getNumerator());
        assertEquals(3, r.getDenominator());
        r = getRationalValue(rs, 4);
        assertEquals(29, r.getNumerator());
        assertEquals(21, r.getDenominator());
        r = getRationalValue(rs, 5);
        assertEquals(-1, r.getNumerator());
        assertEquals(21, r.getDenominator());
        r = getRationalValue(rs, 6);
        assertEquals(1, r.getNumerator());
        assertEquals(21, r.getDenominator());
        r = getRationalValue(rs, 7);
        assertEquals(10, r.getNumerator());
        assertEquals(21, r.getDenominator());
        r = getRationalValue(rs, 8);
        assertEquals(14, r.getNumerator());
        assertEquals(15, r.getDenominator());
        rs.close();

        stmt.execute("drop table rational_test");
        stmt.close();
        conn.close();
    }

    /**
     * Test comparison operations.
     * 
     * @throws SQLException
     */
    @Test
    public void testComparisonOperations() throws SQLException {
        Connection conn = ds.getConnection();
        Statement stmt = conn.createStatement();
        stmt.execute("create temp table if not exists rational_test(id int primary key, a invariantproperties.rational, b invariantproperties.rational, c invariantproperties.rational)");
        stmt.execute("insert into rational_test values (1, '1/3', '1/3', '2/3')");

        ResultSet rs = stmt
                .executeQuery("select a < b, a < c, a <= b, a <= c, a == b, a == c, a <> b, a <> c, a >= b, a >= c, a > b, a > c from rational_test where id=1");
        assertTrue(rs.next());
        assertFalse(rs.getBoolean(1));
        assertTrue(rs.getBoolean(2));
        assertTrue(rs.getBoolean(3));
        assertTrue(rs.getBoolean(4));
        assertTrue(rs.getBoolean(5));
        assertFalse(rs.getBoolean(6));
        assertFalse(rs.getBoolean(7));
        assertTrue(rs.getBoolean(8));
        assertTrue(rs.getBoolean(9));
        assertFalse(rs.getBoolean(10));
        assertFalse(rs.getBoolean(11));
        assertFalse(rs.getBoolean(12));

        stmt.execute("drop table rational_test");
        stmt.close();
        conn.close();
    }

    /**
     * Test aggregate operations.
     * 
     * @throws SQLException
     */
    @Test
    public void testAggregateOperations() throws SQLException {
        Connection conn = ds.getConnection();
        Statement stmt = conn.createStatement();
        stmt.execute("create temp table if not exists rational_test(id int primary key, a invariantproperties.rational)");
        stmt.execute("insert into rational_test values (1, '1/2')");
        stmt.execute("insert into rational_test values (2, '1/3')");
        stmt.execute("insert into rational_test values (3, '2/3')");
        stmt.execute("insert into rational_test values (4, '1/3')");

        ResultSet rs = stmt
                .executeQuery("select min(a), max(a) from rational_test");
        assertTrue(rs.next());
        Rational r = getRationalValue(rs, 1);
        assertEquals(1, r.getNumerator());
        assertEquals(3, r.getDenominator());

        r = getRationalValue(rs, 2);
        assertEquals(2, r.getNumerator());
        assertEquals(3, r.getDenominator());

        stmt.execute("drop table rational_test");
        stmt.close();
        conn.close();
    }
}
