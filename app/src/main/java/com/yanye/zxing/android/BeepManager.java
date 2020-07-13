package com.yanye.zxing.android;

import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.IOException;

public final class BeepManager implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

	private static final float BEEP_VOLUME = 0.40f;
	private MediaPlayer mediaPlayer;
	private static BeepManager instance;
	private boolean hasResource;

	public static BeepManager getInstance() {
		if(instance == null) {
			instance = new BeepManager();
		}
		return instance;
	}

	private BeepManager() {
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		// 监听是否播放完成
		mediaPlayer.setOnCompletionListener(this);
		mediaPlayer.setOnErrorListener(this);
	}

	public void setDataSource(AssetFileDescriptor descriptor) {
		try {
			mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
			mediaPlayer.prepare();
			hasResource = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean hasResource() {
		return this.hasResource;
	}

	public void playBeepSound() {
		mediaPlayer.start();
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		mp.seekTo(0);
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		mp.release();
		return true;
	}
}
