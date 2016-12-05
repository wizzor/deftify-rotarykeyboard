package me.parviainen.visa.simplekeyboard;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.Pair;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.view.KeyEvent;
import android.media.AudioManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by visa on 30.11.2016.
 */

public class SimpleIME extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    private KeyboardView kv;
    private Keyboard keyboard;
    private List<Keyboard.Key> keyboardKeys;
    private ArrayList<Pair<String, Integer>> keylist;
    private Integer currentKey;
    private Integer maxKeyIndex;

    private boolean caps = false;

    @Override
    public View onCreateInputView() {
        kv = (KeyboardView)getLayoutInflater().inflate(R.layout.keyboard, null);
        keyboard = new Keyboard(this, R.xml.rotary);
        Keyboard tmp_keyboard = new Keyboard(this, R.xml.qwerty, R.integer.alpha);
        keyboardKeys = tmp_keyboard.getKeys();

        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);
        keylist = new ArrayList<>();
        keylist.add(new Pair<String, Integer>("a", 97));
        keylist.add(new Pair<String, Integer>("b", 98));
        keylist.add(new Pair<String, Integer>("c", 99));
        keylist.add(new Pair<String, Integer>("d", 100));
        keylist.add(new Pair<String, Integer>("SPACE", 32));
        keylist.add(new Pair<String, Integer>("DONE", -4));
        keylist.add(new Pair<String, Integer>("DEL", -5));

        currentKey = 0;
        maxKeyIndex=keyboardKeys.size()-1;
        return kv;
    }


    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        sendKeycode(primaryCode);
    }

    private void playClick(int keyCode){
        Log.v("Playing", "Click");
        AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
        switch(keyCode){
            case 32:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;
            case Keyboard.KEYCODE_DONE:
            case 10:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;
            default: am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
        }
    }

    @Override
    public void onPress(int primaryCode) {
    }

    @Override
    public void onRelease(int primaryCode) {
    }

    @Override
    public void onText(CharSequence text) {
    }

    @Override
    public void swipeDown() {
    }

    @Override
    public void swipeLeft() {
    }

    @Override
    public void swipeRight() {
    }

    @Override
    public void swipeUp() {
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        Log.v("KEYS", "Keydown"+keyCode);
        List<Keyboard.Key> keys = keyboard.getKeys();
        switch(keyCode){
            case 8: {
                incrementCurrentKey();
                keys.get(0).label = keyboardKeys.get(currentKey).label;
                Log.v("KEYS", "CUR:"+keyboardKeys.get(currentKey).codes[0]);
                break;
            }
            case 9: {
                decrementCurrentKey();
                keys.get(0).label = keyboardKeys.get(currentKey).label;
                Log.v("KEYS", "CUR:"+keyboardKeys.get(currentKey).codes[0]);
                break;
            }
            case 66: {
                sendKeycode(keyboardKeys.get(currentKey).codes[0]);
                Log.v("KEYS", "CUR:"+keyboardKeys.get(currentKey).codes[0]);
                break;
            }
        }




        kv.invalidateKey(0);
        return true;

    }

    private Integer incrementCurrentKey(){
        if(currentKey+1 > maxKeyIndex){
            currentKey = 0;
            return currentKey;
        }else{
            currentKey += 1;
            return currentKey;
        }
    }

    private Integer decrementCurrentKey(){
        if(currentKey-1 < 0){
            currentKey = maxKeyIndex;
            return currentKey;
        }else{
            currentKey -= 1;
            return currentKey;
        }
    }

    private void sendKeycode(int primaryCode){

        InputConnection ic = getCurrentInputConnection();
        playClick(primaryCode);
        switch(primaryCode){
            case Keyboard.KEYCODE_DELETE :
                ic.deleteSurroundingText(1, 0);
                break;
            case Keyboard.KEYCODE_SHIFT:
                caps = !caps;
                keyboard.setShifted(caps);
                kv.invalidateAllKeys();
                break;
            case Keyboard.KEYCODE_DONE:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
            default:
                char code = (char)primaryCode;
                if(Character.isLetter(code) && caps){
                    code = Character.toUpperCase(code);
                }
                ic.commitText(String.valueOf(code),1);
        }
    }


}
