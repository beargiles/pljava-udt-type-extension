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

import org.junit.Test;

import com.invariantproperties.udt.Rational;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for complex type.
 * 
 * @author bgiles@coyotesong.com
 */
public class RationalTest {

    /**
     * Test constructors.
     */
    @Test
    public void testConstructors() {
        // Rational c0 = new Rational();
        // assertEquals("real value did not match", 0.0, c0.Re(), EPSILON);
        // assertEquals("imaginary value did not match", 0.0, c0.Im(), EPSILON);

        Rational c1 = new Rational(1);
        assertEquals("numerator did not match", 1, c1.getNumerator());
        assertEquals("demoninator did not match", 1, c1.getDenominator());

        Rational c2 = new Rational(1, 2);
        assertEquals("numerator did not match", 1, c2.getNumerator());
        assertEquals("demoninator did not match", 2, c2.getDenominator());

        Rational c3 = new Rational(4, 2);
        assertEquals("numerator did not match", 2, c3.getNumerator());
        assertEquals("demoninator did not match", 1, c3.getDenominator());
    }

    /**
     * Test equality.
     */
    @Test
    public void testEquality() {
        Rational c0 = new Rational(1, 2);
        Rational c1 = new Rational(3, 4);
        Rational c2 = new Rational(5, 4);

        assertFalse(c0.equals(c1));

        c0 = c0.add(c1);
        assertTrue(c0.equals(c2));
    }

    /**
     * Test basic operators.
     */
    @Test
    public void testOperators() {
        /*
         * Rational c0 = new Rational(1, 2); Rational c1 = new Rational(3, 4);
         * 
         * assertEquals(c0.Re(), c0.getReal(), EPSILON); assertEquals(c0.Im(),
         * c0.getImaginary(), EPSILON);
         * 
         * Rational c = c0.negate(); assertEquals(-c0.Re(), c.Re(), EPSILON);
         * assertEquals(c0.Im(), c.Im(), EPSILON);
         * 
         * c = c0.getConjugate(); assertEquals(c0.Re(), c.Re(), EPSILON);
         * assertEquals(-c0.Im(), c.Im(), EPSILON);
         * 
         * c = c0.add(c1); assertEquals(c0.Re() + c1.Re(), c.Re(), EPSILON);
         * assertEquals(c0.Im() + c1.Im(), c.Im(), EPSILON);
         * 
         * c = c0.subtract(c1); assertEquals(c0.Re() - c1.Re(), c.Re(),
         * EPSILON); assertEquals(c0.Im() - c1.Im(), c.Im(), EPSILON);
         * 
         * c = c0.multiply(c1); assertEquals(c0.Re() * c1.Re() - c0.Im() *
         * c1.Im(), c.Re(), EPSILON); assertEquals(c0.Re() * c1.Im() + c0.Im() *
         * c1.Re(), c.Im(), EPSILON);
         * 
         * assertEquals(5.0, new Rational(3, 4).getMagnitude(), EPSILON);
         */
    }

    /**
     * Test basic operators with scalar operand.
     */
    @Test
    public void testScalarOperators() {
        /*
         * Rational c0 = new Rational(1, 2); double p = 2;
         * 
         * Rational c = c0.add(p); assertEquals(c0.Re() + p, c.Re(), EPSILON);
         * assertEquals(c0.Im(), c.Im(), EPSILON);
         * 
         * c = c0.subtract(p); assertEquals(c0.Re() - p, c.Re(), EPSILON);
         * assertEquals(c0.Im(), c.Im(), EPSILON);
         * 
         * c = c0.multiply(p); assertEquals(c0.Re() * p, c.Re(), EPSILON);
         * assertEquals(c0.Im() * p, c.Im(), EPSILON);
         * 
         * c = c0.divide(p); assertEquals(c0.Re() / p, c.Re(), EPSILON);
         * assertEquals(c0.Im() / p, c.Im(), EPSILON);
         */
    }

    /**
     * Test toString()/parse().
     */
    @Test
    public void testParse() {
        Rational c0 = new Rational(1, 2);

        // verify string has expected format. We don't care about trailing
        // zeroes.
        String s = c0.toString();
        assertTrue(s.matches("1/2"));

        Rational c = Rational.parse(s);
        assertEquals(c0.getNumerator(), c.getNumerator());
        assertEquals(c0.getDenominator(), c.getDenominator());
    }
}
