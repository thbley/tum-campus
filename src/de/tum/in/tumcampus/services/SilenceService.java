package de.tum.in.tumcampus.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import de.tum.in.tumcampus.Const;
import de.tum.in.tumcampus.models.LectureItemManager;
import de.tum.in.tumcampus.models.Utils;

public class SilenceService extends IntentService {

	public SilenceService() {
		super("SilenceService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		while (Utils.getSettingBool(this, Const.settings.silence)) {

			int mode = AudioManager.RINGER_MODE_NORMAL;

			LectureItemManager lim = new LectureItemManager(this, Const.db);
			Cursor c = lim.getCurrentFromDb();
			if (c.getCount() != 0) {
				mode = AudioManager.RINGER_MODE_SILENT;
			}
			c.close();
			lim.close();

			Utils.Log("set ringer mode: " + mode);
			AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			am.setRingerMode(mode);

			synchronized (this) {
				try {
					wait(60000);
				} catch (Exception e) {
					Utils.Log(e, "");
				}
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Utils.Log("");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Utils.Log("");
	}
}