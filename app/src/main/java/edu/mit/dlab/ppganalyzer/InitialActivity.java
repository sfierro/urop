package edu.mit.dlab.ppganalyzer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Creates the sign in screen
 */
public class InitialActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);

        final EditText name = (EditText) findViewById(R.id.name);
        final EditText id = (EditText) findViewById(R.id.id);

        Button history = (Button) findViewById(R.id.history);
        Button go = (Button) findViewById(R.id.go);

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (name.getText().toString().equals("") || id.getText().toString().equals("")) {
                    Toast.makeText(InitialActivity.this,"Please input name and ID",Toast.LENGTH_LONG).show();
                } else {
                    Intent in = new Intent(InitialActivity.this,HistoryActivity.class);
                    in.putExtra("name", name.getText().toString());
                    in.putExtra("id", id.getText().toString());
                    startActivity(in);
                }
            }
        });

        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (name.getText().toString().equals("") || id.getText().toString().equals("")) {
                    Toast.makeText(InitialActivity.this,"Please input name and ID",Toast.LENGTH_LONG).show();
                } else {
                    Intent in = new Intent(InitialActivity.this, InstructionActivity.class);
                    in.putExtra("name", name.getText().toString());
                    in.putExtra("id", id.getText().toString());
                    startActivity(in);
                }
            }
        });

    }

}
