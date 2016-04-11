package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.service.StockTaskService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

    private static final String TRUE = "true";
    private static String LOG_TAG = Utils.class.getSimpleName();
    public static boolean showPercent = true;

    public static ArrayList<ContentProviderOperation> stocksToContentVals(Map<String, Stock> stocks) {
        if (stocks != null) {
            ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
            for (Stock stock : stocks.values()) {
                batchOperations.add(buildBatchOperation(stock));
            }
            return batchOperations;
        }
        return null;
    }

    public static ArrayList quoteJsonToContentVals(String JSON) {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        try {
            jsonObject = new JSONObject(JSON);
            if (jsonObject != null && jsonObject.length() != 0) {
                jsonObject = jsonObject.getJSONObject("query");
                int count = Integer.parseInt(jsonObject.getString("count"));
                if (count == 1) {
                    jsonObject = jsonObject.getJSONObject("results")
                            .getJSONObject("quote");
                    batchOperations.add(buildBatchOperation(jsonObject));
                } else {
                    resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

                    if (resultsArray != null && resultsArray.length() != 0) {
                        for (int i = 0; i < resultsArray.length(); i++) {
                            jsonObject = resultsArray.getJSONObject(i);
                            batchOperations.add(buildBatchOperation(jsonObject));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }
        return batchOperations;
    }

    public static String truncateBidPrice(String bidPrice) {
        bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
        return bidPrice;
    }

    public static String truncateChange(String change, boolean isPercentChange) {
        String weight = change.substring(0, 1);
        String ampersand = "";
        if (isPercentChange) {
            ampersand = change.substring(change.length() - 1, change.length());
            change = change.substring(0, change.length() - 1);
        }
        change = change.substring(1, change.length());
        double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
        change = String.format("%.2f", round);
        StringBuffer changeBuffer = new StringBuffer(change);
        changeBuffer.insert(0, weight);
        changeBuffer.append(ampersand);
        change = changeBuffer.toString();
        return change;
    }

    public static ContentProviderOperation buildBatchOperation(Stock stock) {
        if (stock != null) {
            ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                    QuoteProvider.Quotes.CONTENT_URI);
            builder.withValue(QuoteColumns.SYMBOL, stock.getSymbol());
            builder.withValue(QuoteColumns.BIDPRICE, stock.getQuote().getBid().toString());
            builder.withValue(QuoteColumns.PERCENT_CHANGE, stock.getQuote().getChangeInPercent().toString());
            builder.withValue(QuoteColumns.CHANGE, stock.getQuote().getChange().toString());
            builder.withValue(QuoteColumns.ISCURRENT, 1);
            if (stock.getQuote().getChange().signum() == -1) {
                builder.withValue(QuoteColumns.ISUP, 0);
            } else {
                builder.withValue(QuoteColumns.ISUP, 1);
            }

            return builder.build();
        }
        return null;

    }

    public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.Quotes.CONTENT_URI);
        try {
            String change = jsonObject.getString("Change");
            builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
            builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
            builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
                    jsonObject.getString("ChangeinPercent"), true));
            builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
            builder.withValue(QuoteColumns.ISCURRENT, 1);
            if (change.charAt(0) == '-') {
                builder.withValue(QuoteColumns.ISUP, 0);
            } else {
                builder.withValue(QuoteColumns.ISUP, 1);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return builder.build();
    }

    public static List<String> fetchStockSymbols(Context context, boolean defaultIfAbsent) {
        List<String> stockSymbols = new ArrayList<String>();
        Cursor initQueryCursor = context.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{"Distinct " + QuoteColumns.SYMBOL}, null,
                null, null);
        if (initQueryCursor != null || initQueryCursor.getCount() != 0) {
            initQueryCursor.moveToFirst();
            for (int i = 0; i < initQueryCursor.getCount(); i++) {
                stockSymbols.add(initQueryCursor.getString(initQueryCursor.getColumnIndex("symbol")));
                initQueryCursor.moveToNext();
            }
        }
        return stockSymbols;
    }

    public static Stock fetchStockDetails(String symbol, boolean includeHistorical) throws IOException {
        return YahooFinance.get(symbol, includeHistorical);
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public static boolean isLargeDevice(Context context) {
        boolean isLargeDev = (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
        if (isLargeDev)
            return true;

        return context.getString(R.string.large_dev).equals(TRUE); // checks for landscape mode as well
    }

    public static void networkToast(Context context) {
        makeToast(context, context.getString(R.string.no_network));
    }

    public static void makeToast(Context context, String msg) {
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
        toast.show();
    }

    @SuppressWarnings("ResourceType")
    public static
    @StockTaskService.NavigationMode
    int getStockStatus(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(context.getString(R.string.stock_status), StockTaskService.STOCK_STATUS_UNKNOWN);
    }

    public static void setStockStatus(Context context, @StockTaskService.NavigationMode int status) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(context.getString(R.string.stock_status), status);
        editor.commit();
    }

    public static boolean hasErr(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(context.getString(R.string.stock_status), -1) != StockTaskService.STOCK_STATUS_OK;
    }

    public static String getErrMsg(Context context) {

        /*if (!isNetworkConnected(context))
            return context.getString(R.string.no_network);*/

        @StockTaskService.NavigationMode int status = getStockStatus(context);
        switch (status) {
            case StockTaskService.STOCK_STATUS_SERVER_DOWN:
                return context.getString(R.string.empty_list_server_down);
            case StockTaskService.STOCK_STATUS_SERVER_INVALID:
                return context.getString(R.string.empty_list_server_error);
            /*case StockTaskService.STOCK_STATUS_UNKNOWN:
                return context.getString(R.string.empty_list_server_unknown);*/
            default:
                if (!isNetworkConnected(context))
                    return context.getString(R.string.no_network);
        }

        return null;
    }
}
