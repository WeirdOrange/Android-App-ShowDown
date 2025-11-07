package com.example.showdown;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.MapboxMap;
import com.mapbox.search.autofill.AddressAutofill;
import com.mapbox.search.autofill.AddressAutofillResult;
import com.mapbox.search.autofill.AddressAutofillSuggestion;
import com.mapbox.search.autofill.Query;
import com.mapbox.search.ui.adapter.autofill.AddressAutofillUiAdapter;
import com.mapbox.search.ui.view.CommonSearchViewConfiguration;
import com.mapbox.search.ui.view.SearchResultsView;
import com.mapbox.search.ui.view.DistanceUnitType;

import java.util.List;

public class MapboxSearchDialog implements LifecycleOwner {

    public interface OnLocationSelectedListener {
        void onLocationSelected(String name, double latitude, double longitude);
    }

    private final Context context;
    private final OnLocationSelectedListener listener;
    private AlertDialog dialog;

    // UI
    private EditText etQuery;
    private SearchResultsView searchResultsView;
    private MapView mapPreview;
    private Button btnConfirm, btnCancel;

    // Mapbox
    private AddressAutofill addressAutofill;
    private AddressAutofillUiAdapter autofillAdapter;
    private MapboxMap mapboxMap;
    private boolean ignoreNextMapIdle = false;

    // Lifecycle (required by Mapbox extensions)
    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    public MapboxSearchDialog(@NonNull Context context,
                              @NonNull OnLocationSelectedListener listener) {
        this.context = context;
        this.listener = listener;
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
    }

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View root = LayoutInflater.from(context).inflate(R.layout.location_search_box, null);
        builder.setView(root);
        dialog = builder.create();

        bindViews(root);
        initMapbox();
        setupSearch();
        setupButtons();

        dialog.show();
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START);
    }

    private void bindViews(View root) {
        etQuery = root.findViewById(R.id.query_text);
        searchResultsView = root.findViewById(R.id.search_results_view);
        mapPreview = root.findViewById(R.id.map);
        btnConfirm = root.findViewById(R.id.btn_confirm_location);
        btnCancel = root.findViewById(R.id.btn_cancel);
    }

    private void initMapbox() {
        CommonSearchViewConfiguration commonConfig = new CommonSearchViewConfiguration(DistanceUnitType.METRIC);
        addressAutofill = AddressAutofill.create();
        searchResultsView.initialize(new SearchResultsView.Configuration(commonConfig));

        autofillAdapter = new AddressAutofillUiAdapter(searchResultsView, addressAutofill);
        autofillAdapter.addSearchListener(new AddressAutofillUiAdapter.SearchListener() {
            @Override
            public void onSuggestionSelected(AddressAutofillSuggestion suggestion) {
                selectSuggestion(suggestion);
            }

            @Override public void onSuggestionsShown(List<AddressAutofillSuggestion> list) {}
            @Override public void onError(Exception e) {
                Toast.makeText(context, "Search error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // 2. Map preview
        mapboxMap = mapPreview.getMapboxMap();
        mapboxMap.loadStyle("mapbox://styles/<user>/<style");

        // Update map when user drags (pin-correction)
//        mapPreview.getMapboxMaps().getGestures().addOnCameraIdleListener(() -> {
//            if (ignoreNextMapIdle) {
//                ignoreNextMapIdle = false;
//                return;
//            }
//            reverseGeocode(mapboxMap.getCameraState().getCenter());
//        });
    }

    private void setupSearch() {
        etQuery.addTextChangedListener(new TextWatcher() {
            private boolean ignoreUpdate = false;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (ignoreUpdate) { ignoreUpdate = false; return; }

                Query query = Query.create(s.toString());
                if (query != null) {
                    autofillAdapter.search(query);
                    searchResultsView.setVisibility(View.VISIBLE);
                } else {
                    searchResultsView.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setupButtons() {
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            // Confirm is enabled only after a valid suggestion is selected
            if (selectedResult != null) {
                listener.onLocationSelected(
                        selectedResult.getSuggestion().getFormattedAddress(),
                        selectedResult.getCoordinate().latitude(),
                        selectedResult.getCoordinate().longitude()
                );
                dialog.dismiss();
            } else {
                Toast.makeText(context, "Please select a valid location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private AddressAutofillResult selectedResult = null;

    private void selectSuggestion(AddressAutofillSuggestion suggestion) {
        addressAutofill.select(suggestion).onValue(result -> {
            selectedResult = result;
            showResult(result, false);
        }).onError(e -> Toast.makeText(context, "Select error", Toast.LENGTH_SHORT).show());
    }

    private void reverseGeocode(Point point) {
        addressAutofill.reverse(point).onValue(results -> {
            if (!results.isEmpty()) showResult(results.get(0), true);
        }).onError(e -> {/* ignore */});
    }

    private void showResult(AddressAutofillResult result, boolean fromReverse) {
        selectedResult = result;

        // Update UI
        etQuery.setText(result.getSuggestion().getFormattedAddress());
        etQuery.clearFocus();
        searchResultsView.setVisibility(View.GONE);

        // Move map
        if (!fromReverse) {
            mapboxMap.setCamera(new CameraOptions.Builder()
                    .center(result.getCoordinate())
                    .zoom(16.0)
                    .build());
            ignoreNextMapIdle = true;
        }
    }

    // -----------------------------------------------------------------------
    //  LifecycleOwner
    // -----------------------------------------------------------------------
    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return lifecycleRegistry;
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) dialog.dismiss();
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
    }
}