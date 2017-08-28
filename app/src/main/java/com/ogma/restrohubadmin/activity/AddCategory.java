package com.ogma.restrohubadmin.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.darsh.multipleimageselect.activities.AlbumSelectActivity;
import com.darsh.multipleimageselect.helpers.Constants;
import com.darsh.multipleimageselect.models.Image;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.ogma.restrohubadmin.R;
import com.ogma.restrohubadmin.application.App;
import com.ogma.restrohubadmin.application.AppSettings;
import com.ogma.restrohubadmin.enums.URL;
import com.ogma.restrohubadmin.network.NetworkConnection;
import com.ogma.restrohubadmin.utility.AppFileManager;
import com.ogma.restrohubadmin.utility.BitmapDecoder;
import com.ogma.restrohubadmin.utility.BitmapHelper;
import com.ogma.restrohubadmin.utility.UniversalImageLoaderFactory;
import com.yalantis.ucrop.UCrop;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AddCategory extends AppCompatActivity {

    public static final String EXTRA_EDIT_DATA = "extra_edit_data";
    private static final int MAX_UPLOAD_LIMIT = 1;
    private static final int REQUEST_TAKE_PHOTO = 131;

    private App app;
    private CoordinatorLayout coordinatorLayout;
    private ImageView ivBanner;
    private TextInputLayout tilName;
    private EditText etName;
    private String imagePath = "";
    private int imageOrientation = 0;
    private BitmapHelper bitmapHelper;
    private boolean isEdit;
    private String categoryId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        app = (App) getApplication();
        app.setAppSettings(new AppSettings(this));
        bitmapHelper = BitmapHelper.getInstance(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        ivBanner = (ImageView) findViewById(R.id.iv_cover);
        tilName = (TextInputLayout) findViewById(R.id.til_name);
        etName = (EditText) findViewById(R.id.et_name);

        if (getIntent().getStringExtra(EXTRA_EDIT_DATA) != null) {
            getSupportActionBar().setTitle("Edit Category");
            isEdit = true;
            String jsonStr = getIntent().getStringExtra(EXTRA_EDIT_DATA);
            try {
                JSONObject jsonObject = new JSONObject(jsonStr);
                categoryId = jsonObject.getString("id");
                String catName = jsonObject.getString("name");
                etName.setText(catName);
                String catImage = jsonObject.getString("image");
                ImageLoader imageLoader = new UniversalImageLoaderFactory.Builder(this).getInstance().initWithDefaultConfig().build();
                imageLoader.displayImage(catImage, ivBanner, UniversalImageLoaderFactory.getDefaultOptions(R.drawable.loader));
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "An internal error occurred", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void previewBanner(String filePath) {
        Bitmap bitmap = BitmapDecoder.decodeBitmapFromFile(filePath);
        if (bitmap != null) {
            ivBanner.setImageBitmap(bitmap);
        }
    }

    private boolean validateUpload() {
        if (TextUtils.isEmpty(imagePath) && !isEdit) {
            Snackbar.make(coordinatorLayout, "Please select an image to upload", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (etName.getText().toString().trim().length() == 0) {
            tilName.setError("Please enter a category name");
            Snackbar.make(coordinatorLayout, "Please enter a category name", Snackbar.LENGTH_SHORT).show();
            return false;
        } else {
            tilName.setErrorEnabled(false);
        }

        return true;
    }

    private boolean prepareExecuteAsync() {
        NetworkConnection connection = new NetworkConnection(this);
        if (connection.isNetworkConnected()) {
            return true;
        } else if (connection.isNetworkConnectingOrConnected()) {
            Snackbar.make(coordinatorLayout, "Connection temporarily unavailable", Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(coordinatorLayout, "You're offline", Snackbar.LENGTH_SHORT).show();
        }
        return false;
    }

    private void showOptions() {
        final String[] options = {"Gallery", "Camera"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options);
        new AlertDialog.Builder(this).setTitle("Choose Source").setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (options[which]) {
                    case "Gallery":
                        Intent intent = new Intent(AddCategory.this, AlbumSelectActivity.class);
                        //set limit on number of images that can be selected, default is 10
                        intent.putExtra(Constants.INTENT_EXTRA_LIMIT, MAX_UPLOAD_LIMIT);
                        startActivityForResult(intent, Constants.REQUEST_CODE);
                        break;
                    case "Camera":
                        dispatchTakePictureIntent();
                        break;
                    default:
                        break;
                }
            }
        }).show();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                showOptions();
                break;
            case R.id.btn_save:
                if (validateUpload() && prepareExecuteAsync()) {
                    new CreateCategoryTask().execute(etName.getText().toString().trim());
                }
                break;
            default:
                break;
        }
    }

    private void dispatchUCrop(String imagePath) {
        File file = new File(imagePath);
        UCrop.of(Uri.fromFile(file), Uri.fromFile(file))
                .withAspectRatio(4, 3)
                .start(this);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = AppFileManager.createImageFile(getString(R.string.app_name));
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                imagePath = photoFile.getAbsolutePath();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            //The array list has the image paths of the selected images
            ArrayList<Image> images = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
            for (Image image : images) {
                Log.e("Image path: ", image.path);
            }
            if (images.size() > 0) {
                //Since we know MAX_UPLOAD_LIMIT = 1
                imagePath = images.get(0).path;
                try {
                    File copied = AppFileManager.copyFileToAppDir(getString(R.string.app_name), Uri.parse(imagePath));
                    imagePath = copied.getAbsolutePath();
                    dispatchUCrop(imagePath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
//            imageOrientation = bitmapHelper.findOrientation(new File(imagePath));
//            previewBanner(imagePath);
        } else if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.setDataAndType(Uri.parse(mCapturedPhotoPath), "image/*");
//                startActivity(intent);

//                ivPhoto.setImageBitmap(rotateBitmap(BitmapDecoder.
//                        decodeSampledBitmapFromFile(mCapturedPhotoPath, 100, 100), orientationPhoto));

                Log.e("Captured Size:", new File(imagePath).length() / 1024 + "");
                Bitmap bitmap = BitmapDecoder.decodeBitmapFromFile(imagePath);
                bitmapHelper.saveCompressedBitmap(bitmap, 90, imagePath);
                Log.e("Compressed Size:", new File(imagePath).length() / 1024 + "");
                dispatchUCrop(imagePath);
//                imageOrientation = bitmapHelper.findOrientation(new File(imagePath));
//                bitmapHelper.rotateBitmap(bitmap, imageOrientation);
//                previewBanner(imagePath);

            } else {
                if (new File(imagePath).delete()) {
                    Log.e("File Operation", "File deleted successfully!");
                } else {
                    Log.e("File Operation", "Unable to delete file!");
                }
            }
        } else if (requestCode == UCrop.REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                final Uri resultUri = UCrop.getOutput(data);
                if (resultUri != null) {
                    File file = new File(resultUri.getPath());
                    imagePath = file.getAbsolutePath();
                    Log.e("onActivityResult: ", "cropped: " + imagePath);
                    previewBanner(imagePath);
                }
            } else if (resultCode == UCrop.RESULT_ERROR) {
                final Throwable cropError = UCrop.getError(data);
                if (cropError != null) {
                    cropError.printStackTrace();
                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.orders, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class CreateCategoryTask extends AsyncTask<String, Void, Boolean> {
        private ProgressDialog mDialog;
        private final String TAG = "UploadTask";
        private JSONObject responseObj;

        @Override
        protected void onPreExecute() {
            mDialog = new ProgressDialog(AddCategory.this);
            mDialog.setMessage("Please wait...");
            mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mDialog.setIndeterminate(true);
            mDialog.setCancelable(false);
            mDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                org.apache.http.client.HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost;
                if (isEdit) {
                    httpPost = new HttpPost(URL.EDIT_CATEGORY.getURL() + "/" + System.currentTimeMillis());
                } else {
                    httpPost = new HttpPost(URL.CREATE_CATEGORY.getURL() + "/" + System.currentTimeMillis());
                }

                MultipartEntity entity = new MultipartEntity(
                        HttpMultipartMode.BROWSER_COMPATIBLE);
                entity.addPart("user_id", new StringBody(app.getAppSettings().__uId));
                entity.addPart("restaurant_id", new StringBody(app.getAppSettings().__uRestaurantId));
                entity.addPart("name", new StringBody(params[0]));

                if (isEdit) {
                    entity.addPart("category_id", new StringBody(categoryId));
                    if (TextUtils.isEmpty(imagePath)) {
                        entity.addPart("upload_image", new StringBody(""));
                    } else {
                        entity.addPart("upload_image", new FileBody(new File(imagePath)));
                    }
                } else {
                    entity.addPart("upload_image", new FileBody(new File(imagePath)));
                }

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                entity.writeTo(bytes);
                String content = bytes.toString();
                Log.e("send:", content);

                httpPost.setEntity(entity);
                HttpResponse response;
                response = httpClient.execute(httpPost);

                HttpEntity resEntity = response.getEntity();
                final String response_str = EntityUtils.toString(resEntity, HTTP.UTF_8);
                Log.e("Response", response_str);

                responseObj = new JSONObject(response_str);
                Log.e("Status", String.valueOf(responseObj.getInt("is_error")));

                return responseObj.getInt("is_error") == 0;
            } catch (JSONException e) {
                e.printStackTrace();
                mDialog.dismiss();
                return false;
            } catch (NullPointerException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean status) {
            mDialog.dismiss();
            if (status) {
                Snackbar snackbar = Snackbar.make(coordinatorLayout, "Category created successfully", Snackbar.LENGTH_SHORT);
                snackbar.setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        super.onDismissed(snackbar, event);
                        AddCategory.this.finish();
                    }
                });
                snackbar.show();
            } else {
                Snackbar.make(coordinatorLayout, "An error occurred please try again later", Snackbar.LENGTH_SHORT).show();
            }
        }
    }
}
