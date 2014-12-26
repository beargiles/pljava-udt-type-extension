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

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of immutable complex numbers. This class does not extend
 * Number since it cannot be uniquely mapped to the other Number classes.
 * 
 * @author bgiles@coyotesong.com
 */
public class Complex implements Serializable {
    private static final long serialVersionUID = 1L;

    // FIXME: add Formattable.

    public static final Complex ZERO = new Complex(0, 0);
    public static final Complex ONE = new Complex(1, 0);
    public static final Complex I = new Complex(0, 1);

    protected double real;
    protected double imaginary;

    /**
     * Default constructor. This returns a value equal to ZERO for consistency
     * with other Number values. (Unfortunately I don't know how to return the
     * ZERO object itself.)
     * 
     * @param real
     */
    public Complex() {
        this(0, 0);
    }

    /**
     * Constructor taking only a real value.
     * 
     * @param real
     */
    public Complex(double real) {
        this(real, 0);
    }

    /**
     * Constructor taking a real and imaginary value.
     * 
     * @param real
     * @param imaginary
     */
    public Complex(double real, double imaginary) {
        this.real = real;
        this.imaginary = imaginary;
    }

    /**
     * Get real value.
     * 
     * @return
     */
    public double getReal() {
        return real;
    }

    /**
     * Get real value (shorter alias)
     */
    public double Re() {
        return getReal();
    }

    /**
     * Get imaginary value.
     * 
     * @return
     */
    public double getImaginary() {
        return imaginary;
    }

    /**
     * Get imaginary value (shorter alias)
     */
    public double Im() {
        return getImaginary();
    }

    /**
     * Get absolute value.
     */
    public Complex abs() {
        return new Complex(Math.abs(real), imaginary);
    }

    /**
     * Get complex conjugate.
     */
    public Complex getConjugate() {
        return new Complex(real, -imaginary);
    }

    /**
     * Get magnitude
     */
    public double getMagnitude() {
        return Math.sqrt(Math.abs(this.multiply(this.getConjugate()).Re()));
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (int) (31 * real + imaginary);
    }

    /**
     * Test for equality with an epsilon value.
     */
    public boolean equals(Object right, double epsilon) {
        if (right == null) {
            return false;
        }

        if (this == right) {
            return true;
        }

        if (!(right instanceof Complex)) {
            return false;
        }

        Complex r = (Complex) right;
        return this.subtract(r).getMagnitude() <= epsilon;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object right) {
        if (right == null) {
            return false;
        }

        if (this == right) {
            return true;
        }

        if (!(right instanceof Complex)) {
            return false;
        }

        Complex r = (Complex) right;
        return (real == r.real) && (imaginary == r.imaginary);
    }

    /**
     * Inverse of equals()
     */
    public boolean notEquals(Object right) {
        return !equals(right);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String value = null;
        // if (imaginary == 0) {
        // value = String.valueOf(real);
        // } else {
        value = String.format("(%s, %s)", Double.toString(real),
                Double.toString(imaginary));
        // }
        return value;
    }

    /**
     * Parse a complex number from a string.
     */
    public static Complex parse(String input) {
        Pattern pattern = Pattern
                .compile("\\((-?[0-9]+(\\.[0-9]+)?)( *, *(-?[0-9]+(\\.[0-9]+)?))?\\)");
        Matcher matcher = pattern.matcher(input);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Unable to parse complex from string \"" + input + '"');
        }

        if (matcher.groupCount() == 5) {
            if (matcher.group(4) == null) {
                return new Complex(Double.parseDouble(matcher.group(1)));
            }
            return new Complex(Double.parseDouble(matcher.group(1)),
                    Double.parseDouble(matcher.group(4)));
        }
        throw new IllegalArgumentException("invalid format: \"" + input + '"');
    }

    /**
     * Negate a complex number.
     * 
     * @return
     */
    public Complex negate() {
        return new Complex(-real, imaginary);
    }

    /**
     * Add two complex numbers.
     * 
     * @param p
     * @return
     */
    public Complex add(Complex p) {
        if (p == null) {
            throw new IllegalArgumentException();
        }
        return new Complex(real + p.real, imaginary + p.imaginary);
    }

    /**
     * Subtract two complex numbers.
     * 
     * @param p
     * @return
     */
    public Complex subtract(Complex p) {
        if (p == null) {
            throw new IllegalArgumentException();
        }
        return new Complex(real - p.real, imaginary - p.imaginary);
    }

    /**
     * Multiply two complex numbers.
     * 
     * @param p
     * @return
     */
    public Complex multiply(Complex p) {
        if (p == null) {
            throw new IllegalArgumentException();
        }
        return new Complex(real * p.real - imaginary * p.imaginary, real
                * p.imaginary + imaginary * p.real);
    }

    /**
     * Divide two complex numbers.
     * 
     * @param p
     * @return
     */
    public Complex divide(Complex p) {
        if (p == null) {
            throw new IllegalArgumentException();
        }
        
        return this.multiply(p.invert());
    }

    /**
     * Invert a complex number.
     * 
     * @param p
     * @return
     */
    public Complex invert() {
        double det = real * real + imaginary * imaginary;
        if (det == 0) {
            throw new IllegalArgumentException("attempt to divide by zero");
        }

        return new Complex(real / det, -imaginary / det);
    }

    /**
     * Add a complex number and a scalar.
     * 
     * @param p
     * @return
     */
    public Complex add(double p) {
        return new Complex(real + p, imaginary);
    }

    /**
     * Subtract a complex number and a scalar.
     * 
     * @param p
     * @return
     */
    public Complex subtract(double p) {
        return new Complex(real - p, imaginary);
    }

    /**
     * Multiply a complex number and a scalar.
     * 
     * @param p
     * @return
     */
    public Complex multiply(double p) {
        return new Complex(real * p, imaginary * p);
    }

    /**
     * Divide a complex number and a scalar.
     * 
     * @param p
     * @return
     */
    public Complex divide(double p) {
        return new Complex(real / p, imaginary / p);
    }
}
