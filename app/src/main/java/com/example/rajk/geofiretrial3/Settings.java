package com.example.rajk.geofiretrial3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class Settings extends AppCompatActivity {
private Button submit;
    public float conversionFactor = 3.281F;
    public static String DistancePreference ="DistancePreference";
    public static String ConversionFactor="conversionFactor";
    public static String Unit ="Unit";
    public static String Setting ="Setting";
    private RadioGroup rG_selectUnit;
    private RadioButton setfeet,setMetre;
    private TextView tv_setDistance_2;
    private EditText et_setDistance;
    private SharedPreferences sharedPreferences; //= getSharedPreferences(Setting,MODE_PRIVATE);
    private SharedPreferences.Editor editor;// = sharedPreferences.edit();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        submit=(Button)findViewById(R.id.submit);
        sharedPreferences = getSharedPreferences(Setting,MODE_PRIVATE);
        editor = sharedPreferences.edit();
        rG_selectUnit = (RadioGroup)findViewById(R.id.rG_selectUnit);
        tv_setDistance_2 = (TextView)findViewById(R.id.tv_setDistance_2);
        et_setDistance = (EditText)findViewById(R.id.et_setDistance);
        tv_setDistance_2.setText(sharedPreferences.getString(Unit,"ft"));
        if(sharedPreferences.getString(Unit,"ft").equals("m")){

        }
        int x = sharedPreferences.getInt(DistancePreference,500);
        et_setDistance.setText(String.valueOf(x));
        setfeet = (RadioButton) findViewById(R.id.setFeet);
        setfeet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_setDistance_2.setText("ft");
            }
        });
        setMetre = (RadioButton)findViewById(R.id.setMetre);
        setMetre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_setDistance_2.setText("m");
            }
        });
        if(sharedPreferences.getString(Unit,"ft").equals("m")){
            setMetre.setChecked(true);
            setfeet.setChecked(false);
        }


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String unit  ="m";
                RadioButton rb = (RadioButton)findViewById(rG_selectUnit.getCheckedRadioButtonId());
                if(rb.getId()==R.id.setFeet)
                {
                    unit="ft";
                    conversionFactor=3.281F;
                }
                else
                {
                    unit="m";
                    conversionFactor=1.0F;
                }
                if(et_setDistance.getText().toString()!=null)
                {
                editor.putString(Unit, unit);
                editor.putFloat(ConversionFactor,conversionFactor);
                    int y = Integer.parseInt(et_setDistance.getText().toString());
                editor.putInt(DistancePreference,y);
                editor.commit();
                startActivity(new Intent(Settings.this,MapsActivity.class));
            }
            else
                {
                    Toast.makeText(Settings.this,"Enter the "+DistancePreference,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();


    }
}
