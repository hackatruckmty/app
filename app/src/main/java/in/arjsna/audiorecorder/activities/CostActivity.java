package in.arjsna.audiorecorder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.text.DecimalFormat;

import in.arjsna.audiorecorder.R;

/**
 * Created by rodolfo on 26/11/17.
 */

public class CostActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceStated){
        super.onCreate(savedInstanceStated);
        setContentView(R.layout.activity_cost);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.cost_resume);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        //Log.i("Testing", savedInstanceStated.toString());
        Bundle b = getIntent().getExtras();

        Double expenses = b.getDouble("expenses");
        expenses += b.getDouble("fuel");
        expenses += b.getDouble("salary");
        String expensesStr = "$" + new DecimalFormat("#.##").format(expenses);

        TextView expensesView = (TextView) findViewById(R.id.cost);
        expensesView.setText(expensesStr);


        String title = "Costo del viaje: " + b.getString("origin") + ", " + b.getString("destination");
        TextView titleView = (TextView) findViewById(R.id.title);
        titleView.setText(title);

    }

    public void onClickLike(View view){
        //Bundle b = getIntent().getExtras();

        Intent intent = new Intent(this, MapsActivity.class);
        //intent.putExtra("points", b.getString("points"));
        startActivity(intent);
    }
}
