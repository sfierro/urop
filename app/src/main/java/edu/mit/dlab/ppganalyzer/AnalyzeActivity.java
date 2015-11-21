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
    TextView test;
    String myString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyze);

        final Bundle extras = getIntent().getExtras();
        int num = (int) extras.getDouble("hr");
        hr = (TextView) findViewById(R.id.hr);
//        hr.setText("" + num);

        FileInputStream fis = null;
        try {
//            extras.getString("id")+"-waveforms"
            fis = getApplicationContext().openFileInput("test");
            // have date from end of file name be passed through extra intent bundle
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader r = new BufferedReader(new InputStreamReader(fis));
        try {
            String line;
            while ((line = r.readLine()) != null) {
                myString += line;
            }
            r.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        test = (TextView) findViewById(R.id.test);
        test.setText(myString);

        Button exit = (Button)findViewById(R.id.quit_btn);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AnalyzeActivity.this.finish();
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

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
                Intent in = new Intent(AnalyzeActivity.this,InitialActivity.class);
                startActivity(in);
            }
        });

    }

}
