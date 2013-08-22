package com.adrguides;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.adrguides.model.Guide;
import com.adrguides.model.Place;

/**
 * Created by adrian on 19/08/13.
 */
public class GuideFragment extends Fragment {

    public final static String TAG = "GUIDE_FRAGMENT";

    private ListView listView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View v = inflater.inflate(R.layout.fragment_guide, container, false);
        listView = (ListView) v.findViewById(R.id.listguides);

        // Inflate the layout for this fragment
        return v;
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Guide guide = getArguments().getParcelable(ReadGuideActivity.ARG_GUIDE);

        ArrayAdapter adapter = new ArrayAdapter<Place>(this.getActivity(),
                android.R.layout.simple_list_item_1, guide.getPlaces());

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                LocationHandler lh = (LocationHandler) getActivity();
                lh.showPlace(guide.getPlaces()[i]);
            }
        });
        listView.setAdapter(adapter);
    }


//    vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//    public View getView(int position, View convertView, ViewGroup parent) {
//        View v = convertView;
//
//        Object cat = menu.get(position);
//        if (cat.getClass().equals(Category.class)) {
//            v = vi.inflate(R.layout.category, null);
//            Category item = (Category)cat;
//            v.setOnClickListener(null);
//            v.setOnLongClickListener(null);
//            v.setLongClickable(false);
//
//            TextView tt = (TextView) v.findViewById(R.id.category);
//            tt.setText(item.getName());
//
//        } else if (cat.getClass().equals(OrderItem.class)) {
//            v = vi.inflate(R.layout.menu, null);
//            OrderItem orderItem = (OrderItem)cat;
//            Item item = orderItem.getItem();
//            TextView tt = (TextView) v.findViewById(R.id.title);
//            tt.setText(item.getName());
//
//            TextView bt = (TextView) v.findViewById(R.id.desc);
//            bt.setText(item.getDescription());
//
//            TextView qty = (TextView) v.findViewById(R.id.qty);
//            qty.setId(item.getId());
//
//
//            ImageButton minus = (ImageButton) v.findViewById(R.id.qtyMinus);
//            minus.setTag(item);
//            ImageButton plus = (ImageButton) v.findViewById(R.id.qtyPlus);
//            plus.setTag(item);
//        }
//
//        return v;
//    }
}
