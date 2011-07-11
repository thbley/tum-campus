package de.tum.in.tumcampus.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.util.Log;
import de.tum.in.tumcampus.models.LectureItemManager;
import de.tum.in.tumcampus.models.Utils;

public class SilenceService extends IntentService {

	public SilenceService() {
		super("SilenceService");
	}

	final static String db = "database.db";

	@Override
	protected void onHandleIntent(Intent intent) {

		while (Utils.getSettingBool(this, "silence")) {
			
			int mode = AudioManager.RINGER_MODE_NORMAL;

			LectureItemManager lim = new LectureItemManager(this, db);
			Cursor c = lim.getCurrentFromDb();
			if (c.getCount() != 0) {
				mode = AudioManager.RINGER_MODE_SILENT;
			}
			c.close();
			lim.close();

			AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			am.setRingerMode(mode);

			Log.d("TumCampus SilenceService", "set ringer mode: " + mode);

			synchronized (this) {
				try {
					wait(60000);
				} catch (Exception e) {
					Log.e("TumCampus SilenceService", e.getMessage());
				}
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.d("TumCampus SilenceService", "TumCampus service destroy");
	}

	@Override
	public void onCreate() {
		super.onCreate();

		Log.d("TumCampus SilenceService", "TumCampus service create");
	}
}