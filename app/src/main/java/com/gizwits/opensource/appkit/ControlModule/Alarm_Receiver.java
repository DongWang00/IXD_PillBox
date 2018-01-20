package com.gizwits.opensource.appkit.ControlModule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Alarm_Receiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service_intent = new Intent(context, RingtonePlayingService.class);
        service_intent.putExtra("extra", intent.getExtras().getString("extra"));
        context.startService(service_intent);
    }
}
