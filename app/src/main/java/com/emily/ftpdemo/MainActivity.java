package com.emily.ftpdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbSession;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String ip = "192.168.0.119";
    private static final int port = 21;
    private static final String userName = "192.168.0.119";
    private static final String password = "123456";
    private static final String ftpDir = "apk";
    private FtpUtils ftpUtils;
    private FTPClient ftpClient;
    private double vision;
    private String savedPath;
    private boolean flag = false;
    private  String filename;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        vision = VersionUtils.getVersion(this);

        savedPath = getFilesDir().getPath()+File.separator;
                //context.getExternalCacheDir().getPath()+ File.separator+"app"+File.separator;
                //getFilesDir().getPath()+File.separator;
        Log.d(TAG, "savedPath:" + savedPath);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();//增加部分
                ftpUtils = new FtpUtils();
                ftpClient = ftpUtils.getFTPClient(ip, port,userName,password);

                if (ftpClient != null) {
                    flag = ftpUtils.isVerion(ftpClient, ftpDir, vision);
                    if (flag){
                        filename = ftpUtils.downLoadFTP(ftpClient, ftpDir, savedPath);
                        ftpUtils.closeFTP(ftpClient);
                            //弹框显示更新
                            AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).setTitle
                                    ("Tips").setMessage("Have new version,please update!")
                                    .setNeutralButton("Cancel", new DialogInterface
                                            .OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).setNegativeButton("Update", new DialogInterface
                                            .OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //安装apk
                                            installApk(context,savedPath);
                                            //删除安装包

                                        }
                                    }).show();
                            dialog.setCanceledOnTouchOutside(false);//可选
                            dialog.setCancelable(false);//可选

                    }else{
                        Log.d(TAG, "当前已是最新版本");
                    }
                }
                Looper.loop();//增加部分
            }
        }).start();

    }


    /**
     * 安装apk
     */
    public void installApk(Context context,String apkPath) {
        if (TextUtils.isEmpty(apkPath)){
            Toast.makeText(context,"更新失败！未找到安装包", Toast.LENGTH_SHORT).show();
            return;
        }

        File apkFile = new File(apkPath + filename);

        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //Android 7.0 系统共享文件需要通过 FileProvider 添加临时权限，否则系统会抛出 FileUriExposedException .
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context,"com.emily.ftpdemo.fileprovider",apkFile);
            intent.setDataAndType(contentUri,"application/vnd.android.package-archive");
        }else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(
                    Uri.fromFile(apkFile),
                    "application/vnd.android.package-archive");
        }
        context.startActivity(intent);
    }
}
