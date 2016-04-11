package com.sam_chordas.android.stockhawk.ui;

import android.app.ActionBar;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.melnykov.fab.FloatingActionButton;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.QuoteCursorAdapter;
import com.sam_chordas.android.stockhawk.rest.RecyclerViewItemClickListener;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.sam_chordas.android.stockhawk.touch_helper.SimpleItemTouchHelperCallback;


/**
 * Created by kuldeep.gupta on 09-04-2016.
 */
public class MyStocksFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private OnFragmentInteractionListener mListener;
    //private CharSequence mTitle;
    private Intent mServiceIntent;
    private ItemTouchHelper mItemTouchHelper;
    private static final int CURSOR_LOADER_ID = 0;
    private QuoteCursorAdapter mCursorAdapter;
    private Context mContext;
    private Cursor mCursor;
    boolean isConnected;

    public MyStocksFragment() {
        // Required empty public constructor
    }


    public static MyStocksFragment newInstance() {
        MyStocksFragment fragment = new MyStocksFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        /*ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);*/

        //NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        //setContentView(R.layout.activity_my_stocks);
        isConnected = Utils.isNetworkConnected(mContext);
        // The intent service is for executing immediate pulls from the Yahoo API
        // GCMTaskService can only schedule tasks, they cannot execute immediately
        mServiceIntent = new Intent(mContext, StockIntentService.class);
        //mTitle = getActivity().getTitle();
        getActivity().getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
        mCursorAdapter = new QuoteCursorAdapter(mContext, null);
       /* if (savedInstanceState == null) {
            if (!setEmptyView()) {
                mServiceIntent.putExtra("tag", "init");
                startService(mServiceIntent);
            }
        }*/

        /*if (savedInstanceState == null) {
            // Run the initialize task service so that some stocks appear upon an empty database
            mServiceIntent.putExtra("tag", "init");
            if (isConnected) {
                getActivity().startService(mServiceIntent);
            } else {
                setNetworkEmptyView();
                //Utils.networkToast(this);
            }
        }*/
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        System.out.println("MyStocksFragment::onCreateView called");
        View rootView = inflater.inflate(R.layout.fragment_my_stocks, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        /*getActivity().getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        mCursorAdapter = new QuoteCursorAdapter(mContext, null);*/
        recyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(mContext,
                new RecyclerViewItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, int position) {

                        if (Utils.isNetworkConnected(mContext)) {
                            String stockSymbol = ((TextView) v.findViewById(R.id.stock_symbol)).getText().toString();
                            mListener.onFragmentInteraction(stockSymbol);
                            /*if (Utils.isLargeDevice(mContext)) {

                            } else {
                                Intent intent = new Intent(mContext, StockDetailsActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString(getString(R.string.stock_symbol), stockSymbol);
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }*/
                        } else {
                            Utils.networkToast(mContext);
                        }

                        //TODO:
                        // do something on item click
                    }
                }));
        recyclerView.setAdapter(mCursorAdapter);

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.attachToRecyclerView(recyclerView);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected) {
                    new MaterialDialog.Builder(mContext).title(R.string.symbol_search)
                            .content(R.string.content_test)
                            .inputType(InputType.TYPE_CLASS_TEXT)
                            .input(R.string.input_hint, R.string.input_prefill, new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(MaterialDialog dialog, CharSequence input) {
                                    // On FAB click, receive user input. Make sure the stock doesn't already exist
                                    // in the DB and proceed accordingly
                                    Cursor c = getActivity().getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                                            new String[]{QuoteColumns.SYMBOL}, QuoteColumns.SYMBOL + "= ?",
                                            new String[]{input.toString()}, null);
                                    if (c.getCount() != 0) {
                                        Utils.makeToast(mContext, getString(R.string.stock_exists_err_msg));
                                        return;
                                    } else {
                                        // Add the stock to DB
                                        Handler handler = new Handler() {
                                            @Override
                                            public void handleMessage(Message msg) {
                                                Bundle bundle = msg.getData();
                                                if (Utils.hasErr(mContext)) {

                                                    String errMsg = Utils.getErrMsg(mContext) == null ? getString(R.string.stock_save_err_msg) : Utils.getErrMsg(mContext);
                                                    Utils.makeToast(mContext, errMsg);
                                                    /*if(errMsg == null)
                                                        Utils.makeToast(MyStocksActivity.this, getString(R.string.stock_save_err_msg));
                                                    else
                                                        Utils.makeToast(MyStocksActivity.this, Utils.getErrMsg(MyStocksActivity.this));*/
                                                    return;
                                                }
                                                /*if (bundle.getBoolean(getString(R.string.error_exists), true)) {
                                                    Utils.makeToast(MyStocksActivity.this, getString(R.string.stock_save_err_msg));
                                                    return;
                                                }*/

                                                if (!bundle.getBoolean(getString(R.string.symbol_exists))) {
                                                    Utils.makeToast(mContext, getString(R.string.stock_symbol_invalid_err_msg));
                                                }

                                            }
                                        };
                                        mServiceIntent.putExtra("tag", "add");
                                        mServiceIntent.putExtra("messenger", new Messenger(handler));
                                        mServiceIntent.putExtra("symbol", input.toString());
                                        getActivity().startService(mServiceIntent);
                                    }
                                }
                            })
                            .show();
                } else {
                    Utils.networkToast(mContext);
                }
            }
        });

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mCursorAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

        if (isConnected) {
            long period = 3600L;
            long flex = 10L;
            String periodicTag = "periodic";

            // create a periodic task to pull stocks once every hour after the app has been opened. This
            // is so Widget data stays up to date.
            PeriodicTask periodicTask = new PeriodicTask.Builder()
                    .setService(StockTaskService.class)
                    .setPeriod(period)
                    .setFlex(flex)
                    .setTag(periodicTag)
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setRequiresCharging(false)
                    .build();
            // Schedule task with tag "periodic." This ensure that only the stocks present in the DB
            // are updated.
            GcmNetworkManager.getInstance(mContext).schedule(periodicTask);
        }
        return rootView;
    }

    /*@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.my_stocks, menu);
        restoreActionBar();
        return true;
    }

    public void restoreActionBar() {
        ActionBar actionBar =  getActivity().getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }*/

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public void setNetworkEmptyView() {
        TextView tv = (TextView) getView().findViewById(R.id.empty_view);
        tv.setText(getString(R.string.no_network));
    }

    public boolean setEmptyView() {
        TextView tv = (TextView) getView().findViewById(R.id.empty_view);
        if (mCursorAdapter != null) {
            if (mCursorAdapter.getItemCount() == 0) {
                String errMsg = Utils.getErrMsg(mContext);
                if (errMsg != null) {
                    tv.setText(errMsg);
                    tv.setVisibility(View.VISIBLE);
                    return true;
                }
            } else {
                tv.setVisibility(View.GONE);
            }
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This narrows the return to only the stocks that are most current.
        return new CursorLoader(mContext, QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
        mCursor = data;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {

        void onFragmentInteraction(String symbol);

        //void removeDetailsFragment();
    }
}
