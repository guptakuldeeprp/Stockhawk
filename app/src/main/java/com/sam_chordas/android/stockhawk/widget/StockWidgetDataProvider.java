package com.sam_chordas.android.stockhawk.widget;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.data.StockDetails;
import com.sam_chordas.android.stockhawk.rest.Utils;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by kuldeep.gupta on 29-03-2016.
 */
public class StockWidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {

    private Context context;
    private List<StockDetails> stockDetailsList;
    private ProgressDialog progressDialog = null;

    public StockWidgetDataProvider(Context context, Intent intent) {
        this.context = context;
    }


    @Override
    public void onCreate() {
        /*progressDialog = ProgressDialog.show(context, "",
                "Loading. Please wait...", true);*/
        initData();

    }

    @Override
    public void onDataSetChanged() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return stockDetailsList.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        System.out.println("StockWidgetDataProvider getViewAt called: " + position);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.stock_widget_row);

        if(stockDetailsList != null && !stockDetailsList.isEmpty()) {
            remoteViews.setTextViewText(R.id.w_symbol, stockDetailsList.get(position).getSymbol());
            remoteViews.setTextViewText(R.id.w_bidPrice, stockDetailsList.get(position).getBidPrice());
            int sdk = Build.VERSION.SDK_INT;
            if (stockDetailsList.get(position).getIsUp() == 1) {
                if (sdk < Build.VERSION_CODES.JELLY_BEAN) {

                    //remoteViews.setInt(R.id.w_change, "setBackgroundDrawable", R.drawable.percent_change_pill_green);
                    //remoteViews.setInt(R.id.w_change, "setBackgroundColor", context.getColor(R.color.material_green_700));
                    remoteViews.setInt(R.id.w_change, "setBackgroundColor", ContextCompat.getColor(context,R.color.material_green_700));
                } else {
                    //remoteViews.setInt(R.id.w_change, "setBackground", R.drawable.percent_change_pill_green);
                    //remoteViews.setInt(R.id.w_change, "setBackgroundColor", context.getColor(R.color.material_green_700));
                    remoteViews.setInt(R.id.w_change, "setBackgroundColor", ContextCompat.getColor(context,R.color.material_green_700));

                }
            } else {
                if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                    //remoteViews.setInt(R.id.w_change, "setBackgroundDrawable", R.drawable.percent_change_pill_red);

                    //remoteViews.setInt(R.id.w_change, "setBackgroundColor", context.getColor(R.color.material_red_700));
                    remoteViews.setInt(R.id.w_change, "setBackgroundColor", ContextCompat.getColor(context,R.color.material_red_700));

                } else {
                   // remoteViews.setInt(R.id.w_change, "setBackground", R.drawable.percent_change_pill_red);
                    //remoteViews.setInt(R.id.w_change, "setBackgroundColor", context.getColor(R.color.material_red_700));
                    remoteViews.setInt(R.id.w_change, "setBackgroundColor", ContextCompat.getColor(context,R.color.material_red_700));
                }
            }
            if (Utils.showPercent) {
                remoteViews.setTextViewText(R.id.w_change, stockDetailsList.get(position).getPercChange());

            } else {
                remoteViews.setTextViewText(R.id.w_change, stockDetailsList.get(position).getChange());
            }
        }
        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    protected void initData() {
        Cursor cursor = context.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);

        stockDetailsList = new ArrayList<StockDetails>();
        while (cursor.moveToNext()) {
            StockDetails stockDetails = new StockDetails(cursor.getString(cursor.getColumnIndex(QuoteColumns.SYMBOL)), cursor.getString(cursor.getColumnIndex(QuoteColumns.BIDPRICE)), cursor.getString(cursor.getColumnIndex(QuoteColumns.PERCENT_CHANGE)), cursor.getString(cursor.getColumnIndex(QuoteColumns.CHANGE)), cursor.getInt(cursor.getColumnIndex(QuoteColumns.ISUP)));
            stockDetailsList.add(stockDetails);
        }
    }

}
