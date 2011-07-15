package de.tum.in.tumcampus.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import de.tum.in.tumcampus.Const;
import de.tum.in.tumcampus.models.LectureItemManager;
import de.tum.in.tumcampus.models.Utils;

/**
 * Service used to silence the mobile during lectures
 */
public class SilenceService extends IntentService {

	/**
	 * interval in milli seconds to check for current lectures 
	 */
	public static int interval = 60000;
	
	public SilenceService() {
		super("SilenceService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		// loop until silence mode gets disabled in settings
		while (Utils.getSettingBool(this, Const.settings.silence)) {

			// default: no silence
			int mode = AudioManager.RINGER_MODE_NORMAL;

			LectureItemManager lim = new LectureItemManager(this, Const.db);
			Cursor c = lim.getCurrentFromDb();
			if (c.getCount() != 0) {
				// if current lecture(s) found, silence the mobile 
				mode = AudioManager.RINGER_MODE_SILENT;
			}
			c.close();
			lim.close();

			Utils.Log("set ringer mode: " + mode);
			// execute (no-)silence mode
			AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			am.setRingerMode(mode);

			// wait unteil next check
			synchronized (this) {
				try {
					wait(interval);
				} catch (Exception e) {
					Utils.Log(e, "");
				}
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Utils.Log(""); // log destroy
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Utils.Log(""); // log create
	}
}