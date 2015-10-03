package com.ken.nethelper.util;

import java.io.BufferedOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import android.util.Log;

import com.ken.nethelper.constant.C;

public class RouterManager {
	
	public static final String TAG = "RouterManager";
	
	public static boolean setPWD(String pwd)
	{

		// TODO 自动生成的方法存根
		try {

			SmbFile smbFile = new SmbFile(
					"smb://"+C.OPENWRT.USER_NAME+":"+C.OPENWRT.USER_PWD+"@"+C.OPENWRT.OPENWRT_ADD);
			
			int length = smbFile.getContentLength();
			String fileNetwork = null;
			String oldPassword = null;
			String newPassword = pwd;
			
			byte buffer[] = new byte[length];
			SmbFileInputStream in = new SmbFileInputStream(smbFile); 
			
			while ((in.read(buffer)) != -1)fileNetwork = new String(buffer);
			
			Pattern pattern = Pattern.compile("option password '(.*?)'");
			Matcher matcher = pattern.matcher(fileNetwork);
			
			//我们假定最后一个密码是l2tp的密码
			while(matcher.find())
			{
				oldPassword = matcher.group(1);
			}
			
			in.close();
			fileNetwork = fileNetwork.replaceAll(oldPassword , newPassword);
			
			BufferedOutputStream out = new BufferedOutputStream(new SmbFileOutputStream(smbFile));
			out.write(fileNetwork.getBytes());
			out.close();
			
			Log.i("sms" , "oldpwd: "+oldPassword);
			Log.i("sms" , "new file: "+fileNetwork);
			
			
		} catch (Exception e) {

			e.printStackTrace();
			return false;

		}
			return true;

	
	}
}
