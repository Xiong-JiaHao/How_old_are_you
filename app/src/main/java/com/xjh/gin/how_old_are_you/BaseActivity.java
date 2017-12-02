package com.xjh.gin.how_old_are_you;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gin on 2017/11/23.
 */

public class BaseActivity extends AppCompatActivity {


    protected void isPermissionGranted(String permission, int requestCode) {

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            return ;
        }

        //判断是否需要请求允许权限
        if (hasPermission(new String[]{permission})) {
            requestPermissions(new String[]{permission}, requestCode);
        }
    }

    protected void isPermissionAllGranted(String[] permissions, int requestCode) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            return ;
        }

        //获得批量请求但被禁止的权限列表
        List<String> deniedPerms = new ArrayList<String>();
        for(int i = 0; permissions != null && i < permissions.length;i++){
            if (!hasPermission(new String[]{permissions[i]})) {
                deniedPerms.add(permissions[i]);
            }
        }
        //进行批量请求
        int denyPermNum = deniedPerms.size();
        if(denyPermNum != 0){
            requestPermissions(deniedPerms.toArray(new String[denyPermNum]), requestCode);
        }
    }

    /**
     * 为子类提供一个权限检查方法
     * @param permissions
     * @return
     */
    public boolean hasPermission(String[] permissions) {

        for (String permission: permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 为子类提供一个权限请求方法
     * @param requestCode
     * @param permissions
     */
    public void requestPermission(int requestCode, String[] permissions) {
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }


//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//
//        if(grantResults.length == 0){
//            return;
//        }
//
//        switch (requestCode) {
//            case 1 :
//                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(this, "未授予拨打电话权限", Toast.LENGTH_SHORT).show();
//                }else{
//                    doCallPhone();
//                }
//                break;
//            case 2 :
//                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(this, "未授予读取SD卡权限", Toast.LENGTH_SHORT).show();
//                }else{
//                    Toast.makeText(this, "用户已经授予读取SD卡权限", Toast.LENGTH_SHORT).show();
//                }
//                break;
//            case 3 :
//                for (int i = 0; i < grantResults.length; i++) {
//                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
//                        Toast.makeText(this, "有权限未授予，部分功能不能使用", Toast.LENGTH_SHORT).show();
//                        break;
//                    } else if (i == grantResults.length - 1){
//                        Toast.makeText(this, "用户已经授予全部权限", Toast.LENGTH_SHORT).show();
//                    }
//                }
//                break;
//        }
//
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//    }
//
//    private void doCallPhone() {
//        Intent intent = new Intent(Intent.ACTION_CALL);
//        Uri data = Uri.parse("tel:" + "10086");
//        intent.setData(data);
//        startActivity(intent);
//    }
}