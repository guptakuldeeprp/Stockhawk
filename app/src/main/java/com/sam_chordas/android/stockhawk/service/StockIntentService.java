package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.Utils;

import java.io.IOException;

import yahoofinance.Stock;

/**
 * Created by sam_chordas on 10/1/15.
 */
public class StockIntentService extends IntentService {

    private static final String NA = "N/A";

    public StockIntentService() {
        super(StockIntentService.class.getName());
    }

    public StockIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(StockIntentService.class.getSimpleName(), "Stock Intent Service");
        StockTaskService stockTaskService = new StockTaskService(this);
        Bundle args = new Bundle();
        if (intent.getStringExtra("tag").equals("add")) {
            args.putString("symbol", intent.getStringExtra("symbol"));
            Bundle intentBundle = intent.getExtras();
            if (intentBundle != null) {
                Messenger messenger = (Messenger) intentBundle.get("messenger");
                Message msg = Message.obtain();
                Bundle msgBundle = new Bundle();
                try {
                    Stock stock = Utils.fetchStockDetails(intent.getStringExtra("symbol"), false);
                    if (stock != null) {
                        msgBundle.putBoolean(getString(R.string.error_exists), false);
                        Utils.setStockStatus(this, StockTaskService.STOCK_STATUS_OK);
                        if (stock.getName().equals(NA)) {
                            msgBundle.putBoolean(getString(R.string.symbol_exists), false);
                            //return;
                        }
                        else {
                            msgBundle.putBoolean(getString(R.string.symbol_exists), true);
                            // We can call OnRunTask from the intent service to force it to run immediately instead of
                            // scheduling a task.
                            stockTaskService.onRunTask(new TaskParams(intent.getStringExtra("tag"), args));

                        }
                    }
                } catch (IOException e) {
                    Utils.setStockStatus(this, StockTaskService.STOCK_STATUS_SERVER_DOWN);
                    msgBundle.putBoolean(getString(R.string.error_exists), true);
                }
                msg.setData(msgBundle);
                try {
                    messenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

        }

    }
}
