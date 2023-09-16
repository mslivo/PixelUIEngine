package org.mslivo.core.engine.tools.sound;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;
import org.mslivo.core.engine.media_manager.MediaManager;
import org.mslivo.core.engine.media_manager.media.CMediaMusic;
import org.mslivo.core.engine.tools.Tools;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;

/*
 * Basic Music Player
 */
public class MusicPlayer {

    public static final byte PLAYMODE_SEQUENTIAL =0, PLAYMODE_RANDOM =1, PLAYMODE_LOOP=2;

    private static final byte STATE_PAUSE=0,STATE_PLAY=1,STATE_STOP=3;

    private byte playMode, state;

    private final ArrayList<CMediaMusic> playlist;

    private final MediaManager mediaManager;

    private Music playCurrent;

    private String playCurrentFileName;

    private int playListPosition;

    private boolean playNext, playPrevious;

    private float volume;

    private final ArrayDeque<Integer> randomHistory;

    public MusicPlayer(MediaManager mediaManager){
        this.mediaManager = mediaManager;
        this.playlist = new ArrayList<>();
        this.playMode = PLAYMODE_SEQUENTIAL;
        this.state = STATE_STOP;
        this.playCurrent = null;
        this.playCurrentFileName = "";
        this.playListPosition = 0;
        this.randomHistory = new ArrayDeque<>();
        this.playNext = this.playPrevious = false;
        this.volume = 1f;
    }

    public void playlist_clear(){
        playlist.clear();
    }

    public ArrayList<CMediaMusic> getPlayList(){
        return new ArrayList<>(playlist);
    }

    public int getCurrentPlayListPosition(){
        return playListPosition;
    }

    public int getMusicPlayListPosition(CMediaMusic cMediaMusic){
        for(int i=0;i<playlist.size();i++){
            if(playlist.get(i) == cMediaMusic){
                return i;
            }
        }
        return -1;
    }

    public void playListRemove(int index){
        if(playlist.size() > 0 && (index > 0 && index < playlist.size()-1)) {
            playlist.remove(index);
        }
    }

    public void playListRemove(CMediaMusic cMediaMusic){
        playlist.remove(cMediaMusic);
    }

    public boolean isPlaying(){
        return playCurrent != null && playCurrent.isPlaying();
    }

    public void playListAdd(CMediaMusic cMediaMusic){
        playlist.add(cMediaMusic);
    }

    public void playListSwap(int index1, int index2){
        Collections.swap(playlist,index1, index2);
    }

    public void update(){
        switch (state){
            case  STATE_STOP->{
                if(playCurrent != null){
                    if(playCurrent.isPlaying()){
                        playCurrent.stop();
                    }
                }
            }
            case  STATE_PAUSE->{
                if(playCurrent != null){
                    if(playCurrent.isPlaying()){
                        playCurrent.pause();
                    }
                }
            }
            case STATE_PLAY -> {
                if(playCurrent != null){
                    if(playCurrent.isPlaying()){
                        if(playCurrent.getVolume() != volume){
                            playCurrent.setVolume(volume);
                        }
                        if(playNext || playPrevious ){
                            playCurrent.stop();
                            playCurrent = null;
                            playCurrentFileName = "";
                        }
                    }else{
                        playCurrent.play();
                    }
                }else{
                    if(playlist.size() > 0){
                        if(!playNext && !playPrevious){
                            // Default: next song
                            playNext = true;
                        }

                        if(playNext){
                            switch (playMode) {
                                case PLAYMODE_RANDOM -> {
                                    randomHistory.add(playListPosition);
                                    if (randomHistory.size() > 128) randomHistory.removeFirst();

                                    int newPosition = 0;
                                    if(playlist.size() >1){
                                        int tries = 0;
                                        while(newPosition == playListPosition && tries < 5){
                                            newPosition = MathUtils.random(0, playlist.size() - 1);
                                            tries++;
                                        }
                                    }
                                    playListPosition = newPosition;
                                }
                                case PLAYMODE_SEQUENTIAL -> playListPosition = (playListPosition + 1 > (playlist.size() - 1) ? 0 : (playListPosition + 1));
                                case PLAYMODE_LOOP -> {}
                            }
                            playNext = false;
                        }else if(playPrevious){
                            switch (playMode) {
                                case PLAYMODE_RANDOM->{
                                    Integer historyPosition = randomHistory.pollLast();
                                    if(historyPosition != null && historyPosition >0 && historyPosition<playlist.size()-1 ){
                                        playListPosition = historyPosition;
                                    }else{
                                        playListPosition = MathUtils.random(0, playlist.size() - 1);
                                    }
                                }
                                case PLAYMODE_SEQUENTIAL -> playListPosition = (playListPosition - 1 < 0 ? playlist.size()-1 : playListPosition -1);
                                case PLAYMODE_LOOP -> {}
                            }
                            playPrevious = false;
                        }

                        CMediaMusic nextTrack = playlist.get(playListPosition);
                        playCurrent = mediaManager.getCMediaMusic(nextTrack);
                        if(playCurrent != null) {
                            playCurrentFileName = nextTrack.file;
                            playCurrent.setVolume(volume);
                            playCurrent.setOnCompletionListener(music -> {
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

    public void setPlayModeLoop(){
        this.playMode = PLAYMODE_LOOP;
    }

    public void setPlayModeRandom(){
        this.playMode = PLAYMODE_RANDOM;
    }

    public void playPosition(int playListPosition){
        this.playListPosition = playListPosition;
        this.playNext = true;
    }

    public void playPositionIfNotAlreadyPlaying(int playListPosition){
        if(this.playListPosition == playListPosition) return;
        this.playListPosition = playListPosition;
        this.playNext = true;
    }

    public String getCurrentPlayedFileName(){
        return playCurrentFileName;
    }

    public int getPlayMode(){
        return this.playMode;
    }

    public void setPlayModeSequential(){
        this.playMode = PLAYMODE_SEQUENTIAL;
    }

    public void setVolume(float volume) {
        this.volume = Tools.Calc.inBounds(volume, 0f, 1f);
    }

    public float volume() {
        return volume;
    }

    public void play(){
        this.state = STATE_PLAY;
    }

    public void previous(){
        this.playPrevious = true;
        this.state = STATE_PLAY;
    }

    public void next(){
        this.playNext = true;
        this.state = STATE_PLAY;
    }

    public void pause(){
        this.state = (this.state == STATE_PAUSE ? STATE_PLAY : STATE_PAUSE);
    }

    public void stop(){
        this.state = STATE_STOP;
    }

    public void shutdown(){
        if(playCurrent != null && playCurrent.isPlaying()) playCurrent.stop();
        this.playCurrent = null;
        this.playCurrentFileName = "";
        this.playlist.clear();
        this.randomHistory.clear();
    }

}
