package es.craftsmanship.toledo.katangapp.activities;

import es.craftsmanship.toledo.katangapp.models.BusStopResult;
import es.craftsmanship.toledo.katangapp.models.QueryResult;

import android.content.Intent;

import android.graphics.Color;

import android.os.Bundle;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.util.List;

/**
 * @author Cristóbal Hermida
 * @author Manuel de la Peña
 */
public class ShowStopsActivity extends BaseGeoLocatedActivity {

    private List<BusStopResult> busStopResults;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Subscribe
    public void busStopsReceived(QueryResult queryResult) {
        busStopResults.clear();

        List<BusStopResult> results = queryResult.getResults();

        busStopResults.addAll(results);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new StopsAdapter(busStopResults));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_show_stops);

        Intent intent = getIntent();

        if (intent.hasExtra("queryResult") &&
            (intent.getSerializableExtra("queryResult") != null)) {

            QueryResult queryResult = (QueryResult) intent.getSerializableExtra("queryResult");

            busStopResults = queryResult.getResults();

            if (busStopResults.isEmpty()) {
                processEmptyResults();

                return;
            }

            recyclerView = (RecyclerView) findViewById(R.id.stops);

            swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

            initializeSwipeRefreshLayout();

            busStopsReceived(queryResult);
        }
        else {
            processEmptyResults();
        }
    }

    private void initializeSwipeRefreshLayout() {
        swipeRefreshLayout.setColorSchemeColors(
            Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW);

        swipeRefreshLayout.setRefreshing(true);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
            }

        });

        final LinearLayoutManager layoutParams = new LinearLayoutManager(this);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                swipeRefreshLayout.setEnabled(
                    layoutParams.findFirstCompletelyVisibleItemPosition() == 0);
            }

        });
    }

    private void processEmptyResults() {
        Toast.makeText(
            ShowStopsActivity.this, getString(R.string.bustop_results_empty), Toast.LENGTH_SHORT
        ).show();

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);

        startActivity(intent);
    }

}