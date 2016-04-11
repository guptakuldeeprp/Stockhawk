package com.sam_chordas.android.stockhawk.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.widget.StockWidgetDataProvider;

public class StockWidgetService extends RemoteViewsService {
    public StockWidgetService() {
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        System.out.println("StockWidgetService onGetViewFactory called..");
        return new StockWidgetDataProvider(getApplicationContext(),intent);
    }
}
