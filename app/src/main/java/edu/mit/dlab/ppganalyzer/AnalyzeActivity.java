package edu.mit.dlab.ppganalyzer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * Creates screen with analysis of PPG
 */
public class AnalyzeActivity extends Activity {

    private TextView hr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyze);

        final Bundle extras = getIntent().getExtras();
        int num = (int) extras.getDouble("hr");
        hr = (TextView) findViewById(R.id.hr);
        hr.setText("" + num);

        Button retest = (Button)findViewById(R.id.retest_btn);
        retest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(AnalyzeActivity.this,MainActivity.class);
                in.putExtras(extras);
                startActivity(in);
            }
        });

        Button startover = (Button)findViewById(R.id.start_over_btn);
        startover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName() );
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

    }

}
