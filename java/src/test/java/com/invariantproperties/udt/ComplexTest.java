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

import com.invariantproperties.udt.Complex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for complex type.
 * 
 * @author bgiles@coyotesong.com
 */
public class ComplexTest {
    private static final double EPSILON = 1e-10;

    /**
     * Test constructors.
     */
    @Test
    public void testConstructors() {
        Complex c0 = new Complex();
        assertEquals("real value did not match", 0.0, c0.Re(), EPSILON);
        assertEquals("imaginary value did not match", 0.0, c0.Im(), EPSILON);

        Complex c1 = new Complex(1);
        assertEquals("real value did not match", 1.0, c1.Re(), EPSILON);
        assertEquals("imaginary value did not match", 0.0, c1.Im(), EPSILON);

        Complex c2 = new Complex(1, 2);
        assertEquals("real value did not match", 1.0, c2.Re(), EPSILON);
        assertEquals("imaginary value did not match", 2.0, c2.Im(), EPSILON);
    }

    /**
     * Test equality.
     */
    @Test
    public void testEquality() {
        Complex c0 = new Complex(1, 2);
        Complex c1 = new Complex(3, 4);

        assertFalse(c0.equals(c1));

        c0 = c0.add(new Complex(2, 2));
        assertTrue(c0.equals(c1));
    }

    /**
     * Compute distance between two complex numbers.
     */
    public double error(Complex c0, Complex c1) {
        return c0.subtract(c1).getMagnitude();
    }
    
    /**
     * Test basic operators.
     */
    @Test
    public void testOperators() {
        Complex c0 = new Complex(1, 2);
        Complex c1 = new Complex(3, 4);

        assertEquals(c0.Re(), c0.getReal(), EPSILON);
        assertEquals(c0.Im(), c0.getImaginary(), EPSILON);

        Complex c = c0.negate();
        assertEquals(-c0.Re(), c.Re(), EPSILON);
        assertEquals(c0.Im(), c.Im(), EPSILON);

        c = c0.getConjugate();
        assertEquals(c0.Re(), c.Re(), EPSILON);
        assertEquals(-c0.Im(), c.Im(), EPSILON);

        c = c0.add(c1);
        assertEquals(c0.Re() + c1.Re(), c.Re(), EPSILON);
        assertEquals(c0.Im() + c1.Im(), c.Im(), EPSILON);

        c = c0.subtract(c1);
        assertEquals(c0.Re() - c1.Re(), c.Re(), EPSILON);
        assertEquals(c0.Im() - c1.Im(), c.Im(), EPSILON);

        c = c0.multiply(c1);
        assertEquals(c0.Re() * c1.Re() - c0.Im() * c1.Im(), c.Re(), EPSILON);
        assertEquals(c0.Re() * c1.Im() + c0.Im() * c1.Re(), c.Im(), EPSILON);
        
        Complex c2 = c0.multiply(c0.invert());
        assertTrue(error(Complex.ONE, c2) < EPSILON);
        
        c2 = c1.multiply(c1.invert());
        assertTrue(error(Complex.ONE, c2) < EPSILON);
        
        c2 = c0.divide(c1).multiply(c1);
        assertTrue(error(c0, c2) < EPSILON);

        assertEquals(5.0, new Complex(3, 4).getMagnitude(), EPSILON);
    }

    /**
     * Test basic operators with scalar operand.
     */
    @Test
    public void testScalarOperators() {
        Complex c0 = new Complex(1, 2);
        double p = 2;

        Complex c = c0.add(p);
        assertEquals(c0.Re() + p, c.Re(), EPSILON);
        assertEquals(c0.Im(), c.Im(), EPSILON);

        c = c0.subtract(p);
        assertEquals(c0.Re() - p, c.Re(), EPSILON);
        assertEquals(c0.Im(), c.Im(), EPSILON);

        c = c0.multiply(p);
        assertEquals(c0.Re() * p, c.Re(), EPSILON);
        assertEquals(c0.Im() * p, c.Im(), EPSILON);

        c = c0.divide(p);
        assertEquals(c0.Re() / p, c.Re(), EPSILON);
        assertEquals(c0.Im() / p, c.Im(), EPSILON);
    }

    /**
     * Test toString()/parse().
     */
    @Test
    public void testParse() {
        Complex c0 = new Complex(1, 2);

        // verify string has expected format. We don't care about trailing
        // zeroes.
        String s = c0.toString();
        assertTrue(s.matches("\\(1(\\.0+)?, 2(\\.0+)?\\)"));

        Complex c = Complex.parse(s);
        assertEquals(c0.Re(), c.Re(), EPSILON);
        assertEquals(c0.Im(), c.Im(), EPSILON);

        c = Complex.parse("(1, 2)");
        assertEquals(1, c.Re(), EPSILON);
        assertEquals(2, c.Im(), EPSILON);

        c = Complex.parse("(-1, 2)");
        assertEquals(-1, c.Re(), EPSILON);
        assertEquals(2, c.Im(), EPSILON);

        c = Complex.parse("(1, -2)");
        assertEquals(1, c.Re(), EPSILON);
        assertEquals(-2, c.Im(), EPSILON);
    }
}
