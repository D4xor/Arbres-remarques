package com.esaip.arbresremarquables;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AjoutPhoto extends AppCompatActivity {

    private static final int REQUEST_TAKE_PHOTO = 1, GALLERY = 2;
    private String currentPath, inf;
    private File sourceFile, destFile;
    private ImageView ivPhoto;
    private Button btTakePhoto, btKeepPhoto,btChoosePhoto;
    private RadioButton rbYes, rbNo, rbType1,rbType2,rbType3;
    private LinearLayout infos;
    private Uri contentUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajout_photo);
        ivPhoto = findViewById(R.id.ivPhotoImage);
        btTakePhoto = findViewById(R.id.btPhotoTake);
        btKeepPhoto = findViewById(R.id.btPhotoKeep);
        btChoosePhoto = findViewById(R.id.btGalleryTake);
        infos = findViewById(R.id.infos);
        rbYes = findViewById(R.id.rdButOui);
        rbNo = findViewById(R.id.rdButNon);
        rbType1 = findViewById(R.id.arbres);
        rbType2 = findViewById(R.id.alignement);
        rbType3 = findViewById(R.id.espaceBoise);

        if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, R.string.no_camera_permission, Toast.LENGTH_LONG);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
            }
        } else {
            Toast.makeText(this, R.string.no_camera, Toast.LENGTH_LONG);
        }

        btTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePhoto(v);
            }
        });

        btKeepPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToAjout(v);
            }
        });

        btChoosePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePhotoFromGallery();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK){
            Bitmap bitmap = BitmapFactory.decodeFile(currentPath);
            bitmap = RotateBitmap(bitmap,90);
            ivPhoto.setImageBitmap(bitmap);
            galleryAddPic();
        }
        else if (requestCode == GALLERY && resultCode == Activity.RESULT_OK && data != null){
            contentUri =  data.getData();
            inf = getRealPathFromURI(contentUri);
            Toast.makeText(AjoutPhoto.this,inf,Toast.LENGTH_LONG).show();
            ivPhoto.setImageURI(contentUri);
            infos.setVisibility(View.VISIBLE);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void changePhoto(View view) {
        if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, R.string.no_camera_permission, Toast.LENGTH_LONG);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
            }
        } else {
            Toast.makeText(this, R.string.no_camera, Toast.LENGTH_LONG);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
                currentPath = photoFile.getAbsolutePath();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.esaip.arbresremarquables",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    //Creer le fichier contenant l'image
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_NoCompress_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",   /* suffix */
                storageDir      /* directory */
        );
        //Sauvegarder l'image
        return image;
    }

    //Rotation de l'image
    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    //Ajouter la photo prise depuis la caméra dans la galerie du téléphone
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
        infos.setVisibility(View.VISIBLE);
    }

    //Accès à un formulaire en fonction du choix effectué entre : arbre, alignement et espace boisé
    public void goToAjout(View view){
        Intent i = getIntent();
        if(rbType1.isChecked()) {
            Intent ajout = new Intent(getApplicationContext(), AjoutArbre.class);
            ajout.putExtra("photo", currentPath);
            if (rbYes.isChecked()) {
                ajout.putExtra("geolocalisation",true);
            }
            startActivity(ajout);
        }
        if (rbType2.isChecked()) {
            Intent ajout2 = new Intent(getApplicationContext(), AjoutAlignement.class);
            ajout2.putExtra("photo", currentPath);
            if (rbYes.isChecked()) {
                ajout2.putExtra("geolocalisation",true);
            }
            startActivity(ajout2);
        }
        if(rbType3.isChecked()){
            Intent ajout3 = new Intent(getApplicationContext(),AjoutEspaceBoise.class);
            ajout3.putExtra("photo",currentPath);
            if(rbYes.isChecked()){
                ajout3.putExtra("geolocalisation",true);
            }
            startActivity(ajout3);
        }
    }


    //Récupérer un photo depuis la gallerie
    public void choosePhotoFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY);
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    private void copyFile(File sourceFile, File destFile) throws IOException {
        if (!sourceFile.exists()) {
            return;
        }
        FileChannel source = null;
        FileChannel destination = null;
        source = new FileInputStream(sourceFile).getChannel();
        destination = new FileOutputStream(destFile).getChannel();
        if (destination != null && source != null) {
            destination.transferFrom(source, 0, source.size());
        }
        if (source != null) {
            source.close();
        }
        if (destination != null) {
            destination.close();
        }
    }
}