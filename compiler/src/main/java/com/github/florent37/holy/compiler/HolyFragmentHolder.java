package com.github.florent37.holy.compiler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by florentchampigny on 12/11/2015.
 */
public class HolyFragmentHolder {
    public Class fragmentClass;
    public List<Object> args;

    public HolyFragmentHolder(Class fragmentClass) {
        this.fragmentClass = fragmentClass;
        this.args = new ArrayList<>();
    }
}
