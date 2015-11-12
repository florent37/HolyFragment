package com.github.florent37.holy.compiler;

import com.squareup.javapoet.TypeName;

/**
 * Created by florentchampigny on 12/11/2015.
 */
public class Variable {
    public TypeName typeName;
    public String elementName;

    public Variable(TypeName typeName, String elementName) {
        this.typeName = typeName;
        this.elementName = elementName;
    }
}
