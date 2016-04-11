package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.service.StockWidgetService;

/**
 * Implementation of App Widget functionality.
 */
public class StockWidget extends AppWidgetProvider {


    private RemoteViews initViews(Context context,
                          AppWidgetManager widgetManager, int widgetId) {

        System.out.println("StockWidget initViews called..");
        RemoteViews mView = new RemoteViews(context.getPackageName(),
                R.layout.stock_widget);
        Intent intent = new Intent(context, StockWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);

        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        //mView.setRemoteAdapter(widgetId, R.id.stock_collection_list, intent);
        mView.setRemoteAdapter(R.id.stock_collection_list, intent);

        return mView;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        System.out.println("StockWidget onUpdate called..");
        for (int widgetId : appWidgetIds) {
            RemoteViews mView = initViews(context, appWidgetManager, widgetId);
            appWidgetManager.updateAppWidget(widgetId, mView);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {

    }

    @Override
    public void onDisabled(Context context) {

    }
}

