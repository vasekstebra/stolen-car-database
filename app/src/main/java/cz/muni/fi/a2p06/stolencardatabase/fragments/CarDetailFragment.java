package cz.muni.fi.a2p06.stolencardatabase.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.VerificationError;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.muni.fi.a2p06.stolencardatabase.R;
import cz.muni.fi.a2p06.stolencardatabase.entity.Car;
import cz.muni.fi.a2p06.stolencardatabase.entity.Coordinates;
import cz.muni.fi.a2p06.stolencardatabase.utils.HelperMethods;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CarDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CarDetailFragment extends Fragment implements Step, OnMapReadyCallback {
    private static final String TAG = "CarDetailFragment";
    private Car mCar;
    private OnCarDetailFragmentInteractionListener mListener;

    @BindView(R.id.car_detail_photo)
    ImageView mPhoto;
    @BindView(R.id.car_detail_manufacturer_and_model)
    TextView mManufacturerAndModel;
    @BindView(R.id.car_detail_regno)
    TextView mRegno;
    @BindView(R.id.car_detail_stolen_date)
    TextView mStolenDate;
    @BindView(R.id.car_detail_color)
    TextView mColor;
    @BindView(R.id.car_detail_vin)
    TextView mVin;
    @BindView(R.id.car_detail_production_year)
    TextView mProductionYear;
    @BindView(R.id.car_detail_production_year_text)
    TextView mProductionYearText;
    @BindView(R.id.car_detail_engine)
    TextView mEngine;
    @BindView(R.id.car_detail_engine_text)
    TextView mEngineText;
    @BindView(R.id.car_map_view)
    MapView mMapView;
    @BindView(R.id.delete_car)
    Button mDeleteButton;


    public CarDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param car car to display.
     * @return A new instance of fragment CarDetailFragment.
     */
    public static CarDetailFragment newInstance(Car car) {
        CarDetailFragment fragment = new CarDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(Car.class.getSimpleName(), car);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCar = getArguments().getParcelable(Car.class.getSimpleName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_car_detail, container, false);
        ButterKnife.bind(this, view);
        mMapView.onCreate(savedInstanceState);
        mMapView.setVisibility(View.GONE);
        if (mCar != null) {
            populateCarDetails();
            if (mCar.getLocation() != null) {
                mMapView.getMapAsync(this);
            }
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            Car car = savedInstanceState.getParcelable(Car.class.getSimpleName());
            updateCarView(car);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCarDetailFragmentInteractionListener) {
            mListener = (OnCarDetailFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCarDetailFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }


    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMapView != null) {
            mMapView.onDestroy();
        }
    }

    @OnClick(R.id.delete_car)
    public void onDeleteCarClick(View view) {
        deleteCar();
        if (mListener != null) {
            mListener.onDeleteCar();
        }
    }

    private void deleteCar() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("cars");
        Query query = ref.orderByChild("regno").equalTo(mCar.getRegno());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled", databaseError.toException());
            }
        });
    }

    public void updateCarView(Car car) {
        mCar = car;
        populateCarDetails();
    }

    private void populateCarDetails() {
        populateCarImage();
        toggleDeleteButtonVisibility();
        mManufacturerAndModel.setText(mCar.getManufacturer() + " " + mCar.getModel());
        mRegno.setText(HelperMethods.formatRegnoFromDB(mCar.getRegno()));
        mStolenDate.setText(HelperMethods.formatDate(mCar.getStolenDate()));
        mColor.setText(mCar.getColor());
        mVin.setText(mCar.getVin());
        if (mCar.getProductionYear() != null) {
            mProductionYear.setText(String.valueOf(mCar.getProductionYear()));
        } else {
            mProductionYear.setVisibility(View.GONE);
            mProductionYearText.setVisibility(View.GONE);
        }
        if (mCar.getEngine() != null) {
            mEngine.setText(mCar.getEngine());
        } else {
            mEngine.setVisibility(View.GONE);
            mEngineText.setVisibility(View.GONE);
        }
    }

    private void populateCarImage() {
        if (mCar.getPhotoUrl() != null) {
            Uri photoUri = Uri.parse(mCar.getPhotoUrl());
            DrawableTypeRequest drawableTypeRequest;

            if (photoUri.getScheme().equals("content")) {
                drawableTypeRequest = Glide.with(this).load(photoUri);
            } else {
                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(mCar.getPhotoUrl());
                drawableTypeRequest = Glide.with(this).using(new FirebaseImageLoader())
                        .load(storageReference);
            }

            drawableTypeRequest.asBitmap()
                    .placeholder(R.drawable.car_placeholder)
                    .centerCrop()
                    .into(mPhoto);
        } else {
            mPhoto.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(Car.class.getSimpleName(), mCar);
        super.onSaveInstanceState(savedInstanceState);
    }

    public Car getCar() {
        return mCar;
    }

    @Override
    public VerificationError verifyStep() {
        return null;
    }

    @Override
    public void onSelected() {

    }

    @Override
    public void onError(@NonNull VerificationError error) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Coordinates carPos = mCar.getLocation();
        if (googleMap != null && carPos != null) {
            LatLng pos = new LatLng(carPos.getLat(), carPos.getLon());
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(pos).title(mCar.getRegno()));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
            mMapView.setVisibility(View.VISIBLE);
        }
    }

    private void toggleDeleteButtonVisibility() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userUid = user.getUid();
            if (userUid.equals(mCar.getUserUid())) {
                mDeleteButton.setVisibility(View.VISIBLE);
            } else {
                mDeleteButton.setVisibility(View.GONE);
            }
        }
    }

    public interface OnCarDetailFragmentInteractionListener {
        void onDeleteCar();
    }
}
