package net.mslivo.pixelui.utils.sound;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntArray;
import net.mslivo.pixelui.media.CMediaMusic;
import net.mslivo.pixelui.media.MediaManager;

/*
 * Basic Music Player
 */
public class MusicPlayer implements Disposable {

    public enum PLAY_MODE {
        SEQUENTIAL, RANDOM, REPEAT
    }

    private enum STATE {
        PAUSE, PLAY, STOP
    }

    private STATE state;
    private PLAY_MODE playMode;

    private final Array<CMediaMusic> playlist;

    private final MediaManager mediaManager;

    private Music playCurrent;

    private String playCurrentFileName;

    private int playListPosition;

    private boolean playNext, playPrevious;

    private float volume;

    private final IntArray randomHistory;

    public MusicPlayer(MediaManager mediaManager) {
        this(mediaManager,null);
    }

    public MusicPlayer(MediaManager mediaManager, CMediaMusic... playlist) {
        this.mediaManager = mediaManager;
        this.playlist = new Array<>();
        this.playMode = PLAY_MODE.SEQUENTIAL;
        this.state = STATE.STOP;
        this.playCurrent = null;
        this.playCurrentFileName = "";
        this.playListPosition = 0;
        this.randomHistory = new IntArray();
        this.playNext = this.playPrevious = false;
        this.volume = 1f;
        if(playlist != null)
            for(int i=0;i<playlist.length;i++)
                playListAdd(playlist[i]);
    }

    public void playlistClear() {
        playlist.clear();
    }

    public Array<CMediaMusic> getPlayList() {
        return new Array<>(playlist);
    }

    public int getCurrentPlayListPosition() {
        return playListPosition;
    }

    public int getMusicPlayListPosition(CMediaMusic cMediaMusic) {
        for (int i = 0; i < playlist.size; i++) {
            if (playlist.get(i) == cMediaMusic) {
                return i;
            }
        }
        return -1;
    }

    public void playListRemove(int index) {
        if (!playlist.isEmpty() && (index > 0 && index < playlist.size - 1)) {
            playlist.removeIndex(index);
        }
    }

    public void playListRemove(CMediaMusic cMediaMusic) {
        playlist.removeValue(cMediaMusic, true);
    }

    public boolean isPlaying() {
        return playCurrent != null && playCurrent.isPlaying();
    }

    public void playListAdd(CMediaMusic cMediaMusic) {
        playlist.add(cMediaMusic);
    }

    public void playListSwap(int index1, int index2) {
        playlist.swap(index1, index2);
    }

    public void update() {
        switch (state) {
            case STOP -> {
                if (playCurrent != null) {
                    if (playCurrent.isPlaying()) {
                        playCurrent.stop();
                    }
                }
            }
            case PAUSE -> {
                if (playCurrent != null) {
                    if (playCurrent.isPlaying()) {
                        playCurrent.pause();
                    }
                }
            }
            case PLAY -> {
                if (playCurrent != null) {
                    if (playCurrent.isPlaying()) {
                        if (playCurrent.getVolume() != volume) {
                            playCurrent.setVolume(volume);
                        }
                        if (playNext || playPrevious) {
                            playCurrent.stop();
                            playCurrent = null;
                            playCurrentFileName = "";
                        }
                    } else {
                        playCurrent.play();
                    }
                } else {
                    if (!playlist.isEmpty()) {
                        if (!playNext && !playPrevious) {
                            // Default: next song
                            playNext = true;
                        }

                        if (playNext) {
                            switch (playMode) {
                                case RANDOM -> {
                                    randomHistory.add(playListPosition);
                                    if (randomHistory.size > 128) randomHistory.removeIndex(0);

                                    int newPosition = 0;
                                    if (playlist.size > 1) {
                                        int tries = 0;
                                        while (newPosition == playListPosition && tries < 5) {
                                            newPosition = MathUtils.random(0, playlist.size - 1);
                                            tries++;
                                        }
                                    }
                                    playListPosition = newPosition;
                                }
                                case SEQUENTIAL ->
                                        playListPosition = (playListPosition + 1 > (playlist.size - 1) ? 0 : (playListPosition + 1));
                                case REPEAT -> {
                                }
                            }
                            playNext = false;
                        } else if (playPrevious) {
                            switch (playMode) {
                                case RANDOM -> {
                                    if (randomHistory.isEmpty()) {
                                        playListPosition = MathUtils.random(0, playlist.size - 1);
                                    } else {
                                        playListPosition = randomHistory.get(randomHistory.size - 1);
                                    }
                                }
                                case SEQUENTIAL ->
                                        playListPosition = (playListPosition - 1 < 0 ? playlist.size - 1 : playListPosition - 1);
                                case REPEAT -> {
                                }
                            }
                            playPrevious = false;
                        }

                        CMediaMusic nextTrack = playlist.get(playListPosition);
                        playCurrent = mediaManager.music(nextTrack);
                        playCurrent.play();
                        if (playCurrent != null) {
                            playCurrentFileName = nextTrack.file;
                            playCurrent.setVolume(volume);
                            playCurrent.setOnCompletionListener(_ -> {
                                playCurrent.stop();
                                playCurrent = null;
                                playCurrentFileName = "";
                                playNext = true;
                            });
                        }
                    }
                }
            }
        }
    }

    public void setPlayMode(PLAY_MODE playMode) {
        if (playMode == null) return;
        this.playMode = playMode;
    }

    public PLAY_MODE getPlayMode() {
        return this.playMode;
    }

    public void playPosition(int playListPosition) {
        this.playListPosition = playListPosition;
        this.playNext = true;
    }

    public void playPositionIfNotAlreadyPlaying(int playListPosition) {
        if (this.playListPosition == playListPosition) return;
        this.playListPosition = playListPosition;
        this.playNext = true;
    }

    public String getCurrentPlayedFileName() {
        return playCurrentFileName;
    }

    public void setVolume(float volume) {
        this.volume = Math.clamp(volume, 0f, 1f);
    }

    public float volume() {
        return volume;
    }

    public void play() {
        if(!this.isPlaying()){
            this.state = STATE.PLAY;
        }
    }

    public void stop() {
        this.state = STATE.STOP;
    }

    public void previous() {
        this.playPrevious = true;
        this.state = STATE.PLAY;
    }

    public void next() {
        this.playNext = true;
        this.state = STATE.PLAY;
    }

    public void pause() {
        this.state = (this.state == STATE.PAUSE ? STATE.PLAY : STATE.PAUSE);
    }

    public boolean isPaused() {
        return state == STATE.PAUSE;
    }

    @Override
    public void dispose() {
        if (playCurrent != null && playCurrent.isPlaying()) playCurrent.stop();
        this.playCurrent = null;
        this.playCurrentFileName = "";
        this.playlist.clear();
        this.randomHistory.clear();
    }

}
