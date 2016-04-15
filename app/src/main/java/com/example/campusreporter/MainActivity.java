package com.example.campusreporter;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    /**
     * These are the variables used for camera, photo and email.
     */
    private static final int CAMERA_REQUEST = 100;
    private static final int EMAIL_REQUEST = 200;
    String imagePath;
    Button photoButton;

    Button sendButton;

    EditText message;
    //ImageView imageView;

    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    Intent sendEmail = new Intent(Intent.ACTION_SEND_MULTIPLE);

    /**
     * These are the variables used for location services.
     */

    private GoogleApiClient mGoogleApiClient;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private Location mLocation;
    double latitude;
    double longitude;

    /**
     * These are the variables used for audio.
     */

    private MediaRecorder mediaRecorder;
    Button recordButton;
    Button stopButton;
    Button playButton;
    String audioPath;

    ArrayList<Uri> uris = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        photoButton = (Button) findViewById(R.id.button1);
        sendButton = (Button) findViewById(R.id.button2);
        recordButton = (Button) findViewById(R.id.button3);
        stopButton = (Button) findViewById(R.id.button4);
        playButton = (Button) findViewById(R.id.button5);
        message = (EditText) findViewById(R.id.editText);
        //imageView = (ImageView) findViewById(R.id.imageView);



        /**
         * Checking for google play services.
         */
        if (checkPlayServices()) {
            /**
             *Building the google api client.
             */
            buildGoogleApiClient();
        }

        if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {

            photoButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    getLocation();

                    if (cameraIntent.resolveActivity(getPackageManager()) != null) {

                        File image = null;
                        try {
                            image = getImageFile();
                        } catch (IOException e) {
                            Toast.makeText(getApplicationContext(), "Please try again!", Toast.LENGTH_LONG).show();
                        }

                        if (image != null) {
                            Log.d("photoButton", image.getAbsolutePath());
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
                            startActivityForResult(cameraIntent, CAMERA_REQUEST);
                        }

                    }
                }

            });
        }

        sendButton.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View v) {

                                              sendEmail.setType("plain/text");
                                              sendEmail.putExtra(Intent.EXTRA_EMAIL, new String[]{"saveenergy@usu.edu"});
                                              sendEmail.putExtra(Intent.EXTRA_SUBJECT, "Campus Reporter");
                                              sendEmail.putExtra(Intent.EXTRA_TEXT, message.getText() + "\n\nLocation: " + "https://www.google.com/maps/?q=" + latitude + "," + longitude + "&z=17");
                                              if(!uris.isEmpty()){
                                                  sendEmail.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                                              }
                                              startActivityForResult(sendEmail, EMAIL_REQUEST);
                                          }
                                      }
        );




        /**
         * recordButton records the audio.
         * It calls the setUpAudio() to create the audio file and it's path.
         */

        recordButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                try{
                    setUpAudio();
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                }catch (IOException e){
                    e.printStackTrace();

                }catch(IllegalStateException e){
                    e.printStackTrace();
                }



                stopButton.setEnabled(true);
                Toast.makeText(getApplicationContext(), "Recording Started", Toast.LENGTH_SHORT).show();
            }


        });

        /**
         *  stopButton stops the audio recording.
         */
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaRecorder.stop();
                mediaRecorder.release();
                //mediaRecorder = null;

                stopButton.setEnabled(false);
                //play.setEnabled(true);

                File audio = new File(audioPath);

                uris.add(Uri.fromFile(audio));

                Toast.makeText(getApplicationContext(), "Audio recorded successfully",Toast.LENGTH_LONG).show();
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) throws IllegalArgumentException,SecurityException,IllegalStateException {
                MediaPlayer m = new MediaPlayer();

                try {
                    m.setDataSource(audioPath);
                }

                catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    m.prepare();
                }

                catch (IOException e) {
                    e.printStackTrace();
                }

                m.start();
                Toast.makeText(getApplicationContext(), "Playing audio", Toast.LENGTH_LONG).show();
            }
        });
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST) {

            if (resultCode == RESULT_OK) {
                galleryAddPic();
                File image = new File(imagePath);
                uris.add(Uri.fromFile(image));
                Toast.makeText(this, "Now, send the image!", Toast.LENGTH_SHORT).show();
                //sendButton.setEnabled(true);

                /**
                 ====================================================
                 Bitmap photo = (Bitmap) data.getExtras().get("data");
                 imageView.setImageBitmap(photo);

                 // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
                 tempUri = getImageUri(getApplicationContext(), photo);

                 // CALL THIS METHOD TO GET THE ACTUAL PATH
                 finalFile = new File(getRealPathFromURI(tempUri));

                 System.out.println(finalFile.getAbsolutePath());
                 ====================================================
                 **/
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Please try again!", Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == EMAIL_REQUEST) {
            //imagePath = null;
            //sendButton.setEnabled(false);
            uris = new ArrayList<>();

            message.setText("");
            Toast.makeText(this, "Thank you!", Toast.LENGTH_LONG).show();



        }
    }

    /***
     *
     =============================================================================================================
     public Uri getImageUri(Context inContext, Bitmap inImage) {
     ByteArrayOutputStream bytes = new ByteArrayOutputStream();
     inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
     String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
     return Uri.parse(path);
     }

     public String getRealPathFromURI(Uri uri) {
     Cursor cursor = getContentResolver().query(uri, null, null, null, null);
     cursor.moveToFirst();
     int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
     return cursor.getString(idx);
     }
     ==============================================================================================================
     **/

    private File getImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());

        String imageFileName = "CampusReporter_" + timeStamp + "_";

        File imageFile = null;

        //File imageFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), imageFileName + ".jpg");

        if (isExternalStorageWritable()) {

            File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

            imageFile = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    folder);

            imagePath = imageFile.getAbsolutePath();

            Log.d("getImageFile", imageFile.getAbsolutePath());

            return imageFile;

       }

        return imageFile;
    }



    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Log.d("gallery", imagePath);
        File image = new File("file:"+imagePath);
        Uri imageUri = Uri.fromFile(image);
        mediaScanIntent.setData(imageUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private File getAudioFile() throws IOException {

        String audioFileName = "MP3_" + "CampusReporter" + "_";

        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);


        File audioFile = File.createTempFile(
                audioFileName,
                ".3gp",
                folder);

        audioPath = audioFile.getAbsolutePath();

        Log.d("getAudioFile", audioFile.getAbsolutePath());

        return audioFile;
    }

    private void setUpAudio(){
        mediaRecorder= new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);

        File audio = null;

        try{
           audio  = getAudioFile();
        }catch(IOException e){
            e.printStackTrace();
        }

        if(audio != null){
            audioPath = audio.getAbsolutePath();
            mediaRecorder.setOutputFile(audioPath);
        }
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {

        getLocation();

    }

    @Override
    public void onConnectionSuspended(int i) {

        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onResume() {
        super.onResume();

        checkPlayServices();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability mGoogleApiAvailability = GoogleApiAvailability.getInstance();

        int resultCode = mGoogleApiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (mGoogleApiAvailability.isUserResolvableError(resultCode)) {
                mGoogleApiAvailability.getErrorDialog(this, resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }

        return true;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }


    private void getLocation() {

            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (mLocation != null) {
                latitude = mLocation.getLatitude();
                longitude = mLocation.getLongitude();

            }
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }else if(Environment.MEDIA_REMOVED.equals(state)){
            Toast.makeText(getApplicationContext(), "Memory-card missing", Toast.LENGTH_SHORT).show();
            return false;
        }
        return false;
    }

}
