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
package com.invariantproperties.udt.sql;

import java.math.BigDecimal;
import java.sql.SQLData;
import java.sql.SQLException;
import java.sql.SQLInput;
import java.sql.SQLOutput;
import java.util.ResourceBundle;

import org.postgresql.pljava.annotation.BaseUDT;
import org.postgresql.pljava.annotation.Cast;
import org.postgresql.pljava.annotation.Function;
import org.postgresql.pljava.annotation.Operator;
import static org.postgresql.pljava.annotation.Cast.Application.ASSIGNMENT;
import static
    org.postgresql.pljava.annotation.Function.OnNullInput.RETURNS_NULL;
import static org.postgresql.pljava.annotation.Function.Effects.IMMUTABLE;
import static org.postgresql.pljava.annotation.Operator.SELF;
import static org.postgresql.pljava.annotation.Operator.TWIN;
import static org.postgresql.pljava.annotation.Operator.SelectivityEstimators.*;

import com.invariantproperties.udt.Complex;

/**
 * Glue that allows Complex numbers to be stored as user-defined types in
 * database.
 * 
 * Design note: this class uses composition, not inheritance, since it should
 * only be used as a container to persist and retrieve a Complex value.
 * 
 * @author bgiles@coyotesong.com
 */
@BaseUDT(
    schema="invariantproperties", name="complex",
    internalLength=16,
    alignment=BaseUDT.Alignment.INT4 // can this be right? components are 8 wide
)
public class ComplexUDT implements SQLData {
    private static final ResourceBundle bundle = ResourceBundle
            .getBundle(ComplexUDT.class.getName());
    private static final String TYPE_NAME = bundle.getString("typeName");
    private Complex value;
    private String typeName;

    /**
     * Parse input string.
     */
    @Function(effects=IMMUTABLE, onNullInput=RETURNS_NULL)
    public static ComplexUDT parse(String input, String typeName)
            throws SQLException {
        // TODO: verify recognized typename.
        Complex value = null;
        try {
            value = Complex.parse(input);
        } catch (IllegalArgumentException e) {
            throw new SQLException(e);
        }
        return new ComplexUDT(value);
    }

    /**
     * No-arg constructor. An SQLData implementation needs one, because
     * readSQL is an instance method; before the runtime can invoke it when
     * initializing a new instance, it first has to be able to <em>make</em>
     * the new instance.
     */
    public ComplexUDT() { }

    /**
     * Constructor taking only real value.
     * 
     * @param value
     * @throws SQLException
     */
    public ComplexUDT(double value) throws SQLException {
        this(value, 0);
    }

    /**
     * Constructor taking only real value.
     * 
     * @param real
     * @throws SQLException
     */
    public ComplexUDT(long real) throws SQLException {
        this(real, 0);
    }

    /**
     * Constructor taking real and imaginary values.
     * 
     * @param real
     * @param imaginary
     * @throws SQLException
     */
    public ComplexUDT(double real, double imaginary) throws SQLException {
        this(real, imaginary, TYPE_NAME);
    }

    /**
     * Constructor taking real, imaginary and type name.
     * 
     * @param real
     * @param imaginary
     * @param typeName
     * @throws SQLException
     */
    public ComplexUDT(double real, double imaginary, String typeName)
            throws SQLException {
        this.value = new Complex(real, imaginary);
        this.typeName = typeName;
    }

    /**
     * Copy constructor (convenience)
     * 
     * @param p
     * @throws SQLException
     */
    protected ComplexUDT(Complex p) throws SQLException {
        this.value = p;
        this.typeName = TYPE_NAME;
    }

    /**
     * Get SQL Type Name.
     */
    public String getSQLTypeName() {
        return typeName;
    }

    /**
     * Read object from SQLInput stream.
     */
    @Function(effects=IMMUTABLE, onNullInput=RETURNS_NULL)
    public void readSQL(SQLInput stream, String typeName) throws SQLException {
        double re = stream.readDouble();
        double im = stream.readDouble();
        this.value = new Complex(re, im);
        this.typeName = typeName;
    }

    /**
     * Write object to SQLOutput stream.
     */
    @Function(effects=IMMUTABLE, onNullInput=RETURNS_NULL)
    public void writeSQL(SQLOutput stream) throws SQLException {
        stream.writeDouble(value.Re());
        stream.writeDouble(value.Im());
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return (value == null) ? 0 : value.hashCode();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        if ((value == null) || (o == null)) {
            return false;
        }

        if (this == o) {
            return true;
        }

        if (!(o instanceof ComplexUDT)) {
            return false;
        }

        ComplexUDT c = (ComplexUDT) o;
        if (c.value == null) {
            return false;
        }

        return value.equals(c.value);
    }

    /**
     * Return string representing value. N.B., the toString() contract
     * says that this method should never return null but the SQL contract
     * says that a null object should return a null value. The UDT can
     * never be null, of course, but it might wrap an uninitialized value.
     * Therefore this method breaks the standard java contract and follows
     * the SQL contract.
     *<p>
     * It's all moot though, because the function is annotated RETURNS_NULL
     * on null input, which means the PostgreSQL optimizer will <em>not even
     * call it</em> on a null {@code invariantproperties.rational} value.
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    @Function(effects=IMMUTABLE, onNullInput=RETURNS_NULL)
    public String toString() {
        return (value == null) ? null : value.toString();
    }

    /**
     * Static methods that will be published as user-defined function.
     * 
     * @param input
     * @return
     * @throws SQLException
     */
    @Function(schema="invariantproperties", name="complex_string_as_complex",
        effects=IMMUTABLE, onNullInput=RETURNS_NULL)
    @Cast(application=ASSIGNMENT)
    public static ComplexUDT newInstance(String input) throws SQLException {
        if (input == null) {
            return null;
        }
        return parse(input, TYPE_NAME);
    }

    /**
     * Static methods that will be published as user-defined function.
     * 
     * @param value
     * @return
     * @throws SQLException
     */
    @Function(schema="invariantproperties", name="complex_double_as_complex",
        effects=IMMUTABLE, onNullInput=RETURNS_NULL)
    @Cast(application=ASSIGNMENT)
    public static ComplexUDT newInstance(double value) throws SQLException {
        return new ComplexUDT(value);
    }

    /**
     * Static methods that will be published as user-defined function. This may
     * result in the loss of significant digits.
     * 
     * @param value
     * @return
     * @throws SQLException
     */
    @Function(schema="invariantproperties",
        name="complex_bigdecimal_as_complex",
        effects=IMMUTABLE, onNullInput=RETURNS_NULL)
    @Cast(application=ASSIGNMENT)
    public static ComplexUDT newInstance(BigDecimal value) throws SQLException {
        return new ComplexUDT(value.doubleValue());
    }

    /**
     * Static methods that will be published as user-defined function.
     * 
     * @param value
     * @return
     * @throws SQLException
     */
    @Function(schema="invariantproperties", name="complex_int_as_complex",
        effects=IMMUTABLE, onNullInput=RETURNS_NULL)
    @Cast(application=ASSIGNMENT)
    public static ComplexUDT newInstance(int value) throws SQLException {
        return new ComplexUDT(value);
    }

    /**
     * Static methods that will be published as user-defined function.
     * 
     * @param value
     * @return
     * @throws SQLException
     */
    @Function(schema="invariantproperties", name="complex_long_as_complex",
        effects=IMMUTABLE, onNullInput=RETURNS_NULL)
    @Cast(application=ASSIGNMENT)
    public static ComplexUDT newInstance(long value) throws SQLException {
        return new ComplexUDT(value);
    }

    /**
     * Static methods that will be published as user-defined function.
     * 
     * @param p
     * @return
     * @throws SQLException
     */
    @Function(schema="invariantproperties", name="complex_negate",
        effects=IMMUTABLE, onNullInput=RETURNS_NULL)
    @Operator(name = "-")
    public static ComplexUDT negate(ComplexUDT p) throws SQLException {
        if ((p == null) || (p.value == null)) {
            return null;
        }
        return new ComplexUDT(p.value.negate());
    }

    /**
     * Static methods that will be published as user-defined function.
     * 
     * @param p
     * @param q
     * @return
     * @throws SQLException
     */
    @Function(schema="invariantproperties", name="complex_add",
        effects=IMMUTABLE, onNullInput=RETURNS_NULL)
    @Operator(name = "+", commutator = SELF)
    public static ComplexUDT add(ComplexUDT p, ComplexUDT q)
            throws SQLException {
        if ((p == null) || (p.value == null) || (q == null)
                || (q.value == null)) {
            return null;
        }
        return new ComplexUDT(p.value.add(q.value));
    }

    /**
     * Static methods that will be published as user-defined function.
     * 
     * @param p
     * @param q
     * @return
     * @throws SQLException
     */
    @Function(schema="invariantproperties", name="complex_add",
        effects=IMMUTABLE, onNullInput=RETURNS_NULL)
    @Operator(name = "+", commutator = TWIN)
    @Operator(name = "+", synthetic = TWIN)
    public static ComplexUDT add(ComplexUDT p, int q) throws SQLException {
        return add(p, (double) q);
    }

    /**
     * Static methods that will be published as user-defined function.
     * 
     * @param p
     * @param q
     * @return
     * @throws SQLException
     */
    @Function(schema="invariantproperties", name="complex_add",
        effects=IMMUTABLE, onNullInput=RETURNS_NULL)
    @Operator(name = "+", commutator = TWIN)
    @Operator(name = "+", synthetic = TWIN)
    public static ComplexUDT add(ComplexUDT p, long q) throws SQLException {
        return add(p, (double) q);
    }

    /**
     * Static methods that will be published as user-defined function.
     * 
     * @param p
     * @param q
     * @return
     * @throws SQLException
     */
    @Function(schema="invariantproperties", name="complex_add",
        effects=IMMUTABLE, onNullInput=RETURNS_NULL)
    @Operator(name = "+", commutator = TWIN)
    @Operator(name = "+", synthetic = TWIN)
    public static ComplexUDT add(ComplexUDT p, float q) throws SQLException {
        return add(p, (double) q);
    }

    /**
     * Static methods that will be published as user-defined function.
     * 
     * @param p
     * @param q
     * @return
     * @throws SQLException
     */
    @Function(schema="invariantproperties", name="complex_add",
        effects=IMMUTABLE, onNullInput=RETURNS_NULL)
    @Operator(name = "+", commutator = TWIN)
    @Operator(name = "+", synthetic = TWIN)
    public static ComplexUDT add(ComplexUDT p, double q) throws SQLException {
        if ((p == null) || (p.value == null)) {
            return null;
        }
        return new ComplexUDT(p.value.add(new Complex(q)));
    }

    /**
     * Static methods that will be published as user-defined function.
     * 
     * @param p
     * @param q
     * @return
     * @throws SQLException
     */
    @Function(schema="invariantproperties", name="complex_add",
        effects=IMMUTABLE, onNullInput=RETURNS_NULL)
    @Operator(name = "+", commutator = TWIN)
    @Operator(name = "+", synthetic = TWIN)
    public static ComplexUDT add(ComplexUDT p, BigDecimal q)
            throws SQLException {
        return add(p, q.doubleValue());
    }

    /**
     * Static methods that will be published as user-defined function.
     * 
     * @param p
     * @param q
     * @return
     * @throws SQLException
     */
    @Function(schema="invariantproperties", name="complex_subtract",
        effects=IMMUTABLE, onNullInput=RETURNS_NULL)
    @Operator(name = "-")
    public static ComplexUDT subtract(ComplexUDT p, ComplexUDT q)
            throws SQLException {
        if ((p == null) || (p.value == null) || (q == null)
                || (q.value == null)) {
            return null;
        }
        return new ComplexUDT(p.value.subtract(q.value));
    }

    /**
     * Static methods that will be published as user-defined function.
     * 
     * @param p
     * @param q
     * @return
     * @throws SQLException
     */
    @Function(schema="invariantproperties", name="complex_multiply",
        effects=IMMUTABLE, onNullInput=RETURNS_NULL)
    @Operator(name = "*", commutator = SELF)
    public static ComplexUDT multiply(ComplexUDT p, ComplexUDT q)
            throws SQLException {
        if ((p == null) || (p.value == null) || (q == null)
                || (q.value == null)) {
            return null;
        }
        return new ComplexUDT(p.value.multiply(q.value));
    }

    /**
     * Static methods that will be published as user-defined function.
     * 
     * @param p
     * @param q
     * @return
     * @throws SQLException
     */
    @Function(schema="invariantproperties", name="complex_multiply",
        effects=IMMUTABLE, onNullInput=RETURNS_NULL)
    @Operator(name = "*", commutator = TWIN)
    @Operator(name = "*", synthetic = TWIN)
    public static ComplexUDT multiply(ComplexUDT p, int q) throws SQLException {
        return multiply(p, (double) q);
    }

    /**
     * Static methods that will be published as user-defined function.
     * 
     * @param p
     * @param q
     * @return
     * @throws SQLException
     */
    @Function(schema="invariantproperties", name="complex_multiply",
        effects=IMMUTABLE, onNullInput=RETURNS_NULL)
    @Operator(name = "*", commutator = TWIN)
    @Operator(name = "*", synthetic = TWIN)
    public static ComplexUDT multiply(ComplexUDT p, long q) throws SQLException {
        return multiply(p, (double) q);
    }

    /**
     * Static methods that will be published as user-defined function.
     * 
     * @param p
     * @param q
     * @return
     * @throws SQLException
     */
    @Function(schema="invariantproperties", name="complex_multiply",
        effects=IMMUTABLE, onNullInput=RETURNS_NULL)
    @Operator(name = "*", commutator = TWIN)
    @Operator(name = "*", synthetic = TWIN)
    public static ComplexUDT multiply(ComplexUDT p, float q)
            throws SQLException {
        return multiply(p, (double) q);
    }

    /**
     * Static methods that will be published as user-defined function.
     * 
     * @param p
     * @param q
     * @return
     * @throws SQLException
     */
    @Function(schema="invariantproperties", name="complex_multiply",
        effects=IMMUTABLE, onNullInput=RETURNS_NULL)
    @Operator(name = "*", commutator = TWIN)
    @Operator(name = "*", synthetic = TWIN)
    public static ComplexUDT multiply(ComplexUDT p, double q)
            throws SQLException {
        if ((p == null) || (p.value == null)) {
            return null;
        }
        return new ComplexUDT(p.value.multiply(new Complex(q)));
    }

    /**
     * Static methods that will be published as user-defined function.
     * 
     * @param p
     * @param q
     * @return
     * @throws SQLException
     */
    @Function(schema="invariantproperties", name="complex_multiply",
        effects=IMMUTABLE, onNullInput=RETURNS_NULL)
    @Operator(name = "*", commutator = TWIN)
    @Operator(name = "*", synthetic = TWIN)
    public static ComplexUDT multiply(ComplexUDT p, BigDecimal q)
            throws SQLException {
        return multiply(p, q.doubleValue());
    }

    /**
     * Static methods that will be published as user-defined function.
     * 
     * @param p
     * @return
     * @throws SQLException
     */
    @Function(schema="invariantproperties",
        effects=IMMUTABLE, onNullInput=RETURNS_NULL)
    public static ComplexUDT abs(ComplexUDT p) throws SQLException {
        if ((p == null) || (p.value == null)) {
            return null;
        }
        return new ComplexUDT(p.value.abs());
    }

    /**
     * Static methods that will be published as user-defined function.
     * 
     * @param p
     * @return
     * @throws SQLException
     */
    @Function(schema="invariantproperties",
        effects=IMMUTABLE, onNullInput=RETURNS_NULL)
    public static ComplexUDT conjugate(ComplexUDT p) throws SQLException {
        if ((p == null) || (p.value == null)) {
            return null;
        }
        return new ComplexUDT(p.value.getConjugate());
    }

    /**
     * Static methods that will be published as user-defined function.
     * 
     * @param p
     * @return
     * @throws SQLException
     */
    @Function(schema="invariantproperties",
        effects=IMMUTABLE, onNullInput=RETURNS_NULL)
    public static Double magnitude(ComplexUDT p) throws SQLException {
        if ((p == null) || (p.value == null)) {
            return null;
        }
        return p.value.getMagnitude();
    }
}
