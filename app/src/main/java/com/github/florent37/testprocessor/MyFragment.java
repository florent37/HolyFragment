package com.github.florent37.testprocessor;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.florent37.holy.annotations.Holy;

/**
 * Created by florentchampigny on 28/12/2015.
 */
public class MyFragment extends Fragment{

    @Holy int number;
    @Holy String name;

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main,container,false);
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        HolyMyFragment.bless(this);

        ((TextView)view.findViewById(R.id.text)).setText(String.valueOf(number));
    }
}
