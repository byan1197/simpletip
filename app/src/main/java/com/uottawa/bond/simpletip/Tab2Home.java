package com.uottawa.bond.simpletip;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.Rating;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by Bond on 2017-05-17.
 */

public class Tab2Home extends Fragment {

    Spinner spinner;
    ArrayAdapter<CharSequence> adapter;
    EditText numPeopleEdit, billAmount, tipInput;
    TextView tipPrompt, servicePrompt, curTextView,percentTextView, swipeTextV;
    Button addPpl, subPpl;
    LinearLayout input;
    int numPpl;
    RatingBar serviceRate;

    OnDataSetListener odsl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab2home, container, false);

        //Spinner and adapter
        spinner = (Spinner)rootView.findViewById(R.id.tipSpinner);
        adapter = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.tipArray, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        //views
        tipInput = (EditText) rootView.findViewById(R.id.tipInputEdit);
        serviceRate=(RatingBar) rootView.findViewById(R.id.serviceBar);
        tipPrompt = (TextView) rootView.findViewById(R.id.tipSuggestPrompt);
        servicePrompt = (TextView) rootView.findViewById(R.id.serviceRatePrompt);
        numPeopleEdit = (EditText) rootView.findViewById(R.id.numPeople);
        subPpl = (Button) rootView.findViewById(R.id.subPplBtn);
        billAmount = (EditText)rootView.findViewById(R.id.billEditText);
        addPpl = (Button) rootView.findViewById(R.id.addPplBtn);
        curTextView = (TextView) rootView.findViewById(R.id.curText);
        percentTextView = (TextView) rootView.findViewById(R.id.percent);
        swipeTextV = (TextView) rootView.findViewById(R.id.swipeTV);
        input = (LinearLayout) rootView.findViewById(R.id.input_container);
        input.setVisibility(View.GONE);

        //input filter for decimal places
        billAmount.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(5,2)});
        tipInput.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(4,1)});

        //Starting with default
        SharedPreferences sp = rootView.getContext().getSharedPreferences("defaultValues", rootView.getContext().MODE_PRIVATE);
        int savedTip = (sp.contains("tip")? sp.getInt("tip", 0): 0);
        spinner.setSelection(savedTip);
        int savedCur= (sp.contains("currency")? sp.getInt("currency", 0): 0);

        //TODO
        //Maybe take this out??????
        final SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("changed", false);

        if (savedCur ==0) {
            curTextView.setText("$");
        }
        else if (savedCur == 1) {
            curTextView.setText("£");
        }
        else {
            curTextView.setText("€");
        }

        //listeners
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0){
                    serviceRate.setVisibility(View.VISIBLE);
                    tipPrompt.setVisibility(View.VISIBLE);
                    servicePrompt.setVisibility(View.VISIBLE);
                    percentTextView.setVisibility(View.VISIBLE);
                    //percentTextView.setVisibility(View.INVISIBLE);
                    input.setVisibility(View.GONE);
                }
                else if (position == 1){
                    tipPrompt.setVisibility(View.GONE);
                    servicePrompt.setVisibility(View.GONE);
                    serviceRate.setVisibility(View.GONE);
                    input.setVisibility(View.VISIBLE);
                    percentTextView.setVisibility(View.INVISIBLE);
                }
                else{
                    tipPrompt.setVisibility(View.GONE);
                    servicePrompt.setVisibility(View.GONE);
                    serviceRate.setVisibility(View.GONE);
                    percentTextView.setText("%");
                    percentTextView.setVisibility(View.VISIBLE);
                    input.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        swipeTextV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    odsl.calculate();
                }
                return true;
            }
        });

        serviceRate.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                percentTextView.setText(String.valueOf(serviceRate.getRating() * 2 + 10) +"%");
            }
        });

        addPpl.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                numPpl = Integer.parseInt(String.valueOf(numPeopleEdit.getText()));
                numPpl ++;
                numPeopleEdit.setText(Integer.toString(numPpl));
            }
        });

        subPpl.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                numPpl = Integer.parseInt(String.valueOf(numPeopleEdit.getText()));
                if (numPpl > 1)
                    numPpl--;
                else
                    numPpl = 1;
                numPeopleEdit.setText(Integer.toString(numPpl));
            }
        });

        return rootView;
    }
    public void setDefaults(int tipPos, int curPos){
        spinner.setSelection(tipPos);
        if (curPos ==0) {
            curTextView.setText("$");
        }
        else if (curPos == 1) {
            curTextView.setText("£");
        }
        else {
            curTextView.setText("€");
        }
    }

    //TODO
    //CHANGE FOR TOASTS
    public int noData(){
        if (billAmount.getText() == null || String.valueOf(billAmount.getText()).isEmpty() || String.valueOf(billAmount.getText())== "")
            return 0;
        else if (tipInput.getText() == null || String.valueOf(tipInput.getText()).isEmpty() || String.valueOf(tipInput.getText())== "")
            return 1;
        return 2;
    }

    public void transferInfo(){
        double bill, percentage;
        bill = Double.parseDouble(String.valueOf(billAmount.getText()));
        if (spinner.getSelectedItemPosition()==0)
            percentage = ((double)(serviceRate.getRating()) * 2.0 + 10.0)/100.0;
        else if (spinner.getSelectedItemPosition()==1)
            percentage = (Double.parseDouble(String.valueOf(tipInput.getText())))/100.0;
        else
            percentage = Double.parseDouble(String.valueOf(spinner.getSelectedItem()))/100;

        odsl.setData(bill, percentage, numPpl);
    }

    public interface OnDataSetListener {
        public void setData(double bill, double tip, int ppl);
        public void calculate ();
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        try {
            odsl = (OnDataSetListener) context;
        } catch (Exception e){}
    }

}
