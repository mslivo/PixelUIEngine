package org.mslivo.core.engine.tools.soundplayer2d;

import org.mslivo.core.engine.media_manager.MediaManager;
import org.mslivo.core.engine.media_manager.media.CMediaSound;
import org.mslivo.core.engine.tools.Tools;

/*
 * Plays sounds and automatically adjusts volume to distance
 */
public class SoundPlayer2D {

    private int range;
    private float volume;

    private float camera_x, camera_y;

    private final MediaManager mediaManager;

    public SoundPlayer2D(MediaManager mediaManager, int range) {
        this.volume = 0;
        this.mediaManager = mediaManager;
        setRange(range);
        setVolume(1f);
    }

    public float volume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = Tools.Calc.inBounds(volume, 0f, 1f);
    }

    private void setRange(int range) {
        this.range = Tools.Calc.lowerBounds(range, 1);
    }

    public long playSoundSimple(CMediaSound cMediaSound, float volume, float pan, float pitch) {
        return mediaManager.playCMediaSound(cMediaSound, volume * this.volume, pan, pitch);
    }

    public long playSoundSimple(CMediaSound cMediaSound, float volume, float pan) {
        return playSoundSimple(cMediaSound, volume, pan, 1);
    }

    public long playSoundSimple(CMediaSound cMediaSound, float volume) {
        return playSoundSimple(cMediaSound, volume, 0, 1);
    }

    public long playSoundSimple(CMediaSound cMediaSound) {
        return playSoundSimple(cMediaSound, 1, 0, 1);
    }

    public long playSound(CMediaSound cMediaSound, float position_x, float position_y, float volume, float pitch) {
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

    public void playSound(CMediaSound cMediaSound, float position_x, float position_y, float volume) {
        playSound(cMediaSound, position_x, position_y, volume, 1);
    }

    public void playSound(CMediaSound cMediaSound, float position_x, float position_y) {
        playSound(cMediaSound, position_x, position_y, 1, 1);
    }

    public void setPlayingSoundVolume(CMediaSound cMediaSound, long id, float volume){
        mediaManager.getCMediaSound(cMediaSound).setVolume(id, this.volume*volume);
    }

    public void update(float camera_x, float camera_y) {
        this.camera_x = camera_x;
        this.camera_y = camera_y;
    }

    public void shutdown() {
        return;
    }

}
