package com.keniobyte.bruino.minsegapp.features.location_police_report;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.keniobyte.bruino.minsegapp.R;
import com.keniobyte.bruino.minsegapp.features.police_report.PoliceReportActivityBeta2;
import com.keniobyte.bruino.minsegapp.model.PoliceReport;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import nucleus.factory.RequiresPresenter;

@RequiresPresenter(LocationPoliceReportPresenter.class)
public class LocationPoliceReportActivity extends AppCompatActivity implements ILocationPoliceReportView, OnMapReadyCallback{
    private final String TAG = getClass().getSimpleName();
    private final double LAT = -26.8167;
    private final double LNG = -65.2167;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.addressAutoCompleteTextView) AutoCompleteTextView addressAutoCompleteTextView;
    @BindView(R.id.nextStepButton) Button nextStepButton;

    @BindDrawable(R.drawable.ic_arrow_back_white_24dp) Drawable arrowBack;

    @BindString(R.string.incident_location) String titleIncidentLocation;
    @BindString(R.string.selected_map_drug) String titleDrugsLocation;
    @BindString(R.string.selected_map_aircraft) String titleAircraftLocation;
    @BindString(R.string.required) String subtitleRequired;
    @BindString(R.string.optional) String subtitleOptional;
    @BindString(R.string.title_attention) String titleWarning;
    @BindString(R.string.text_reverse_geocode) String messageReverseGeocode;
    @BindString(R.string.accept) String ok;

    private Context context = this;
    private LocationPoliceReportPresenter presenter;

    private GoogleMap googleMap;
    private LatLng locationPoliceReport = null;
    private ArrayList resultAddressList = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_police_report);
        ButterKnife.bind(this);

        setTitleToolbar();
        toolbar.setNavigationIcon(arrowBack);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ArrayAdapter adapter = new ArrayAdapter<ArrayList>(context, R.layout.list_item, resultAddressList);
        addressAutoCompleteTextView.setAdapter(adapter);
        addressAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.i(TAG, editable.toString());
                if (!editable.toString().isEmpty()){
                    try {
                        presenter.resultAddressList(editable.toString());
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        addressAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                presenter.onItemSelectedAddressAutoCompleteTextView(adapterView, position);
            }
        });

        if (googleMap == null){
            MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }

        presenter = new LocationPoliceReportPresenter(context);
        presenter.addView(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.unregisterReceiver();
    }

    @OnClick(R.id.nextStepButton)
    public void onClickNextStepButton(){
        presenter.nextStep();
    }

    public String getTypePolicReport() {
        return getIntent().getExtras().getString("type_report");
    }

    @Override
    public ArrayAdapter createArrayAdapter() {
        return new ArrayAdapter<ArrayList>(context, R.layout.list_item, resultAddressList);
    }

    @Override
    public void setEnableNextStepButton(boolean i) {
        nextStepButton.setEnabled(i);
    }

    @Override
    public void addMarkerInGoogleMap(LatLng latLng) {
        googleMap.clear();
        googleMap.addMarker(new MarkerOptions().position(latLng));
    }

    @Override
    public void setTextAutoCompleteTextView(String string) {
        addressAutoCompleteTextView.setText(string);
    }

    @Override
    public void setResultAddressList(ArrayList resultAddressList) {
        this.resultAddressList = resultAddressList;
    }

    @Override
    public void setLocationPoliceReport(LatLng location) {
        this.locationPoliceReport = location;
    }

    @Override
    public void setAdapterAutoCompleteTextView(ArrayAdapter adapter) {
        addressAutoCompleteTextView.setAdapter(adapter);
    }

    @Override
    public void navigationToPoliceReportActivity() {
        startActivity(new Intent(context, PoliceReportActivityBeta2.class)
                .putExtra("type_report", getTypePoliceReport())
                .putExtra("latitude", locationPoliceReport.latitude)
                .putExtra("longitude", locationPoliceReport.longitude)
                .putExtra("address", addressAutoCompleteTextView.getText().toString()));
    }

    @Override
    public void geocodingReverseMessageError() {
        new AlertDialog.Builder(context)
                .setTitle(titleWarning)
                .setMessage(messageReverseGeocode)
                .setCancelable(true)
                .setPositiveButton(ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(LAT, LNG), 11));
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        this.googleMap = googleMap;
        this.googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                presenter.onMapClick(latLng);
            }
        });
    }

    public String getTypePoliceReport() {
        return getIntent().getExtras().getString("type_report");
    }

    private void setTitleToolbar() {
        switch (getTypePoliceReport()) {
            case PoliceReport.TYPE_POLICE_REPORT_AFFAIR:
                toolbar.setTitle(subtitleOptional);
                toolbar.setSubtitle(titleIncidentLocation);
                setEnableNextStepButton(true);
                break;
            case PoliceReport.TYPE_POLICE_REPORT_AIRCRAFT:
                toolbar.setSubtitle(titleAircraftLocation);
                toolbar.setTitle(subtitleRequired);
                break;
            case PoliceReport.TYPE_POLICE_REPORT_DRUGS:
                toolbar.setTitle(subtitleRequired);
                toolbar.setSubtitle(titleDrugsLocation);
                break;
            case PoliceReport.TYPE_POLICE_REPORT_ONLINE:
                toolbar.setTitle(subtitleRequired);
                toolbar.setSubtitle(titleIncidentLocation);
                break;
        }
    }
}
