package com.ken.nethelper.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import com.ken.nethelper.bean.SMS;

public class SMSManager {

	public static final String ALARM_ACTION = "ALARM_ACTION";

	/**
	 * 设置发送短信任务
	 * 
	 * @param time
	 * @param isEveryday
	 */
	public void setTask(int hour , int min , boolean isEveryday, Context context) {
		Intent intent = new Intent(ALARM_ACTION);
		
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				context.getApplicationContext(), 0, intent, 0);
		Calendar calendar = Calendar.getInstance();
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, min);
		
		alarmManager.cancel(pendingIntent);
		
		if (isEveryday) {
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
					calendar.getTimeInMillis(), 12 * 60 * 60 * 1000,
					pendingIntent);
		} else {
			alarmManager.set(AlarmManager.RTC_WAKEUP,
					calendar.getTimeInMillis(), pendingIntent);
		}
	}

	/**
	 * 发送短信
	 * 
	 * @param sms
	 */
	public void sendMsg(SMS sms) {

		SmsManager smsManager = SmsManager.getDefault();
		if (sms.getContent().length() > 50) {
			List<String> contents = smsManager.divideMessage(sms.getContent());
			for (String smsText : contents) {
				smsManager.sendTextMessage(sms.getTarget(), null, smsText,
						null, null);
			}
		} else {
			smsManager.sendTextMessage(sms.getTarget(), null, sms.getContent(),
					null, null);
		}

		Log.i("sms send", sms.getTarget() + ":" + sms.getContent());
	}

	/**
	 * 处理短信
	 * 
	 * @param content
	 * @return
	 */
	public List<SMS> digestMsg(Bundle content) {
		// 可能会有多条
		Object[] pdusObj = (Object[]) content.get("pdus");
		SmsMessage[] messages = new SmsMessage[pdusObj.length];
		List<SMS> smsList = new ArrayList<SMS>();

		for (int i = 0; i < pdusObj.length; i++) {

			SMS sms = new SMS();
			messages[i] = SmsMessage.createFromPdu((byte[]) pdusObj[i]);

			Date date = new Date(messages[i].getTimestampMillis());// 时间
			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss", Locale.CHINA);
			String receiveTime = format.format(date);
			sms.setTarget(messages[i].getDisplayOriginatingAddress());
			sms.setTime(receiveTime);
			sms.setContent(messages[i].getMessageBody());

			smsList.add(sms);
		}

		return smsList;
	}
}
