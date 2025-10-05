package net.mslivo.pixelui.utils.sound;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.*;
import net.mslivo.pixelui.media.CMediaSoundEffect;
import net.mslivo.pixelui.media.MediaManager;
import net.mslivo.pixelui.utils.Tools;

/*
 * Plays sounds and automatically adjusts volume to distance
 */
public class SoundPlayer implements Disposable{
    private int range;
    private float volume;
    private float camera_x, camera_y;
    private final MediaManager mediaManager;
    private final Array<CMediaSoundEffect> playedSounds;
    private final ObjectMap<CMediaSoundEffect, LongArray> playedSoundIds;

    public SoundPlayer(MediaManager mediaManager) {
        this(mediaManager, 0);
    }

    public SoundPlayer(MediaManager mediaManager, int range2D) {
        this.mediaManager = mediaManager;
        this.volume = 1f;
        this.playedSounds = new Array<>();
        this.playedSoundIds = new ObjectMap<>();
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
        return playSoundInternal(cMediaSoundEffect, 0f,0f, 1f,1f,0f,false,false);
    }

    public long playSound(CMediaSoundEffect cMediaSoundEffect,  float volume) {
        return playSoundInternal(cMediaSoundEffect, 0f,0f, volume,1f,0f,false,false);
    }

    public long playSound(CMediaSoundEffect cMediaSoundEffect,  float volume, float pitch) {
        return playSoundInternal(cMediaSoundEffect, 0f,0f, volume,pitch,0f,false,false);
    }

    public long playSound(CMediaSoundEffect cMediaSoundEffect,  float volume, float pitch, float pan) {
        return playSoundInternal(cMediaSoundEffect, 0f,0f, volume,pitch,pan,false,false);
    }

    public long loopSound(CMediaSoundEffect cMediaSoundEffect) {
        return playSoundInternal(cMediaSoundEffect, 0f,0f, 1f,1f,0f,true,false);
    }

    public long loopSound(CMediaSoundEffect cMediaSoundEffect,  float volume) {
        return playSoundInternal(cMediaSoundEffect, 0f,0f, volume,1f,0f,true,false);
    }

    public long loopSound(CMediaSoundEffect cMediaSoundEffect,  float volume, float pitch) {
        return playSoundInternal(cMediaSoundEffect, 0f,0f, volume,pitch,0f,true,false);
    }

    public long loopSound(CMediaSoundEffect cMediaSoundEffect,  float volume, float pitch, float pan) {
        return playSoundInternal(cMediaSoundEffect, 0f,0f, volume,pitch,pan,true,false);
    }

    public long playSound2D(CMediaSoundEffect cMediaSoundEffect, float position_x, float position_y) {
        return playSoundInternal(cMediaSoundEffect, position_x, position_y, 1f,1f,0f,false,true);
    }

    public long playSound2D(CMediaSoundEffect cMediaSoundEffect, float position_x, float position_y, float volume) {
        return playSoundInternal(cMediaSoundEffect, position_x, position_y, volume,1f,0f,false,true);
    }

    public long playSound2D(CMediaSoundEffect cMediaSoundEffect, float position_x, float position_y, float volume, float pitch) {
        return playSoundInternal(cMediaSoundEffect, position_x, position_y, volume,pitch,0f,false,true);
    }

    public long playSound2D(CMediaSoundEffect cMediaSoundEffect, float position_x, float position_y, float volume, float pitch, float pan) {
        return playSoundInternal(cMediaSoundEffect, position_x, position_y, volume,pitch,pan,false,true);
    }

    public long loopSound2D(CMediaSoundEffect cMediaSoundEffect, float position_x, float position_y) {
        return playSoundInternal(cMediaSoundEffect, position_x, position_y, 1f,1f,0f,true,true);
    }

    public long loopSound2D(CMediaSoundEffect cMediaSoundEffect, float position_x, float position_y, float volume) {
        return playSoundInternal(cMediaSoundEffect, position_x, position_y, volume,1f,0f,true,true);
    }

    public long loopSound2D(CMediaSoundEffect cMediaSoundEffect, float position_x, float position_y, float volume, float pitch) {
        return playSoundInternal(cMediaSoundEffect, position_x, position_y, volume,pitch,0f,true,true);
    }

    public long loopSound2D(CMediaSoundEffect cMediaSoundEffect, float position_x, float position_y, float volume, float pitch, float pan) {
        return playSoundInternal(cMediaSoundEffect, position_x, position_y, volume,pitch,pan,true,true);
    }

    private long playSoundInternal(CMediaSoundEffect cMediaSoundEffect,float position_x, float position_y, float volume, float pitch, float pan, boolean loop, boolean play2D) {
        final Sound sound = mediaManager.sound(cMediaSoundEffect);
        final float playVolume, playPan;
        if (play2D) {
            float positionVolume = (range - (Math.clamp(Tools.Calc.distance(camera_x, camera_y, position_x, position_y), 0, range))) / (float) range;
            float positionPan = 0;
            if (camera_x > position_x) {
                pan = Math.clamp(-((camera_x - position_x) / (float) range), -1, 0);
            } else if (camera_x < position_x) {
                pan = Math.clamp((position_x - camera_x) / (float) range, 0, 1);
            }
            playVolume = positionVolume * this.volume * volume;
            playPan = Math.clamp(positionPan + pan, -1f, 1f);
        } else {
            playVolume = this.volume*volume;
            playPan = pan;
        }

        final long soundId;
        if (loop) {
            soundId= sound.loop(playVolume, pitch, playPan);
        } else {
            soundId= sound.play(playVolume, pitch, playPan);
        }

        if(soundId != -1) {
            playedSounds.add(cMediaSoundEffect);
            if (!playedSoundIds.containsKey(cMediaSoundEffect)) {
                playedSoundIds.put(cMediaSoundEffect, new LongArray());
            }
            LongArray soundIds = playedSoundIds.get(cMediaSoundEffect);
            soundIds.add(soundId);
        }

        return soundId;
    }

    public void stopAllSounds(){
        this.stopSounds(this.playedSounds);
    }

    public void stopSounds(Array<CMediaSoundEffect> stopSounds){
        for(int i=0;i<stopSounds.size;i++) {
            final CMediaSoundEffect soundEffect = stopSounds.get(i);
            stopSound(soundEffect);
        }
    }

    public void stopSound(CMediaSoundEffect soundEffect){
        if(!this.playedSoundIds.containsKey(soundEffect))
            return;
        final LongArray ids = this.playedSoundIds.get(soundEffect);
        for (int i = 0; i < ids.size; i++) {
            long id = ids.get(i);
            mediaManager.sound(soundEffect).stop(id);
        }
        this.playedSounds.removeValue(soundEffect, true);
        this.playedSoundIds.remove(soundEffect);
    }

    public void update() {
        update(0, 0);
    }

    public void update(float camera_x, float camera_y) {
        this.camera_x = camera_x;
        this.camera_y = camera_y;
    }

    @Override
    public void dispose() {
        stopAllSounds();
    }


}
