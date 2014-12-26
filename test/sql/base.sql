\set ECHO 0
BEGIN;
\i sql/pljava-udt-type.sql
\set ECHO all

-- You should write your tests

SELECT pljava-udt-type('foo', 'bar');

SELECT 'foo' #? 'bar' AS arrowop;

CREATE TABLE ab (
    a_field pljava-udt-type
);

INSERT INTO ab VALUES('foo' #? 'bar');
SELECT (a_field).a, (a_field).b FROM ab;

SELECT (pljava-udt-type('foo', 'bar')).a;
SELECT (pljava-udt-type('foo', 'bar')).b;

SELECT ('foo' #? 'bar').a;
SELECT ('foo' #? 'bar').b;

ROLLBACK;
