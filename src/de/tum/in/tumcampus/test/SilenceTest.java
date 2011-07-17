package de.tum.in.tumcampus.test;

import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.test.ServiceTestCase;
import de.tum.in.tumcampus.Const;
import de.tum.in.tumcampus.models.LectureItem;
import de.tum.in.tumcampus.models.LectureItemManager;
import de.tum.in.tumcampus.services.SilenceService;

public class SilenceTest extends ServiceTestCase<SilenceService> {

	AudioManager am;

	public SilenceTest() {
		super(SilenceService.class);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();

		am = (AudioManager) getContext()
				.getSystemService(Context.AUDIO_SERVICE);
		am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
	}

	public void testSilence() throws Exception {
		Intent service = new Intent(getContext(), SilenceService.class);
		SilenceService.interval = 1000;

		_addLecture();

		getContext().startService(service);
		Thread.sleep(1000);
		assertEquals(am.getRingerMode(), AudioManager.RINGER_MODE_SILENT);

		_removeLecture();

		Thread.sleep(2000);
		assertEquals(am.getRingerMode(), AudioManager.RINGER_MODE_NORMAL);
	}

	private void _addLecture() throws Exception {
		// inject test data
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR, 1);

		LectureItem li = new LectureItem("S1", "S1", new Date(),
				calendar.getTime(), "V1", "", "", "", "", "S1");

		LectureItemManager lim = new LectureItemManager(getContext(), Const.db);
		lim.replaceIntoDb(li);
		lim.close();
	}

	private void _removeLecture() {
		// remove test data
		LectureItemManager lim = new LectureItemManager(getContext(), Const.db);
		lim.deleteLectureFromDb("S1");
		lim.close();
	}
}