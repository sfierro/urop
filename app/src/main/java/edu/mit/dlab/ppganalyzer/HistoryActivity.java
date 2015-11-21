package edu.mit.dlab.ppganalyzer;

import android.app.Activity;
import android.os.Bundle;
import android.provider.MediaStore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates screen to view patient history
 */
public class HistoryActivity extends Activity {

    String myString;
    List<String> myList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyze);

        final Bundle extras = getIntent().getExtras();
//
//        for (int i = 0; i < getApplicationContext().getFilesDir().length(); i++) {
//
//            List<MediaStore.Files> files = new ArrayList<MediaStore.Files>(getApplicationContext().getFilesDir());
//
//        }
        FileInputStream fis = null;
        try {
            fis = getApplicationContext().openFileInput(extras.getString("id") + "-waveforms");
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
    }
}
