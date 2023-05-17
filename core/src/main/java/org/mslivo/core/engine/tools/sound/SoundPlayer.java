package org.mslivo.core.engine.tools.sound;

import org.mslivo.core.engine.media_manager.MediaManager;
import org.mslivo.core.engine.media_manager.media.CMediaSound;
import org.mslivo.core.engine.tools.Tools;

/*
 * Plays sounds and automatically adjusts volume to distance
 */
public class SoundPlayer {

    private int range;
    private float volume;

    private float camera_x, camera_y;

    private final MediaManager mediaManager;

    private float muteVolume;

    public SoundPlayer(MediaManager mediaManager) {
        this(mediaManager, 0);
    }
    public SoundPlayer(MediaManager mediaManager, int range2D) {
        this.volume = 0;
        this.mediaManager = mediaManager;
        this.muteVolume = 0;
        set2DRange(range2D);
        setVolume(1f);
    }

    public float volume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = Tools.Calc.inBounds(volume, 0f, 1f);
    }

    private void set2DRange(int range2D) {
        this.range = Tools.Calc.lowerBounds(range2D, 1);
    }

    private boolean isVolumeZero(float volume){
        return (volume*this.volume) <= 0;
    }

    public long playSound(CMediaSound cMediaSound, float volume, float pan, float pitch) {
        if(isVolumeZero(volume)) return -1;
        return mediaManager.playCMediaSound(cMediaSound, volume * this.volume, pan, pitch);
    }

    public long playSound(CMediaSound cMediaSound, float volume, float pan) {
        if(isVolumeZero(volume)) return -1;
        return playSound(cMediaSound, volume, pan, 1);
    }

    public long playSound(CMediaSound cMediaSound, float volume) {
        if(isVolumeZero(volume)) return -1;
        return playSound(cMediaSound, volume, 0, 1);
    }

    public long playSound(CMediaSound cMediaSound) {
        if(isVolumeZero(volume)) return -1;
        return playSound(cMediaSound, 1, 0, 1);
    }

    public long playSound2D(CMediaSound cMediaSound, float position_x, float position_y, float volume, float pitch) {
        if(isVolumeZero(volume)) return -1;
        float playVolume = (range - (Tools.Calc.inBounds(Tools.Calc.distancef(camera_x, camera_y, position_x, position_y), 0, range))) / (float) range;
        playVolume = playVolume * volume * this.volume;
        float pan = 0;
        if (camera_x > position_x) {
            pan = Tools.Calc.inBounds(-((camera_x - position_x) / (float) range), -1, 0);
        } else if (camera_x < position_x) {
            pan = Tools.Calc.inBounds((position_x - camera_x) / (float) range, 0, 1);
        }
        return mediaManager.playCMediaSound(cMediaSound, playVolume * this.volume, pan, pitch);
    }

    public void playSound2D(CMediaSound cMediaSound, float position_x, float position_y, float volume) {
        if(isVolumeZero(volume)) return;
        playSound2D(cMediaSound, position_x, position_y, volume, 1);
    }

    public void playSound2D(CMediaSound cMediaSound, float position_x, float position_y) {
        if(isVolumeZero(volume)) return;
        playSound2D(cMediaSound, position_x, position_y, 1, 1);
    }

    public void setPlayingSoundVolume(CMediaSound cMediaSound, long id, float volume){
        mediaManager.getCMediaSound(cMediaSound).setVolume(id, this.volume*volume);
    }

    public void mute() {
        this.muteVolume = this.volume;
        setVolume(0f);
    }

    public void unmute() {
        setVolume(this.muteVolume);
        this.muteVolume = 0;
    }

    public void update() {
        update(0,0);
    }

    public void update(float camera_x, float camera_y) {
        this.camera_x = camera_x;
        this.camera_y = camera_y;
    }

    public void shutdown() {
    }

}
