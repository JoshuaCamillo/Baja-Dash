package com.example.dash3;

import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;

public class SMSSender {                //changed to loop through twice with a 30 second break between sending

    private static final String TAG = "SMSSender";

    static String josh = "7058229216"; // Replace with the recipient's phone number
    static String ethan = "5195320619"; // Replace with the recipient's phone number
    static String chooch = "5196970400"; // Replace with the recipient's phone number
    static String message = "Cars Broke!! Cars Broke!!"; // Replace with your message
    static String[] recipients = {josh, ethan};//, chooch};
    static boolean sending = false;
    static int count = 0;

    public static void sendSMS() {
        if (!sending) {
            sending = true;
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(recipients[count], null, message, null, null);
                Log.d(TAG, "SMS sent successfully to " + recipients[count]);
                count++;
                if (count >= recipients.length) {
                    count = 0;
                    sending = false;
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to send SMS");
                sending = false;
                return;
            }

            /*
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    sending = false;
                    sendSMS(); // Send SMS again after 15 seconds
                }
            }, 30000); // 15 seconds delay

             */
        }
    }
}
