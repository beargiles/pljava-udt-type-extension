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
import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of immutable rational numbers.
 * 
 * @author bgiles@coyotesong.com
 */
public class Rational extends Number implements Serializable,
        Comparable<Rational> {
    private static final long serialVersionUID = 1L;
    protected static final int NULL_POSITION = 1; // sort nulls high

    protected long numerator;
    protected long denominator;

    /**
     * Constructor taking only a numerator.
     * 
     * @param numerator
     */
    public Rational(long numerator) {
        this(numerator, 1);
    }

    /**
     * Constructor taking a numerator and denominator.
     * 
     * @param numerator
     * @param denominator
     */
    public Rational(long numerator, long denominator) {
        if (denominator == 0) {
            throw new IllegalArgumentException("demominator must be non-zero");
        }

        // do a little bit of normalization
        if (denominator < 0) {
            numerator = -numerator;
            denominator = -denominator;
        }

        long gcd = Rational.gcd(numerator, denominator);
        
        this.numerator = numerator / gcd;
        this.denominator = denominator / gcd;
        
        if (this.denominator < 0) {
            this.numerator = -this.numerator;
            this.denominator = -this.denominator;
        }
    }

    /**
     * Get numerator.
     * 
     * @return
     */
    public long getNumerator() {
        return numerator;
    }

    /**
     * Get denominator.
     * 
     * @return
     */
    public long getDenominator() {
        return denominator;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (int) (31 * numerator + denominator);
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

        if (!(right instanceof Rational)) {
            return false;
        }

        Rational r = (Rational) right;
        return (numerator == r.numerator) && (denominator == r.denominator);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String value = null;
        if (denominator == 1) {
            value = String.valueOf(numerator);
        } else {
            value = String.format("%d/%d", numerator, denominator);
        }
        return value;
    }

    /**
     * Parse a rational number from a string.
     */
    public static Rational parse(String input) {
        Pattern pattern = Pattern.compile("(-?[0-9]+)( */ *(-?[0-9]+))?");
        Matcher matcher = pattern.matcher(input);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Unable to parse rational from string \"" + input + '"');
        }
        if (matcher.groupCount() == 3) {
            if (matcher.group(3) == null) {
                return new Rational(Long.parseLong(matcher.group(1)));
            }
            return new Rational(Long.parseLong(matcher.group(1)),
                    Long.parseLong(matcher.group(3)));
        }
        throw new IllegalArgumentException("invalid format: \"" + input + '"');
    }

    /**
     * Compare two Rational numbers.
     * 
     * @param p
     * @return
     */
    @Override
    public int compareTo(Rational p) {
        if (p == null) {
            return NULL_POSITION;
        }
        BigInteger l = BigInteger.valueOf(numerator).multiply(
                BigInteger.valueOf(p.denominator));
        BigInteger r = BigInteger.valueOf(p.numerator).multiply(
                BigInteger.valueOf(denominator));
        return l.compareTo(r);
    }

    /**
     * Compare a Rational number and a double.
     * 
     * @param p
     * @return
     */
    public int compareTo(double p) {
        double d = doubleValue();
        return (d < p) ? -1 : ((d == p) ? 0 : 1);
    }

    /**
     * @see {java.lang.Number#doubleValue()}
     */
    @Override
    public double doubleValue() {
        return ((double) getNumerator()) / ((double) getDenominator());
    }

    /**
     * @see {java.lang.Number#floatValue()}. Implementation note: we go through
     *      double first for max. accuracy.
     */
    @Override
    public float floatValue() {
        return Double.valueOf(doubleValue()).floatValue();
    }

    /**
     * @see {java.lang.Number#byteValue()}. Implementation note: we go through
     *      double first for max. accuracy.
     */
    @Override
    public byte byteValue() {
        return Double.valueOf(doubleValue()).byteValue();
    }

    /**
     * @see {java.lang.Number#shortValue()}. Implementation note: we go through
     *      double first for max. accuracy.
     */
    @Override
    public short shortValue() {
        return Double.valueOf(doubleValue()).shortValue();
    }

    /**
     * @see {java.lang.Number#intValue()}. Implementation note: we go through
     *      double first for max. accuracy.
     */
    @Override
    public int intValue() {
        return Double.valueOf(doubleValue()).intValue();
    }

    /**
     * @see {java.lang.Number#longValue()}. Implementation note: we go through
     *      double first for max. accuracy.
     */
    @Override
    public long longValue() {
        return Double.valueOf(doubleValue()).longValue();
    }

    /**
     * Compute GCD of two values.
     */
    public static long gcd(long p, long q) {
        if (p < 0) {
            return -gcd(-p, q);
        } else if (q < 0) {
            return -gcd(p, -q);
        } else if (q < p) {
            return gcd(q, p);
        }

        while (p != 0) {
            long t = p;
            p = q % p;
            q = t;
        }

        return q;
    }

    /**
     * Determine the minimum of two Rational numbers.
     * 
     * @param p
     * @param q
     * @return
     */
    public static Rational min(Rational p, Rational q) {
        if ((p == null) || (q == null)) {
            throw new IllegalArgumentException();
        }
        return (p.compareTo(q) <= 0) ? p : q;
    }

    /**
     * Determine the minimum of two Rational numbers.
     * 
     * @param p
     * @param q
     * @return
     */
    public static Rational max(Rational p, Rational q) {
        if ((p == null) || (q == null)) {
            throw new IllegalArgumentException();
        }
        return (p.compareTo(q) > 0) ? p : q;
    }

    /**
     * Negate a rational number.
     * 
     * @return
     */
    public Rational negate() {
        return new Rational(-numerator, denominator);
    }

    /**
     * Add two rational numbers.
     * 
     * @param p
     * @return
     */
    public Rational add(Rational p) {
        if (p == null) {
            throw new IllegalArgumentException();
        }
        BigInteger n = BigInteger
                .valueOf(numerator)
                .multiply(BigInteger.valueOf(p.denominator))
                .add(BigInteger.valueOf(p.numerator).multiply(
                        BigInteger.valueOf(denominator)));
        BigInteger d = BigInteger.valueOf(denominator).multiply(
                BigInteger.valueOf(p.denominator));
        BigInteger gcd = n.gcd(d);
        n = n.divide(gcd);
        d = d.divide(gcd);
        return new Rational(n.longValue(), d.longValue());
    }

    /**
     * Subtract two rational numbers.
     * 
     * @param p
     * @return
     */
    public Rational subtract(Rational p) {
        if (p == null) {
            throw new IllegalArgumentException();
        }
        BigInteger n = BigInteger
                .valueOf(numerator)
                .multiply(BigInteger.valueOf(p.denominator))
                .subtract(
                        BigInteger.valueOf(p.numerator).multiply(
                                BigInteger.valueOf(denominator)));
        BigInteger d = BigInteger.valueOf(denominator).multiply(
                BigInteger.valueOf(p.denominator));
        BigInteger gcd = n.gcd(d);
        n = n.divide(gcd);
        d = d.divide(gcd);
        return new Rational(n.longValue(), d.longValue());
    }

    /**
     * Multiply two rational numbers.
     * 
     * @param p
     * @return
     */
    public Rational multiply(Rational p) {
        if (p == null) {
            throw new IllegalArgumentException();
        }
        BigInteger n = BigInteger.valueOf(numerator).multiply(
                BigInteger.valueOf(p.numerator));
        BigInteger d = BigInteger.valueOf(denominator).multiply(
                BigInteger.valueOf(p.denominator));
        BigInteger gcd = n.gcd(d);
        n = n.divide(gcd);
        d = d.divide(gcd);
        return new Rational(n.longValue(), d.longValue());
    }

    /**
     * Divide two rational numbers.
     * 
     * @param p
     * @return
     */
    public Rational divide(Rational p) {
        if (p == null) {
            throw new IllegalArgumentException();
        }
        BigInteger n = BigInteger.valueOf(numerator).multiply(
                BigInteger.valueOf(p.denominator));
        BigInteger d = BigInteger.valueOf(denominator).multiply(
                BigInteger.valueOf(p.numerator));
        BigInteger gcd = n.gcd(d);
        n = n.divide(gcd);
        d = d.divide(gcd);
        return new Rational(n.longValue(), d.longValue());
    }
}
