package jdulal.com.np.uploadimageexample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    Button btnImageUpload;
    ImageView imageView;
    Bitmap bitmap;
    EditText imgTags;
    private static final int PERMISSION_REQUEST=1;
    private static final int IMAGE_REQUEST=2;
    private static final String UPLOAD_IMAGE_URL="http://192.168.0.102/bracesdev/androidbackend/uploadimage.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnImageUpload=findViewById(R.id.upload);
        imageView=findViewById(R.id.imageview);
        imgTags=findViewById(R.id.imgTags);

        imageView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                displayFileChoose();
            }
        });
        btnImageUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UploadImage();
            }
        });

        if(Build.VERSION.SDK_INT>=23){
            if(checkPermission()){

            }else{
                requestPermission();
            }
        }
    }

    private void requestPermission()
    {
        if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            Toast.makeText(MainActivity.this, "Please allow this permission in App settings.", Toast.LENGTH_LONG).show();
        }else{
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
        }
    }

    public boolean checkPermission() {
        int result=ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(result==PackageManager.PERMISSION_DENIED){
            return true;
        }else {
            return  false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_REQUEST:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public void displayFileChoose()
    {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "SELECT IMAGE"), IMAGE_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==IMAGE_REQUEST && resultCode==RESULT_OK && data !=null && data.getData() != null){
            Uri imgPath=data.getData();
            try{
                bitmap=MediaStore.Images.Media.getBitmap(getContentResolver(), imgPath);
                imageView.setImageBitmap(bitmap);
            }catch (IOException e){
                e.printStackTrace();
            }

        }
    }

    public void UploadImage()
    {
        StringRequest stringRequest=new StringRequest(Request.Method.POST, UPLOAD_IMAGE_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.contains("success"))
                {
                    Toast.makeText(MainActivity.this, "Image upload succeed.", Toast.LENGTH_LONG).show();
                    imageView.setImageResource(R.drawable.ic_launcher_foreground);
                    imgTags.setText("");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "" +error, Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
               Map<String, String> param= new HashMap<>();
               String images=getStringImage(bitmap);
               param.put("image", images);
               param.put("imgtags", imgTags.getText().toString());
               return param;
            }
        };
        Volley.newRequestQueue(MainActivity.this).add(stringRequest);
        Log.d("Request", stringRequest.toString());
    }
    public String getStringImage(Bitmap bitmap)
    {
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b= baos.toByteArray();
        String temp=Base64.encodeToString(b, Base64.DEFAULT);
        return  temp    ;
    }
}
