package de.tum.in.tumcampus.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import de.tum.in.tumcampus.Const;
import de.tum.in.tumcampus.Const.Settings;
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

	/**
	 * default init (run intent in new thread)
	 */
	public SilenceService() {
		super("SilenceService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		// loop until silence mode gets disabled in settings
		while (Utils.getSettingBool(this, Const.Settings.silence)) {

			AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

			LectureItemManager lim = new LectureItemManager(this, Const.db);
			if (!lim.hasLectures()) {
				// no lectures available
				return;
			}
			Cursor c = lim.getCurrentFromDb();
			if (c.getCount() != 0) {
				// if current lecture(s) found, silence the mobile
				Utils.setSettingBool(this, Settings.silence_on, true);

				Utils.log("set ringer mode: silent");
				am.setRingerMode(AudioManager.RINGER_MODE_SILENT);

			} else if (Utils.getSettingBool(this, Settings.silence_on)) {
				// default: no silence
				Utils.log("set ringer mode: normal");
				am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			}
			c.close();
			lim.close();

			// wait unteil next check
			synchronized (this) {
				try {
					wait(interval);
				} catch (Exception e) {
					Utils.log(e, "");
				}
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Utils.log(""); // log destroy
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Utils.log(""); // log create
	}
}