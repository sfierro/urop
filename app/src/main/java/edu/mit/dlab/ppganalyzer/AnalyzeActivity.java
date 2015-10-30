package edu.mit.dlab.ppganalyzer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Creates screen with analysis of PPG
 */
public class AnalyzeActivity extends Activity {

    private TextView hr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyze);
        Bundle extras = getIntent().getExtras();
        int num = (int) extras.getDouble("hr");
        hr = (TextView) findViewById(R.id.hr);
        hr.setText("" + num);

    }

}
