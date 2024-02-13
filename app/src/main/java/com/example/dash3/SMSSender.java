package com.example.dash3;

import android.telephony.SmsManager;
import android.util.Log;

public class SMSSender {

    private static final String TAG = "SMSSender";

    static String josh = "7058229216"; // Replace with the recipient's phone number
    static String ethan = "5195320619"; // Replace with the recipient's phone number
    static String chooch = "5196970400"; // Replace with the recipient's phone number
    static String message = "Emergency"; // Replace with your message

    public static void sendSMS() {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(josh, null, message, null, null);
            Log.d(TAG, "SMS sent successfully to " + josh);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to send SMS to " + josh);
        }
        //try {                                                                 //uncomment this if you want to send to two numbers
          //  SmsManager smsManager = SmsManager.getDefault();
            //smsManager.sendTextMessage(ethan, null, message, null, null);
            //Log.d(TAG, "SMS sent successfully to " + ethan);
        //} catch (Exception e) {
          //  e.printStackTrace();
            //Log.e(TAG, "Failed to send SMS to " + ethan);
        //}
        //try {                                                                 //uncomment this if you want to send to two numbers
        //  SmsManager smsManager = SmsManager.getDefault();
        //smsManager.sendTextMessage(chooch, null, message, null, null);
        //Log.d(TAG, "SMS sent successfully to " + chooch);
        //} catch (Exception e) {
        //  e.printStackTrace();
        //Log.e(TAG, "Failed to send SMS to " + chooch);
        //}
    }
}
