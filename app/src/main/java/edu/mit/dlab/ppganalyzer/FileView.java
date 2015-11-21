package edu.mit.dlab.ppganalyzer;

import android.app.Activity;
import android.os.Bundle;
import android.text.Layout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Creates activity to view file contents
 */
public class FileView extends Activity{

    String myString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_view);

        Bundle extras = getIntent().getExtras();
        ScrollView layout = (ScrollView) findViewById(R.id.sv);
        TextView textView = new TextView(this);

        FileInputStream fis = null;
        try {
            fis = getApplicationContext().openFileInput(extras.getString("file_name"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader r = new BufferedReader(new InputStreamReader(fis));
        try {
            String line;
            while ((line = r.readLine()) != null) {
                myString += line;
                myString += "\n";
            }
            r.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        textView.setText(extras.getString("file_name") + "\n\n" + myString);
        layout.addView(textView);


    }

}
