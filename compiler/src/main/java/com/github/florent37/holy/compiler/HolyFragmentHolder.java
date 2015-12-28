package com.github.florent37.holy.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Name;

/**
 * Created by florentchampigny on 12/11/2015.
 */
public class HolyFragmentHolder {
    public ClassName classNameComplete;
    public List<Variable> args;
    public String className;

    public HolyFragmentHolder(ClassName classNameComplete, String className) {
        this.classNameComplete = classNameComplete;
        this.className = className;
        this.args = new ArrayList<>();
    }


}
