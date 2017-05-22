package org.binarytrio.wiseloan;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private static final String SERVER_URL = "http://40.71.220.240:8000/upload";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String PICTURE_FILENAME = "picture";
    public static final String JPG_EXT = ".jpg";

    private Button btPhoto;
    private Button btSend;
    private ImageView ivPhoto;
    private File photo;
    private RequestQueue requestQueue;
    private ProgressBar progressBar;
    private LinearLayout summaryLayout;
    private TextView tvMoney;
    private TextView tvPercentage;
    private TextView tvAgreement;
    private TextView tvTimer;
    private TextView tvRecommendation;
    private TextView tvTitle;
    private TextView tvSubtitle;
    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        requestQueue = Volley.newRequestQueue(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if(photo != null && photo.exists()) {
                Bitmap bitmap = getPhotoBitMap();
                ivPhoto.setImageBitmap(bitmap);
                show(btSend);
                show(ivPhoto);
            }
        }
    }

    private void initViews() {
        btPhoto = (Button) findViewById(R.id.bt_photo);
        btPhoto.setOnClickListener(getPhotoButtonListener());
        ivPhoto = (ImageView) findViewById(R.id.iv_photo);
        btSend = (Button) findViewById(R.id.bt_send);
        btSend.setOnClickListener(getSendButtonListener());
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        summaryLayout = (LinearLayout) findViewById(R.id.summary_layout);
        tvAgreement = (TextView) findViewById(R.id.tv_agreement);
        tvMoney = (TextView) findViewById(R.id.tv_money);
        tvPercentage = (TextView) findViewById(R.id.tv_percentage);
        tvTimer = (TextView) findViewById(R.id.tv_timer);
        tvRecommendation = (TextView) findViewById(R.id.tv_recommendation);
        tvTitle = (TextView) findViewById(R.id.title);
        tvSubtitle = (TextView) findViewById(R.id.subtitle);
        relativeLayout = (RelativeLayout) findViewById(R.id.layout_main);
    }

    @NonNull
    private View.OnClickListener getPhotoButtonListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                hide(summaryLayout);
                hide(tvTitle);
                hide(tvSubtitle);
                if(takePicIntent.resolveActivity(getPackageManager()) != null) {

                    try {
                        createImageFile();
                    } catch (Exception e) {
                    }

                    if (photo != null) {
                        Uri photoUri = FileProvider.getUriForFile(MainActivity.this, "com.example.android.fileprovider", photo);
                        takePicIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                        startActivityForResult(takePicIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        };
    }

    private void createImageFile() throws IOException {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        photo = File.createTempFile(PICTURE_FILENAME, JPG_EXT, storageDir);
    }

    public View.OnClickListener getSendButtonListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide(ivPhoto);
                show(progressBar);
                postPhoto();
                hide(btSend);
            }
        };
    }

    private void postPhoto() {
        if (photo != null && photo.exists()) {
//            String encodedImage =ImageEncoder.encodeImage(BitmapFactory.decodeFile(photo.getAbsolutePath()));
//            StringRequest postRequest = prepareImageRequest(encodedImage);
            MultipartRequest postRequest = prepareImageRequest();
            try {
                Log.i("Queue", "Posting image of length: " + postRequest.getBody().length);
            } catch (AuthFailureError authFailureError) {
                authFailureError.printStackTrace();
            }
//            Log.i("Queue", "Image: " + encodedImage);
            requestQueue.add(postRequest);
        }
    }

    private MultipartRequest prepareImageRequest() {
        MultipartRequest multipartRequest = new MultipartRequest(
                SERVER_URL,
                new HashMap<String, String>(),
                photo,
                PICTURE_FILENAME + JPG_EXT,
                "image",
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hide(progressBar);
                        Log.d("Error.Response", error.toString());
                    }
                },
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JsonObject responseObject = parse(response);
                        setSummary(responseObject);
                        hide(progressBar);
                        show(summaryLayout);
                        Log.d("Response", response);
                    }
                }
        );
        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(1000 * 180, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        return multipartRequest;
    }


//    private StringRequest prepareImageRequest(final String encodedImage) {
//        StringRequest stringRequest = new StringRequest(Request.Method.POST, SERVER_URL,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        // response
//                        hide(progressBar);
//                        Toast.makeText(MainActivity.this, "Upload successfull", Toast.LENGTH_SHORT).show();
//                        Log.d("Response", response);
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        // error
//                        hide(progressBar);
//                        Log.d("Error.Response", error.toString());
//                    }
//                }
//        ) {
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("image", encodedImage);
//
//                return params;
//            }
//        };
//        int socketTimeout = 30000;
//        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
//        stringRequest.setRetryPolicy(policy);
//        return stringRequest;
//    }


    private void hide(View view) {
        view.setVisibility(View.GONE);
    }

    private void show(View view) {
        view.setVisibility(View.VISIBLE);
    }

    private Bitmap getPhotoBitMap() {
        return BitmapFactory.decodeFile(photo.getAbsolutePath());
    }

    public JsonObject parse(String jsonLine) {
        return new JsonParser().parse(new JsonParser().parse(jsonLine).getAsString()).getAsJsonObject();
    }

    public void setSummary(JsonObject summary) {
        String amount = summary.get("AMOUNT").toString();
        String interest = summary.get("INTEREST").toString();
        String loan_months = summary.get("LOAN_MONTHS").toString();
        String total = summary.get("TOTAL").toString();

        tvAgreement.setText("You borrowed:\n"+ amount + " PLN");
        tvPercentage.setText("Your interest rate:\n" + interest + "%");
        tvTimer.setText("Contract length:\n" + loan_months + " months");
        tvMoney.setText("You will pay:\n" + total + " PLN");

        String recommendation_text = getRecommendationText(InterestRateReccomendation.findByInterest(Double.parseDouble(interest)));

        tvRecommendation.setText(recommendation_text);
//        tvAgreement.setText(summary.get("AMOUNT").toString());
//        tvAgreement.setTextSize(32);
//        tvPercentage.setText(summary.get("INTEREST").toString());
//        tvPercentage.setTextSize(32);
//        tvTimer.setText(summary.get("LOAN_MONTHS").toString());
//        tvTimer.setTextSize(32);
//        tvMoney.setText(summary.get("TOTAL").toString());
//        tvMoney.setTextSize(32);
    }

    private String getRecommendationText(InterestRateReccomendation interestRate) {
        switch (interestRate) {
            case HIGH_INTEREST:
                relativeLayout.setBackgroundColor(getResources().getColor(R.color.darkred));
                return "Warning, make sure its not a fraud!";
            case MEDIUM_INTEREST:
                relativeLayout.setBackgroundColor(getResources().getColor(R.color.darkyellow));
                return "Maybe you should reconsider it.";
            case LOW_INTEREST:
                relativeLayout.setBackgroundColor(getResources().getColor(R.color.bzdark));
                return "Contract and rules seem to be okay";
        }
        return null;
    }


}
