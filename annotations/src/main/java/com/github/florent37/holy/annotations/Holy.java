package com.github.florent37.holy.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Created by florentchampigny on 11/11/2015.
 */
@Retention(CLASS) @Target(FIELD)
public @interface Holy {
}
