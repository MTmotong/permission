package com.example.dzy.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;


import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.View;
import android.hardware.Camera;
import android.widget.Button;
import android.widget.Toast;


import java.io.File;
import java.util.List;

import static android.content.Intent.ACTION_CALL;


public class MainActivity extends Activity implements View.OnClickListener{

    private Context mContext;

    private static final int CODE_CAMERA_REQUEST = 0xa1;
    private Uri imageUri;
    private File fileUri = new File(Environment.getExternalStorageDirectory().getPath() + "/photo.jpg");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.and_permission).setOnClickListener(this);
        findViewById(R.id.skip).setOnClickListener(this);
        findViewById(R.id.get_camera5).setOnClickListener(this);
        findViewById(R.id.get_camera6).setOnClickListener(this);
        findViewById(R.id.get_camera7).setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.get_camera5:{
                PackageManager pm = getPackageManager();
                Log.e("String", "Mani " + Manifest.permission.CAMERA);
                Log.e("String", "android " + "android.permission.Camera");

//                if (pm.checkPermission(Manifest.permission.CAMERA, "com.example.dzy.permission")
                if (pm.checkPermission("android.permission.CAMERA", "com.example.dzy.permission")
                        == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(MainActivity.this, "注册过权限！", Toast.LENGTH_SHORT).show();
                    if (IsCameraCanUse()) {
                        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, 0);
                    } else {
                        Toast.makeText(MainActivity.this, "没有相机权限，请到应用程序权限管理开启权限",
                                Toast.LENGTH_SHORT).show();
                        //跳转至app设置
                        getAppDetailSettingIntent();
                    }
                }
            }
            break;
            case R.id.get_camera6:{
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    //无权限，如果用户拒绝过权限，需要给用户解释为什么需要权限
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CAMERA)) {
                        Toast.makeText(MainActivity.this, "需要相机权限才能继续", Toast.LENGTH_SHORT).show();
//                        getAppDetailSettingIntent();
                    }
                    //请求权限处理
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
                    //activty
//                        ActivityCompat.requestPermissions(conent, perms, requestCode);
                    //fragment
//                        fragment.requestPermissions(permission, requestCode);
                } else {
                    //有权限，开启系统摄像头
                    Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 0);


                }
            }
            break;
            case R.id.get_camera7:{
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CAMERA)) {
                        Toast.makeText(MainActivity.this, "需要相机权限才能继续", Toast.LENGTH_SHORT).show();
                    }
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
                } else {//有权限直接调用系统相机拍照
                    imageUri = Uri.fromFile(fileUri);
                    //通过FileProvider创建一个content类型的Uri
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        imageUri = FileProvider.getUriForFile(MainActivity.this, "com.example.dzy.permission", fileUri);
                    }
                    takePicture(MainActivity.this, imageUri, CODE_CAMERA_REQUEST);
                }

            }
            break;
        }
    }


    public boolean IsCameraCanUse() {

        boolean canUse = true;
        Camera mCamera = null;
        try {
            mCamera = Camera.open();
            Camera.Parameters mPara = mCamera.getParameters();
            mCamera.setParameters(mPara);
        } catch (Exception e) {
            canUse = false;
        }
        if (mCamera != null) {
            try {
                mCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
                return canUse;
            }
        }
        return canUse;
    }


    private void getAppDetailSettingIntent() {
        Intent localIntent = new Intent();
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", getApplicationContext().getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", getApplicationContext().getPackageName());
        }
        startActivity(localIntent);
    }

    //用户响应权限请求的回调
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                    //如果授权被取消，结果数组是空的，这里是empty，而不是null
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        imageUri = Uri.fromFile(fileUri);
                        //通过FileProvider创建一个content类型的Uri
                        imageUri = FileProvider.getUriForFile(MainActivity.this, "com.example.dzy.permission", fileUri);
                        takePicture(this, imageUri, CODE_CAMERA_REQUEST);

                    } else {
                        Toast.makeText(MainActivity.this, "用户拒绝授权！", Toast.LENGTH_SHORT).show();
                    }

                return;
            }
        }
    }


    public void takePicture(Activity activity, Uri imageUri, int requestCode ) {
        //调用系统相机
        Intent intentCamera = new Intent();
        //添加这一句表示对目标应用临时授权该Uri所代表的文件
        intentCamera.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intentCamera.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        //将拍照结果保存至photo_file的Uri中，不保留在相册中
        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        activity.startActivityForResult(intentCamera, requestCode);
    }




}
