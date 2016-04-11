package com.sam_chordas.android.stockhawk.ui;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;

import com.sam_chordas.android.stockhawk.R;

/**
 * Created by kuldeep.gupta on 25-03-2016.
 */
public class StockDetailsActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_details);
        if(savedInstanceState == null) {
            String stockSymbol = getIntent().getExtras().getString(getString(R.string.stock_symbol));
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.stock_details_container, new StockDetailsFragment())
                    .commit();
        }
    }
}

