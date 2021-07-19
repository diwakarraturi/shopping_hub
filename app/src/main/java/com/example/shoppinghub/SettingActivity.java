package com.example.shoppinghub;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.example.shoppinghub.prevalent.prevalent;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {

    private CircleImageView profileImageView;
    private EditText fullNameEditText,userPhoneEditText,addreessEditText;
    private TextView profileChangeTextBtn,closeTextBtn,updateTextBtn;
    private Button securityQuestionBtn;
    private Uri imageUri;
    private String myUrl="";
    private StorageReference storagePofilePictureRef;
    private String checker = "";
    private StorageTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        storagePofilePictureRef = FirebaseStorage.getInstance().getReference().child("Profile pictures");

        profileImageView =(CircleImageView) findViewById(R.id.settings_profile_image);
        fullNameEditText = (EditText) findViewById(R.id.settings_full_name);
        userPhoneEditText = (EditText) findViewById(R.id.settings_phone_number);
        addreessEditText = (EditText) findViewById(R.id.settings_address);
        profileChangeTextBtn= (TextView) findViewById(R.id.profile_image_change_btn);
        closeTextBtn= (TextView) findViewById(R.id.close_settings_btn);
        updateTextBtn= (TextView) findViewById(R.id.update_account_settings_btn);
        securityQuestionBtn = findViewById(R.id.security_question_btn);


        userInfoDisplay(profileImageView,fullNameEditText,userPhoneEditText,addreessEditText);


        closeTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        securityQuestionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(SettingActivity.this,ResetPasswordActivity.class);
                intent.putExtra("check","settings");
                startActivity(intent);
            }
        });

        updateTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checker.equals("clicked")){

                    userInfoSaved();

                }
                else{
                    updateOnlyUserInfo();
                }
            }
        });

        profileChangeTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checker = "clicked";
                CropImage.activity(imageUri)
                        .setAspectRatio(1,1)
                        .start(SettingActivity.this);
            }
        });

    }

    private void updateOnlyUserInfo() {
        DatabaseReference ref =FirebaseDatabase.getInstance().getReference().child("users");

        HashMap<String,Object> userMap =new HashMap<>();
        userMap.put("name",fullNameEditText.getText().toString());
        userMap.put("address",addreessEditText.getText().toString());
        userMap.put("phoneOrder",userPhoneEditText.getText().toString());
        ref.child(prevalent.currentOnlineUser.getPhone()).updateChildren(userMap);


        startActivity(new Intent(SettingActivity.this,HomeActivity.class));
        Toast.makeText(SettingActivity.this, "Profile info update successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE  && resultCode==RESULT_OK && data!=null){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            profileImageView.setImageURI(imageUri);
        }
        else{
            Toast.makeText(this, "Error! Try Again", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SettingActivity.this,SettingActivity.class));
            finish();
        }
    }

    private void userInfoSaved() {
        if(TextUtils.isEmpty(fullNameEditText.getText().toString())){
            Toast.makeText(this, "Name is mandatory.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(addreessEditText.getText().toString())){
            Toast.makeText(this, "Address is mandatory..", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(userPhoneEditText.getText().toString())){
            Toast.makeText(this, "Phone Number is mandatory.", Toast.LENGTH_SHORT).show();
        }
        else if(checker.equals("clicked")){
            uploadImage();
        }


    }

    private void uploadImage() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Updating Profile");
        progressDialog.setMessage("Please wait for a while");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        if(imageUri!=null){
            final StorageReference fileRef = storagePofilePictureRef.child(prevalent.currentOnlineUser.getPhone()+".jpg");
            uploadTask = fileRef.putFile(imageUri);

            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {

                    if(!task.isSuccessful()){
                        throw task.getException();
                    }

                    return fileRef.getDownloadUrl();

                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUrl = task.getResult();
                        myUrl=downloadUrl.toString();

                        DatabaseReference ref =FirebaseDatabase.getInstance().getReference().child("users");

                        HashMap<String,Object> userMap =new HashMap<>();
                        userMap.put("name",fullNameEditText.getText().toString());
                        userMap.put("address",addreessEditText.getText().toString());
                        userMap.put("phoneOrder",userPhoneEditText.getText().toString());
                        userMap.put("image",myUrl);
                        ref.child(prevalent.currentOnlineUser.getPhone()).updateChildren(userMap);

                        progressDialog.dismiss();
                        startActivity(new Intent(SettingActivity.this,HomeActivity.class));
                        Toast.makeText(SettingActivity.this, "Profile info update successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    else{
                        progressDialog.dismiss();
                        Toast.makeText(SettingActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else{
            Toast.makeText(this, "image is not selected.", Toast.LENGTH_SHORT).show();
        }
    }

    private void userInfoDisplay(CircleImageView profileImageView, EditText fullNameEditText, EditText userPhoneEditText, EditText addreessEditText) {

        DatabaseReference UsersRef = FirebaseDatabase.getInstance().getReference().child("users").child(prevalent.currentOnlineUser.getPhone());

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.child("image").exists()){
                        String image =snapshot.child("image").getValue().toString();
                        String name =snapshot.child("name").getValue().toString();
                        String phone =snapshot.child("phone").getValue().toString();
                        String address =snapshot.child("address").getValue().toString();

                        Picasso.get().load(image).into(profileImageView);
                        fullNameEditText.setText(name);
                        userPhoneEditText.setText(phone);
                        addreessEditText.setText(address);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}

//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.app.ProgressDialog;
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.view.View;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.example.shoppinghub.prevalent.prevalent;
//import com.google.android.gms.tasks.Continuation;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//import com.google.firebase.storage.StorageReference;
//import com.google.firebase.storage.StorageTask;
//import com.squareup.picasso.Picasso;
//import com.theartofdev.edmodo.cropper.CropImage;
//
//import org.jetbrains.annotations.NotNull;
//
//import java.util.HashMap;
//
//import de.hdodenhof.circleimageview.CircleImageView;
//
//public class SettingActivity extends AppCompatActivity
//{
//    private CircleImageView profileImageView;
//    private EditText fullNameEditText, userPhoneEditText, addressEditText;
//    private TextView profileChangeTextBtn, closeTextBtn, saveTextButton;
//
//    private Uri imageUri;
//    private StorageTask uploadTask;
//    private String myUrl = "";
//    private StorageReference storageProfilePictureRef;
//    private  String checker="";
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState)
//    {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_setting);
//
//        profileImageView = (CircleImageView) findViewById(R.id.settings_profile_image);
//        fullNameEditText = (EditText) findViewById(R.id.settings_full_name);
//        userPhoneEditText = (EditText) findViewById(R.id.settings_phone_number);
//        addressEditText = (EditText) findViewById(R.id.settings_address);
//        profileChangeTextBtn = (TextView) findViewById(R.id.profile_image_change_btn);
//        closeTextBtn = (TextView) findViewById(R.id.close_settings_btn);
//        saveTextButton = (TextView) findViewById(R.id.update_account_settings_btn);
//
//        userInfoDisplay(profileImageView, fullNameEditText, userPhoneEditText, addressEditText);
//
//        closeTextBtn.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                finish();
//
//            }
//        });
//
//        saveTextButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(checker.equals("clicked"))
//                {
//                    userInfoSaved();
//
//                }
//                else
//                {
//                    updateOnlyUserInfo();
//
//                }
//            }
//        });
//
//        profileChangeTextBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                checker = "clicked";
//
//                CropImage.activity(imageUri)
//                        .setAspectRatio(1,1)
//                        .start(SettingActivity.this);
//            }
//        });
//
//    }
//
//    private void updateOnlyUserInfo()
//    {
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
//
//        HashMap<String,Object> userMap = new HashMap<>();
//        userMap.put("name",fullNameEditText.getText().toString());
//        userMap.put("address",addressEditText.getText().toString());
//        userMap.put("phoneOrder",userPhoneEditText.getText().toString());
//        ref.child(prevalent.currentOnlineUser.getPhone()).updateChildren(userMap);
//
//        startActivity(new Intent(SettingActivity.this,MainActivity.class));
//        Toast.makeText(SettingActivity.this, "Profile info update succesfully...", Toast.LENGTH_SHORT).show();
//        finish();
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode,Intent data)
//    {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if ((requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode==RESULT_OK) && data!=null)
//        {
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//            imageUri = result.getUri();
//
//            profileImageView.setImageURI(imageUri);
//
//        }
//        else
//        {
//            Toast.makeText(this, "Error, Try Again...", Toast.LENGTH_SHORT).show();
//
//            startActivity(new Intent(SettingActivity.this,SettingActivity.class));
//            finish();
//        }
//    }
//
//    private void userInfoSaved()
//    {
//        if(TextUtils.isEmpty(fullNameEditText.getText().toString()))
//        {
//            Toast.makeText(this, "Name is Mandatory...", Toast.LENGTH_SHORT).show();
//        }
//        else if(TextUtils.isEmpty(addressEditText.getText().toString()))
//        {
//            Toast.makeText(this, "Name is Mandatory...", Toast.LENGTH_SHORT).show();
//        }
//        else if(TextUtils.isEmpty(userPhoneEditText.getText().toString()))
//        {
//            Toast.makeText(this, "Name is Mandatory...", Toast.LENGTH_SHORT).show();
//        }
//        else if(checker.equals("clicked"))
//        {
//            uploadImage();
//        }
//
//    }
//
//    private void uploadImage()
//    {
//        final ProgressDialog progressDialog = new ProgressDialog(this);
//        progressDialog.setTitle("Update Profile");
//        progressDialog.setMessage("Please wait we are updating your account information");
//        progressDialog.setCanceledOnTouchOutside(false);
//        progressDialog.show();
//
//        if(imageUri!=null)
//        {
//            final StorageReference fileRef = storageProfilePictureRef
//                    .child(prevalent.currentOnlineUser.getPhone() + ".jpg");
//
//            uploadTask = fileRef.putFile(imageUri);
//            uploadTask.continueWithTask(new Continuation()
//            {
//                @Override
//                public Object then(@NonNull @NotNull Task task) throws Exception
//                {
//                    if(!task.isSuccessful())
//                    {
//                        throw task.getException();
//                    }
//
//                    return fileRef.getDownloadUrl();
//                }
//            })
//                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
//                @Override
//                public void onComplete(@NonNull @NotNull Task<Uri> task)
//                {
//                   if (task.isSuccessful())
//                   {
//                       Uri downloadurl = task.getResult();
//                       myUrl = downloadurl.toString();
//
//                       DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
//
//                       HashMap<String,Object> userMap = new HashMap<>();
//                       userMap.put("name",fullNameEditText.getText().toString());
//                       userMap.put("address",addressEditText.getText().toString());
//                       userMap.put("phoneOrder",userPhoneEditText.getText().toString());
//                       userMap.put("image",myUrl);
//                       ref.child(prevalent.currentOnlineUser.getPhone()).updateChildren(userMap);
//
//                       progressDialog.dismiss();
//
//                       startActivity(new Intent(SettingActivity.this,MainActivity.class));
//                       Toast.makeText(SettingActivity.this, "Profile info update succesfully...", Toast.LENGTH_SHORT).show();
//                       finish();
//                   }
//                   else
//                   {
//                       progressDialog.dismiss();
//                       Toast.makeText(SettingActivity.this, "Error..", Toast.LENGTH_SHORT).show();
//                   }
//
//                }
//            });
//
//        }
//        else
//        {
//            Toast.makeText(this, "Image is not selected", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void userInfoDisplay(CircleImageView profileImageView, EditText fullNameEditText, EditText userPhoneEditText, EditText addressEditText)
//    {
//        DatabaseReference UsersRef = FirebaseDatabase.getInstance().getReference().child("users").child(prevalent.currentOnlineUser.getPhone());
//        UsersRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot)
//            {
//
//                if(dataSnapshot.exists())
//                {
//                    if(dataSnapshot.child("image").exists())
//                    {
//                        String image = dataSnapshot.child("image").getValue().toString();
//                        String name = dataSnapshot.child("name").getValue().toString();
//                        String phone = dataSnapshot.child("phone").getValue().toString();
//                        String address = dataSnapshot.child("address").getValue().toString();
//
//                        Picasso.get().load(image).into(profileImageView);
//                        fullNameEditText.setText(name);
//                        userPhoneEditText.setText(phone);
//                        addressEditText.setText(address);
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError)
//            {
//
//            }
//        });
//    }
//}