package de.tum.in.tumcampus.receivers;

import de.tum.in.tumcampus.services.SilenceService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompleted extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {

			Intent i = new Intent(context, SilenceService.class);
			context.startService(i);
		}
	}
}