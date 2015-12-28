# HolyFragment

Instantiate a new fragment
```java
HolyMyFragment.newInstance(3,"Florent");
```

And then bless it
```java
public class MyFragment extends Fragment{

    @Holy int number;
    @Holy String name;

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        HolyMyFragment.bless(this);
    }
}
```

It automatically generates newInstance and data retrieving

```java
public final class HolyMyFragment {
  public static MyFragment newInstance(int number, String name) {
    Bundle bundle = new Bundle();
    bundle.putInt("number",number);
    bundle.putString("name",name);
    MyFragment fragment = new MyFragment();
    fragment.setArguments(bundle);
    return fragment;
  }

  public static void bless(MyFragment fragment) {
    Bundle args = fragment.getArguments();
    fragment.number = args.getInt("number");
    fragment.name = args.getString("name");
  }
}
```