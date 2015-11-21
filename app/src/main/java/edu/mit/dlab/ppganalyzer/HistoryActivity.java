package edu.mit.dlab.ppganalyzer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Creates screen to view patient history
 */
public class HistoryActivity extends Activity {


    HashMap<ImageButton,String> folderMap;
    ImageButton imageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        folderMap = new HashMap<ImageButton,String>();

        final Bundle extras = getIntent().getExtras();
        TableLayout tr = (TableLayout) findViewById(R.id.table);

        File dir = getApplicationContext().getFilesDir();
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if (child.getName().matches(extras.getString("id")+"[^0-9].*")) {

                    ImageButton btn = new ImageButton(this);
                    btn.setBackgroundResource(R.drawable.file);

                    TableRow t = new TableRow(this);
                    TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
                    t.setLayoutParams(lp);
                    t.setGravity(Gravity.CENTER_VERTICAL);
                    t.setPadding(50, 50, 50, 50);
                    t.addView(btn);

                    TextView textView = new TextView(this);
                    textView.setText(child.getName());
                    textView.setPadding(10, 10, 10, 10);
                    t.addView(textView);

                    tr.addView(t);

                    folderMap.put(btn,child.getName());

                }
            }
        }

        for (ImageButton ib : folderMap.keySet()) {
            imageButton = ib;
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent in = new Intent(HistoryActivity.this,FileView.class);
                    in.putExtra("file_name",folderMap.get(imageButton));
                    startActivity(in);
                }
            });
        }

    }
}
