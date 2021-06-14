package com.escalon.JustChat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.auth.FirebaseAuth;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ImageExtraction extends AppCompatActivity {
    static final int CAMERA_REQUEST_CODE = 200;
    static final int STORAGE_REQUEST_CODE = 400;
    static final int IMAGE_PICK_GALLARY_CODE = 1000;
    static final int IMAGE_PICK_CAMERA_CODE = 1001;

    private EditText resultET;
    private ImageView image_preview;
    private Uri imageuri,resulturi;

    String CameraPermission[];
    String[] StoragePermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_extraction);
        resultET = findViewById(R.id.resultEt);
        image_preview = findViewById(R.id.image_preview);

        CameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        StoragePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        image_preview.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ImageExtraction.this);
                builder.setTitle("Save");
                builder.setMessage("Convert Image To Pdf And Save");
                builder.setIcon(R.drawable.ic_baseline_save_24);
                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final EditText filename = new EditText(view.getContext());
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(ImageExtraction.this);
                        builder1.setTitle("Give Name");
                        builder1.setMessage("Please give a Name to your file");
                        builder1.setView(filename);
                        builder1.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            if(TextUtils.isEmpty(filename.getText().toString())){
                                Toast.makeText(ImageExtraction.this, "Please give a file name", Toast.LENGTH_SHORT).show();
                            }
                            else
                            createnewpdf(view,filename.getText().toString());
                            }
                        });
                        builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        builder1.create().show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                builder.create().show();
                return false;
            }
        });

    }

    private void createnewpdf(View view, String filename) {
        PdfDocument document = new PdfDocument();
        // crate a page description
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(view.getWidth(), view.getHeight(), 1).create();
// start a page
        PdfDocument.Page page = document.startPage(pageInfo);
        // draw on page
        view.draw(page.getCanvas());
        // finish the page
        document.finishPage(page);
        // generate pdf
        new PdfGenerationTask(document,filename).execute();
    }

    private class  PdfGenerationTask  extends AsyncTask<Void, Void, File> {
        PdfDocument pdfDocument;
        String Filename;
        public PdfGenerationTask(PdfDocument document, String filename) {
            this.pdfDocument = document;
            this.Filename = filename;

        }
        final ProgressDialog progressDialog = new ProgressDialog(ImageExtraction.this);
        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Converting and Saving");
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected File doInBackground(Void... params) {
            return generatePdf(pdfDocument,Filename);
        }


        @Override
        protected void onPostExecute(File file) {

            /* Dismiss the progress dialog after sharing */
            Toast.makeText(ImageExtraction.this, "Pdf created in Internal Storage in Pdf Folder", Toast.LENGTH_SHORT).show();

            progressDialog.dismiss();
        }
    }

    private File generatePdf(PdfDocument pdfDocument, String filename){


        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyhhmmss");
        String pdfName = filename
                + sdf.format(Calendar.getInstance().getTime()) + ".pdf";


        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pdf";

        File dir = new File(path);
        if(!dir.exists())
            dir.mkdirs();


        File file = new File(dir, pdfName);


        if( pdfDocument != null ){

            // write the document content
            try {
                OutputStream out = new FileOutputStream(file);

                if( out != null ){

                    pdfDocument.writeTo(out);
                    // close the document
                    pdfDocument.close();
                    out.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return file;

    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.imagemenu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ImageExtraction.this,LoginActivity.class));
                finish();
                return true;
            case R.id.Back:
                startActivity(new Intent(ImageExtraction.this,MainActivity.class));
                finish();
                return true;
            case R.id.camera:
                showimageimportdialog();
                return true;
        }
        return false;
    }

    private void showimageimportdialog() {
        String[] items = {"Camera","Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_baseline_perm_identity_24);
        builder.setTitle("Select Image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i==0){
//                    open camera
                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    }
                    else{
                        pickimagefromCamera();
                    }
                }
                if(i==1){
//                    open gallery
                    if(!checkStoragePermission()){
                        requestStoragePermission();
                    }
                    else{
                        pickimagefromGallery();
                    }
                }
            }
        });
        builder.create().show();

    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);

        return result;
    }
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,StoragePermission,STORAGE_REQUEST_CODE);

    }

    private void pickimagefromGallery() {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent,IMAGE_PICK_GALLARY_CODE);
    }

    private void pickimagefromCamera() {

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Select Picture");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Image to Text");
        imageuri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageuri);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,CameraPermission,CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);

        return result&&result1;
    }

//    handeling permission result


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case CAMERA_REQUEST_CODE:
                if (grantResults.length>0){
                    boolean cameraAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted =grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && writeStorageAccepted){
                        pickimagefromCamera();
                    }
                    else {
                        Toast.makeText(this, "Permission Required", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case STORAGE_REQUEST_CODE:
                if (grantResults.length>0){
                    boolean StorageAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(StorageAccepted){
                        pickimagefromCamera();
                    }
                    else {
                        Toast.makeText(this, "Permission Required", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

//    handeling  image result


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Extract Image From Text");
        if (resultCode==RESULT_OK){
//                get image and crop
            if(requestCode==IMAGE_PICK_CAMERA_CODE){
                CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON).start(this);

            }
            if(requestCode==IMAGE_PICK_GALLARY_CODE){
                CropImage.activity(imageuri).setGuidelines(CropImageView.Guidelines.ON).start(this);

            }
        }
        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode==RESULT_OK){
                resulturi = result.getUri(); //get image uri
//                set image to image view
                image_preview.setImageURI(resulturi);

                BitmapDrawable  bitmapDrawable = (BitmapDrawable) image_preview.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();
                TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

                if(!textRecognizer.isOperational()){
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                }
                else{
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> items = textRecognizer.detect(frame);
                    StringBuilder stringBuilder = new StringBuilder();
//                    getting the text
                    for (int i = 0;i<items.size();i++){
                        TextBlock myItem = items.valueAt(i);
                        stringBuilder.append(myItem.getValue());
                        stringBuilder.append("\n");

                    }
                    /*set value to edit text*/
                    resultET.setText(stringBuilder.toString());
                }

            }
            else if(resultCode==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception exception = result.getError();
                Toast.makeText(this, "ERROR----------"+exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}