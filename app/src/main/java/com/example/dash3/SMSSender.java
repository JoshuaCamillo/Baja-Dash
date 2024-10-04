package com.example.dash3;

import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;

public class SMSSender {

    private static final String TAG = "SMSSender";

    static String josh = "7058229216"; // Replace with the recipient's phone number
    static String ethan = "5195320619"; // Replace with the recipient's phone number
    static String chooch2= "5593731966";
    static String jaxon = "2266221387";
    static String jaxon2 = "3107572551";
    static String garrett = "2269884246";
    static String justin = "5598810007";
    static String message = "Core 1 Panic'ed!! Core1 Panic'd!!"; // Replace with your message
    static String[] recipients = {};//josh, ethan, chooch2, jaxon,jaxon2, garrett, justin};
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


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    sending = false;
                    sendSMS(); // Send SMS again after 15 seconds
                }
            }, 30000); // 15 seconds delay


        }
    }
}
