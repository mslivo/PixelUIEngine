package net.mslivo.core.engine.tools.sound;

import net.mslivo.core.engine.media_manager.MediaManager;
import net.mslivo.core.engine.media_manager.CMediaSoundEffect;
import net.mslivo.core.engine.tools.Tools;

import java.util.HashSet;

/*
 * Plays sounds and automatically adjusts volume to distance
 */
public class SoundPlayer {
    private int range;
    private float volume;
    private float camera_x, camera_y;
    private final MediaManager mediaManager;
    HashSet<CMediaSoundEffect> playedSounds;

    public SoundPlayer(MediaManager mediaManager) {
        this(mediaManager, 0);
    }

    public SoundPlayer(MediaManager mediaManager, int range2D) {
        this.mediaManager = mediaManager;
        this.volume = 1f;
        this.playedSounds = new HashSet<>();
        setRange2D(range2D);
    }

    public float volume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = Math.clamp(volume, 0f, 1f);
    }

    private void setRange2D(int range2D) {
        this.range = Math.max(range2D, 1);
    }

    public long playSound(CMediaSoundEffect cMediaSoundEffect) {
        return playSoundInternal(cMediaSoundEffect, 1, 1, 0, false);
    }

    public long playSound(CMediaSoundEffect cMediaSoundEffect, float volume) {
        return playSoundInternal(cMediaSoundEffect, volume, 1, 0, false);
    }

    public long playSound(CMediaSoundEffect cMediaSoundEffect, float volume, float pitch) {
        return playSoundInternal(cMediaSoundEffect, volume, pitch, 0, false);
    }

    public long playSound(CMediaSoundEffect cMediaSoundEffect, float volume, float pitch, float pan) {
        return playSoundInternal(cMediaSoundEffect, volume, pitch, pan, false);
    }

    public long loopSound(CMediaSoundEffect cMediaSoundEffect) {
        return playSoundInternal(cMediaSoundEffect, 1, 1, 0, true);
    }

    public long loopSound(CMediaSoundEffect cMediaSoundEffect, float volume) {
        return playSoundInternal(cMediaSoundEffect, volume, 1, 0, true);
    }

    public long loopSound(CMediaSoundEffect cMediaSoundEffect, float volume, float pitch) {
        return playSoundInternal(cMediaSoundEffect, volume, pitch, 0, true);
    }

    public long loopSound(CMediaSoundEffect cMediaSoundEffect, float volume, float pitch, float pan) {
        return playSoundInternal(cMediaSoundEffect, volume, pitch, pan, true);
    }

    public long playSound2D(CMediaSoundEffect cMediaSoundEffect, float position_x, float position_y) {
        return playSound2DInternal(cMediaSoundEffect, position_x, position_y, 1, 1, false);
    }

    public long playSound2D(CMediaSoundEffect cMediaSoundEffect, float position_x, float position_y, float volume) {
        return playSound2DInternal(cMediaSoundEffect, position_x, position_y, volume, 1, false);
    }

    public long playSound2D(CMediaSoundEffect cMediaSoundEffect, float position_x, float position_y, float volume, float pitch) {
        return playSound2DInternal(cMediaSoundEffect, position_x, position_y, volume, pitch, false);
    }

    public long loopSound2D(CMediaSoundEffect cMediaSoundEffect, float position_x, float position_y) {
        return playSound2DInternal(cMediaSoundEffect, position_x, position_y, 1, 1, true);
    }

    public long loopSound2D(CMediaSoundEffect cMediaSoundEffect, float position_x, float position_y, float volume) {
        return playSound2DInternal(cMediaSoundEffect, position_x, position_y, volume, 1, true);
    }

    public long loopSound2D(CMediaSoundEffect cMediaSoundEffect, float position_x, float position_y, float volume, float pitch) {
        return playSound2DInternal(cMediaSoundEffect, position_x, position_y, volume, pitch, true);
    }

    private long playSoundInternal(CMediaSoundEffect cMediaSoundEffect, float volume, float pitch, float pan, boolean loop) {
        long id;
        if(loop) {
            id = mediaManager.sound(cMediaSoundEffect).loop(volume * this.volume,pitch,pan);
        }else{
            id= mediaManager.sound(cMediaSoundEffect).play(volume * this.volume, pitch, pan);
        }
        playedSounds.add(cMediaSoundEffect);
        return id;
    }

    private long playSound2DInternal(CMediaSoundEffect cMediaSoundEffect, float position_x, float position_y, float volume, float pitch, boolean loop) {
        float playVolume = (range - (Math.clamp(Tools.Calc.distance(camera_x, camera_y, position_x, position_y), 0, range))) / (float) range;
        playVolume = playVolume * volume * this.volume;
        float pan = 0;
        if (camera_x > position_x) {
            pan = Math.clamp(-((camera_x - position_x) / (float) range), -1, 0);
        } else if (camera_x < position_x) {
            pan = Math.clamp((position_x - camera_x) / (float) range, 0, 1);
        }
        long id;
        if(loop) {
            id = mediaManager.sound(cMediaSoundEffect).loop(playVolume * this.volume,pitch,pan);
        }else{
            id= mediaManager.sound(cMediaSoundEffect).play(playVolume * this.volume, pitch, pan);
        }
        playedSounds.add(cMediaSoundEffect);
        return id;
    }

    public void stopAllSounds() {
        CMediaSoundEffect[] playedSoundsArray = playedSounds.toArray(new CMediaSoundEffect[0]);
        for (int i = 0; i < playedSoundsArray.length; i++) mediaManager.sound(playedSoundsArray[i]).stop();
    }

    public void update() {
        update(0, 0);
    }

    public void update(float camera_x, float camera_y) {
        this.camera_x = camera_x;
        this.camera_y = camera_y;
    }

    public void shutdown() {
        stopAllSounds();
        playedSounds.clear();
    }

}
