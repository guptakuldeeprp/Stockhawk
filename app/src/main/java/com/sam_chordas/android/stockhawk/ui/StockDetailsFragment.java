package com.sam_chordas.android.stockhawk.ui;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.ParcelableHistoricalQuote;
import com.sam_chordas.android.stockhawk.rest.AsyncTaskResult;
import com.sam_chordas.android.stockhawk.rest.Utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

/**
 * Created by kuldeep.gupta on 25-03-2016.
 */
public class StockDetailsFragment extends Fragment {
    private String stockSymbol;
    //private RotateAnimation rotate = null;
    private View pendingView = null;
    private static final int NUM_DATA_POINTS = 6;
    private ProgressDialog progressDialog = null;
    private ArrayList<ParcelableHistoricalQuote> histQuotes;

    public StockDetailsFragment() {
        /*rotate = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        rotate.setDuration(600);
        rotate.setRepeatMode(Animation.RESTART);
        rotate.setRepeatCount(Animation.INFINITE);*/


        /*ImageView imgView = new ImageView(getActivity());
        imgView.setImageResource(R.drawable.ic_popup_sync_1);
        pendingView = imgView;*/
    }

    public static StockDetailsFragment newInstance() {
        return new StockDetailsFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            handleSavedInstance(savedInstanceState);
        } else
            handleNewInstance();
        if (stockSymbol != null)
            getActivity().setTitle(stockSymbol);
    }

    private void handleSavedInstance(Bundle savedInstanceState) {
        stockSymbol = savedInstanceState.getString(getString(R.string.stock_symbol));
        histQuotes = savedInstanceState.getParcelableArrayList(getString(R.string.stock_hist_quotes));
    }

    private void handleNewInstance() {

        if (Utils.isLargeDevice(getContext())) {
            if (getArguments() != null)
                stockSymbol = getArguments().getString(getString(R.string.stock_symbol));
        } else {

            if (getActivity().getIntent().getExtras() != null)
                stockSymbol = getActivity().getIntent().getExtras().getString(getString(R.string.stock_symbol));
        }

    }

  /*  private void startProgressAnimation() {
        if (pendingView != null) {
            pendingView.startAnimation(rotate);
        }
    }*/

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(getString(R.string.stock_symbol), stockSymbol);
        if (histQuotes != null)
            outState.putParcelableArrayList(getString(R.string.stock_hist_quotes), histQuotes);

        super.onSaveInstanceState(outState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_line_graph, container, false);
        //  pendingView = rootView.findViewById(R.id.pendingView);
        if (stockSymbol != null && !"".equals(stockSymbol.trim())) {
            //    pendingView.setVisibility(View.VISIBLE);
            //   startProgressAnimation();
            if (histQuotes != null && !histQuotes.isEmpty()) {
                setLineChartView(rootView, histQuotes);
            } else {
                progressDialog = ProgressDialog.show(getActivity(), "",
                        "Loading. Please wait...", true);
                //if (Utils.isNetworkConnected(getContext())) {
                new AsyncFetchStock(rootView).execute(stockSymbol);
            }
            //}
        }
        return rootView;
    }

    private void setErrMsg(View rootView) {
        TextView errView = (TextView) rootView.findViewById(R.id.stock_err_view);
        errView.setVisibility(View.VISIBLE);
        errView.setText(getString(R.string.empty_list_server_down));
    }

    private ArrayList<ParcelableHistoricalQuote> getParcelableHistQuote(List<HistoricalQuote> histQuotes) {
        if (histQuotes == null)
            return null;
        ArrayList<ParcelableHistoricalQuote> parcelableHistoricalQuotes = new ArrayList<ParcelableHistoricalQuote>();
        for (HistoricalQuote hq : histQuotes) {
            ParcelableHistoricalQuote phq = new ParcelableHistoricalQuote();
            phq.setSymbol(hq.getSymbol());
            phq.setDate(hq.getDate());
            phq.setOpen(hq.getOpen());
            phq.setLow(hq.getLow());
            phq.setHigh(hq.getHigh());
            phq.setClose(hq.getClose());
            phq.setAdjClose(hq.getAdjClose());
            phq.setVolume(hq.getVolume());
            parcelableHistoricalQuotes.add(phq);
        }
        return parcelableHistoricalQuotes;
    }


    private void setLineChartView(View rootView, ArrayList<ParcelableHistoricalQuote> histQuotes) {
        if (histQuotes != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd");
            LineChartView lcView = (LineChartView) rootView.findViewById(R.id.linechart);
            LineSet lineSet = new LineSet();

            ListIterator<ParcelableHistoricalQuote> histItr
                    = histQuotes.listIterator(histQuotes.size());

            while (histItr.hasPrevious()) {
                HistoricalQuote stockHistory = histItr.previous();
                lineSet.addPoint(dateFormat.format(stockHistory.getDate().getTime()), Math.round(stockHistory.getClose().floatValue() * 100.0f) / 100.0f);
            }

                    /*for (HistoricalQuote stockHistory : stockAsyncTaskResult.getResult().getHistory()) {
                        //System.out.println("Adding historical data");

                        lineSet.addPoint(dateFormat.format(stockHistory.getDate().getTime()), stockHistory.getClose().floatValue());

                    }*/

            lineSet.setColor(getResources().getColor(R.color.material_red_700));
            lineSet.setFill(Color.WHITE);

            lcView.setHorizontalScrollBarEnabled(true);
            lcView.setVerticalScrollBarEnabled(true);
            lcView.addData(lineSet);
            lcView.setXAxis(true);
            lcView.setYAxis(true);
            //lcView.canScrollHorizontally(1);
            lcView.setHorizontalScrollBarEnabled(true);
            lcView.setVerticalScrollBarEnabled(true);
            lcView.canScrollVertically(1);
            //lcView.setTranslationY(50);
            //lcView.setTranslationX(50);
            lcView.refreshDrawableState();

            lcView.setVisibility(View.VISIBLE);
            lcView.setBackgroundColor(Color.WHITE);
            lcView.show();
        }

    }


    private class AsyncFetchStock extends AsyncTask<String, Void, AsyncTaskResult<Stock>> {
        private View rootView;

        public AsyncFetchStock(View rootView) {
            this.rootView = rootView;
        }

        @Override
        protected AsyncTaskResult<Stock> doInBackground(String... params) {
            String stockSym = params[0];
            try {
                Calendar to = Calendar.getInstance();
                Calendar from = Calendar.getInstance();
                from.add(Calendar.WEEK_OF_MONTH, -NUM_DATA_POINTS);
                Stock stock = YahooFinance.get(stockSym, from, to, Interval.WEEKLY);

                return new AsyncTaskResult<Stock>(stock);
            } catch (IOException e) {
                System.err.println("Error while fetching historical data for stock " + stockSym + ": " + e.getMessage());
                return new AsyncTaskResult<Stock>(null, e);
            }
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<Stock> stockAsyncTaskResult) {
            super.onPostExecute(stockAsyncTaskResult);
            progressDialog.dismiss();
            if (stockAsyncTaskResult.isSucessful()) {
                try {
                    List<HistoricalQuote> hq = stockAsyncTaskResult.getResult().getHistory();
                    histQuotes = getParcelableHistQuote(hq);
                    setLineChartView(rootView, histQuotes);
                } catch (IOException e) {
                    setErrMsg(rootView);
                }
            } else {
                setErrMsg(rootView);
            }

        }


      /*  private void showTooltip(Rect rect){
            // With the help of ``LayoutParams``you can set the position and size of your tooltip.
            Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(rect.width(), rect.height());
            layoutParams.leftMargin = rect.left;
            layoutParams.topMargin = rect.top;

            // The ``R.layout.tooltip``can take any shape you want (once again, you can see the
            // tooltip xml examples in the sample application).
            mBarTooltip = (TextView) getLayoutInflater().inflate(R.layout.tooltip, null);

            // Set the previous configured ``LayoutParams``to the inflated layout.
            mBarTooltip.setLayoutParams(layoutParams);

            // Pass the view to the chart
            chart.showTooltip(mBarTooltip);
        }*/


    }
}
