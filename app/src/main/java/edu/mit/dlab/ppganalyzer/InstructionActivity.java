package edu.mit.dlab.ppganalyzer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Creates the instruction screen
 */
public class InstructionActivity extends Activity {

    private Button continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction);

        continueButton = (Button) findViewById(R.id.continue_btn);
        continueButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent in = new Intent(InstructionActivity.this,MainActivity.class);
                    startActivity(in);
                }
            }
        );
    }

}
