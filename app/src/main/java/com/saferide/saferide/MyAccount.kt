package com.saferide.saferide

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import de.hdodenhof.circleimageview.CircleImageView
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_my_account.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*

class MyAccount : AppCompatActivity() {

    //Create a DB Reference
    private lateinit var mUserDatabase: DatabaseReference
    //Create a Firebase User reference
    private lateinit var mCurrentUser: FirebaseUser

    //Android Layout
    private lateinit var mDisplayImage: CircleImageView
    private lateinit var mName: TextView
    private lateinit var mStatus: TextView
    private lateinit var mStatusBtn: Button
    private lateinit var mImageBtn: Button
    private lateinit var myAccProgress: ProgressBar

    private val GALLERY_PICK = 1

    // Storage Firebase
    private lateinit var mImageStorage: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_account)
        setSupportActionBar(toolbar)

        mDisplayImage = findViewById(R.id.settings_image) as CircleImageView
        mName = findViewById(R.id.settings_name) as TextView
        mStatus = findViewById(R.id.settings_status) as TextView

        mStatusBtn = findViewById(R.id.settings_status_btn) as Button
        mImageBtn = findViewById(R.id.settings_image_btn) as Button

        myAccProgress = findViewById(R.id.accProgressBar)

        // Instantiate FirebaseStorage Object
        mImageStorage = FirebaseStorage.getInstance().reference

        // Get the current firebase user
        mCurrentUser = FirebaseAuth.getInstance().currentUser!!

        // Get the current firebase user ID
        val current_uid = mCurrentUser.uid

        //Refer to the user's data records
        mUserDatabase = FirebaseDatabase.getInstance().reference.child("Users").child(current_uid)

        //To achieve offline data persistency
        mUserDatabase.keepSynced(true)

        //Add a listener to the database reference
        mUserDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                //Get user details from the db reference
                val name = dataSnapshot.child("name").value!!.toString()
                //image url
                val image = dataSnapshot.child("image").value!!.toString()
                val status = dataSnapshot.child("status").value!!.toString()
                val thumb_image = dataSnapshot.child("thumb_image").value!!.toString()

                mName.text = name
                mStatus.text = status

                //Get image from storage, If it is not there it will place a placeholder
                Picasso.get()
                        .load(image)
                        .into(mDisplayImage)

                /*Above code should be replaced with below commented code*/
                /*But below will always get avatar image*/
                /*
                * Picasso.get()
                        .load(image)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .into(mDisplayImage)*/

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })


        mStatusBtn.setOnClickListener {
            val status_value = mStatus.text.toString()

            val status_intent = Intent(this@MyAccount, StatusActivity::class.java)
            //Add extra values with intent which can use later
            status_intent.putExtra("status_value", status_value)
            startActivity(status_intent)
        }


        mImageBtn.setOnClickListener {
            val galleryIntent = Intent()
            galleryIntent.type = "image/*"
            galleryIntent.action = Intent.ACTION_GET_CONTENT

            startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK)
            // start picker to get image for cropping and then use the image in cropping activity
            /*CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this);*/
        }
    }

    lateinit var imageUri: Any
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //This will call after getting image
        resultCode?.let { super.onActivityResult(requestCode, it, data) }

        if (requestCode == GALLERY_PICK && resultCode == Activity.RESULT_OK) {

            imageUri = data?.data!!

            Log.d("tt","onActivityResult invokeddd..")


            if(CropImage.isExplicitCameraPermissionRequired(this@MyAccount!!)) {
                ActivityCompat.requestPermissions(this@MyAccount!!, arrayOf(android.Manifest.permission.CAMERA), CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE)
            }
            else {
                startActivityForResult(CropImage.getPickImageChooserIntent(this@MyAccount!!), CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE)
            }


            //Below lines gives an error
            /*CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(500, 500)
                    .start(this)*/

            //Toast.makeText(SettingsActivity.this, imageUri, Toast.LENGTH_LONG).show();

        }


       /* if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {*/

            //val result = CropImage.getActivityResult(data)

            //If result isn't having an error
            /*if (resultCode == Activity.RESULT_OK) {*/

                // Show Progress
                myAccProgress.visibility = View.VISIBLE

                //Get the uri of the result
                val resultUri = imageUri
                //Get the file path from the uri
                //val thumb_filePath = File(resultUri)

                //Get the current use id
                val current_user_id = mCurrentUser.uid

                var thumb_bitmap: Bitmap? = null

                //Compress image
                /*try {
                    thumb_bitmap = Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath)
                } catch (e: IOException) {
                    e.printStackTrace()
                }*/

                //To store a bitmap in firebase we need a bytearrayoutput stream
                val baos = ByteArrayOutputStream()
                //thumb_bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                //val thumb_byte = baos.toByteArray()

                //Create a reference to our imagestorage
                val filepath = mImageStorage.child("profile_images").child("$current_user_id.jpg")
                //Create a reference to our thumbnail image
                //val thumb_filepath = mImageStorage.child("profile_images").child("thumbs").child("$current_user_id.jpg")


                //Storing the image
                filepath.putFile(imageUri as Uri).addOnCompleteListener { task ->
                    //If uploaded successfully
                    if (task.isSuccessful) {




                        /*Below lines set used only for fix bugs which get after using kotlin.
                        * So should remove after fixes*/
                        val download_url = task.result.downloadUrl!!.toString()
                        val update_hashMap = HashMap<String,String>()
                        update_hashMap.put("image", download_url)
                        mUserDatabase.updateChildren(update_hashMap as Map<String, Any>?).addOnCompleteListener {
                            myAccProgress.visibility = View.INVISIBLE
                        }

                        //Store thumbnail
                        /*val uploadTask = thumb_filepath.putBytes(thumb_byte)
                        uploadTask.addOnCompleteListener { thumb_task ->
                            val thumb_downloadUrl = thumb_task.result.downloadUrl!!.toString()

                            //Thumbanail adding task
                            if (thumb_task.isSuccessful) {

                                val update_hashMap = HashMap<String,String>()
                                update_hashMap.put("image", download_url)
                                update_hashMap.put("thumb_image", thumb_downloadUrl)

                                mUserDatabase.updateChildren(update_hashMap as Map<String, Any>?).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        myAccProgress.visibility = View.INVISIBLE
                                        Toast.makeText(this@MyAccount, "Success Uploading.", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(this@MyAccount, "Error in uploading.", Toast.LENGTH_LONG).show()
                                    }
                                }

                            } else {
                                Toast.makeText(this@MyAccount, "Error in uploading thumbnail.", Toast.LENGTH_LONG).show()
                                myAccProgress.visibility = View.INVISIBLE
                            }
                        }*/

                    } else {
                        Toast.makeText(this@MyAccount, "Error in uploading.", Toast.LENGTH_LONG).show()
                        myAccProgress.visibility = View.INVISIBLE
                    }
                }
            /*}
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                val error = result.getError()

            }*/
        /*}*/
    }
}
