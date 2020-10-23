package com.emily.ftpdemo;

import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FtpUtils {
    private static final String TAG = FtpUtils.class.getSimpleName();
    private FTPClient ftpClient;

    public FTPClient getFTPClient(String ftpHost, int ftpPort,String ftpUserName,String ftpPassword) {
        if (ftpClient == null) {
            ftpClient = new FTPClient();
        }
        if (ftpClient.isConnected()) {
            return ftpClient;
        }
        Log.d(TAG, "ftpHost:" + ftpHost + ",ftpPort:" + ftpPort);

        try {
            // connect to the ftp server
            // set timeout
            ftpClient.setConnectTimeout(60000);
            // 设置中文编码集，防止中文乱码
            ftpClient.setControlEncoding("UTF-8");

            ftpClient.connect(ftpHost, ftpPort);
            ftpClient.login(ftpUserName, ftpPassword);
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                Log.d(TAG, "无法连接到ftp服务器，错误码为：" + replyCode);
                return ftpClient;
            }
            Log.d(TAG, "connect success: replyCode" + replyCode);

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            return null;
        }

//        Log.d(TAG, "ftpUserName:" + ftpUserName + ",ftpPassword:" + ftpPassword);
//        // login on the ftp server
//        try {
//
//            ftpClient.login(ftpUserName, ftpPassword);
//            int replyCode = ftpClient.getReplyCode();
//            if (!FTPReply.isPositiveCompletion(replyCode)) {
//                ftpClient.disconnect();
//                Log.d(TAG, "login fail: replyCode:" + replyCode);
//                return ftpClient;
//            }
//
//            Log.d(TAG, "login success: replyCode:" + replyCode);
//        } catch (IOException e) {
//            e.printStackTrace();
//            Log.e(TAG, e.getMessage());
//            return null;
//        }

        return ftpClient;
    }


    /**
     * 关闭FTP方法
     *
     * @param ftp
     * @return
     */
    public boolean closeFTP(FTPClient ftp) {

        try {
            ftp.logout();
        } catch (Exception e) {
            Log.e(TAG, "FTP关闭失败");
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                    Log.e(TAG, "FTP关闭失败");
                }
            }
        }
        return false;
    }

    /**
     * 判断版本大小
     */
    public boolean isVerion(FTPClient ftp, String filePath, double vision){
        FTPFile[] files;
        // 跳转到文件目录
        try {
//            ftp.changeWorkingDirectory(filePath);
            // 获取目录下文件集合
            ftp.enterLocalPassiveMode();
            files = ftp.listFiles();
        } catch (IOException e) {
            Log.e(TAG, "downLoadFTP: " + e);
            e.printStackTrace();
            return false;
        }

        for (FTPFile file : files) {
            Log.d(TAG, "fileName:" + file.getName());
            int result1 = file.getName().indexOf(".apk");
            if(result1 != -1){
                String[] str = file.getName().split("t");
                String version = str[1].substring(6, 7);
                double newversioncode = Double
                        .parseDouble(version);
                if (vision < newversioncode) {
                    return true;
                }
            }
            // 取得指定文件并下载
//            if (file.getName().equals(fileName)) {

        }
        return false;
    }

    /**
     * 下载FTP下指定文件
     *
     * @param ftp      FTPClient对象
     * @param filePath FTP文件路径
     * @param downPath 下载保存的目录
     * @return true:success, false:fail
     */
    public String  downLoadFTP(FTPClient ftp, String filePath,String downPath)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      {

        // 默认失败
        boolean flag = false;
        String filename = null;
        FTPFile[] files;
        // 跳转到文件目录
        try {
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.setFileTransferMode(FTP.BINARY_FILE_TYPE);
//            ftp.changeWorkingDirectory(filePath);
            // 获取目录下文件集合
            ftp.enterLocalPassiveMode();
            files = ftp.listFiles();
            for (FTPFile file : files) {
                int result1 = file.getName().indexOf(".apk");
                if(result1 != -1) {
                    filename = file.getName();
                    Log.d(TAG, "fileName:" + file.getName());
                    // 取得指定文件并下载
//            if (file.getName().equals(fileName)) {
                    File downFile = new File(downPath + File.separator
                            + file.getName());
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(downFile);
                        // 绑定输出流下载文件,需要设置编码集，不然可能出现文件为空的情况
                        flag = ftp.retrieveFile(new String(file.getName().getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1), fos);
                        // 下载成功删除文件,看项目需求
                        // ftpClient.deleteFile(new String(file.getName().getBytes("UTF-8"),"ISO-8859-1"));
                        fos.flush();
                        if (flag) {
                            Log.d(TAG, "Params downloaded successful.");
                        } else {
                            Log.e(TAG, "Params downloaded failed.");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (fos != null) {
                            try {
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

//            }
            }
        } catch (IOException e) {
            Log.e(TAG, "downLoadFTP: " + e);
            e.printStackTrace();
        }
        return filename;
    }
}
