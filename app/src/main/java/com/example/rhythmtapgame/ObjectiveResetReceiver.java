package com.example.rhythmtapgame;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ObjectiveResetReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ObjectiveManager objectiveManager = new ObjectiveManager(context);
        objectiveManager.resetDailyObjectives();
    }
}
