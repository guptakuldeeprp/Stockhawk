package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.QuoteCursorAdapter;
import com.sam_chordas.android.stockhawk.rest.RecyclerViewItemClickListener;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.melnykov.fab.FloatingActionButton;
import com.sam_chordas.android.stockhawk.touch_helper.SimpleItemTouchHelperCallback;

public class MyStocksActivity extends AppCompatActivity implements MyStocksFragment.OnFragmentInteractionListener{
    private static final String STOCK_DETAILS_FRAGMENT_TAG = StockDetailsFragment.class.getName();

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private Context mContext;
    private Intent mServiceIntent;
    boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_my_stocks);
        removeDetailsFragment();
        mTitle = getTitle();
        isConnected = Utils.isNetworkConnected(mContext);
        findViewById(R.id.fragment_stock_details).setVisibility(View.GONE);


        mServiceIntent = new Intent(this, StockIntentService.class);

        if(savedInstanceState == null) {
            System.out.println("savedInstanceState is null");
            mServiceIntent.putExtra("tag", "init");
            if (isConnected) {
                startService(mServiceIntent);
            } else {
                setNetworkEmptyView();
                //Utils.networkToast(this);
            }

            System.out.println("Adding my stocks fragment");

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_my_stocks, MyStocksFragment.newInstance())
                    .commit();
        } else {
            System.out.println("savedInstanceState is not null");
        }

    }


    public void setNetworkEmptyView() {
        TextView tv = (TextView) findViewById(R.id.empty_view);
        tv.setText(getString(R.string.no_network));
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_stocks, menu);
        restoreActionBar();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_change_units) {
            // this is for changing stock changes from percent value to dollar value
            Utils.showPercent = !Utils.showPercent;
            this.getContentResolver().notifyChange(QuoteProvider.Quotes.CONTENT_URI, null);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(String symbol) {
        if (Utils.isLargeDevice(mContext)) {

            findViewById(R.id.fragment_stock_details).setVisibility(View.VISIBLE);
            Bundle arguments = new Bundle();

            arguments.putString(getString(R.string.stock_symbol), symbol);

            StockDetailsFragment fragment = StockDetailsFragment.newInstance();
            fragment.setArguments(arguments);
            //getSupportFragmentManager().beginTransaction().replace(R.id.movie_details_fragment_inner, fragment).commit();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_stock_details, fragment, STOCK_DETAILS_FRAGMENT_TAG).commit();

        } else {
            Intent intent = new Intent(mContext, StockDetailsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(getString(R.string.stock_symbol), symbol);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    public void removeDetailsFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(STOCK_DETAILS_FRAGMENT_TAG);
        if (fragment != null) {
            fragment.setMenuVisibility(false);
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }

    }
}
