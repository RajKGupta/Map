package com.example.rajk.geofiretrial3.main;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.rajk.geofiretrial3.R;

public class AuthenticatePhone extends AppCompatActivity {
private EditText phone;
    private Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticate_phone);
        phone = (EditText)findViewById(R.id.phone);
        button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number = phone.getText().toString().trim();
                boolean valid = true;
                if(number.length()==10)
                {
                    for(int i =0;i<number.length();i++)
                    {
                        if(number.indexOf(i)>='0'&&number.indexOf(i)<='9')
                        {
                         continue;
                        }
                        else
                        {
                            showToast("Number is not valid");
                            valid=false;
                            break;
                        }
                    }
                    if(valid)
                    {
                        validate(number);
                    }
                    }
                else
                {
                    showToast("Number is not valid");
                }
            }
        });
    }

    private void validate(String number) {
    }

    private  void showToast(String text)
    {
        Toast.makeText(AuthenticatePhone.this,text,Toast.LENGTH_SHORT).show();
    }
}
