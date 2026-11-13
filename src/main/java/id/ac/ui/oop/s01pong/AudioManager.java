package id.ac.ui.oop.s01pong;

import javafx.scene.media.AudioClip;
import java.util.HashMap;
import java.util.Map;

public final class AudioManager {
    private static AudioManager instance;

    private Map<String, AudioClip> soundMap;

    private AudioManager() {
        soundMap = new HashMap<>();
    }

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    public void registerSound(String key, String filename) {
        var resource = AudioManager.class.getResource("/" + filename);

        if (resource == null) {
            throw new IllegalArgumentException("Audio file not found: " + filename);
        }

        soundMap.put(key, new AudioClip(resource.toExternalForm()));
    }

    public void playSound(String key) {
        AudioClip sound = soundMap.get(key);

        if (sound != null) {
            sound.play();
        }
    }

    public void init() {
        registerSound("hit", "freesound.mp3");
        registerSound("score", "hint.mp3");
        registerSound("wall", "Bite.mp3");
    }
}