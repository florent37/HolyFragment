package com.github.florent37.holy.compiler;

import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Name;

/**
 * Created by florentchampigny on 12/11/2015.
 */
public class HolyFragmentHolder {
    public TypeName fragmentClassName;
    public List<Variable> args;

    public HolyFragmentHolder(TypeName fragmentClassName) {
        this.fragmentClassName = fragmentClassName;
        this.args = new ArrayList<>();
    }
}
