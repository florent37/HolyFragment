package com.github.florent37.holyfragment.processor;

import com.squareup.javapoet.ClassName;

import java.util.ArrayList;
import java.util.List;

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

    public void addArgument(Variable variable){
        args.add(variable);
    }


}
