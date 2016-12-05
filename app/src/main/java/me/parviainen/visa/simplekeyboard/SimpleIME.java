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
    private int currentKeyset;
    private List<Keyboard> keysets;
    private List<Keyboard.Key> keyboardKeys;
    private ArrayList<Pair<String, Integer>> keylist;
    private Integer currentKey;
    private Integer maxKeyIndex;
    private Integer mainKey = 3;

    private boolean caps = false;

    @Override
    public View onCreateInputView() {
        kv = (KeyboardView)getLayoutInflater().inflate(R.layout.keyboard, null);
        keyboard = new Keyboard(this, R.xml.rotary);
        keysets = new ArrayList<Keyboard>();
        keysets.add(new Keyboard(this, R.xml.qwerty, R.integer.numeric));
        keysets.add(new Keyboard(this, R.xml.qwerty, R.integer.alpha));
        keysets.add(new Keyboard(this, R.xml.qwerty, R.integer.symbol));
        keyboardKeys = keysets.get(0).getKeys();
        currentKeyset = 1;
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);

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
            case 9: {
                incrementCurrentKey();
                keys.get(mainKey).label = keyboardKeys.get(currentKey).label;
                Log.v("KEYS", "CUR:"+keyboardKeys.get(currentKey).codes[0]);
                break;
            }
            case 8: {
                decrementCurrentKey();
                keys.get(mainKey).label = keyboardKeys.get(currentKey).label;
                Log.v("KEYS", "CUR:"+keyboardKeys.get(currentKey).codes[0]);
                break;
            }
            case 66: {
                sendKeycode(keyboardKeys.get(currentKey).codes[0]);
                Log.v("KEYS", "CUR:"+keyboardKeys.get(currentKey).codes[0]);
                break;
            }
            case 21:{
                switchKeyboard(-1);
                break;
            }
            case 22:{
                switchKeyboard(1);
                break;
            }
        }




        kv.invalidateKey(mainKey);
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

    private void switchKeyboard(int amount){
        Log.v("KEYS", "KB#:"+currentKeyset);
        List<Keyboard.Key> keys = keyboard.getKeys();
        keys.get(7+currentKeyset).pressed = false;
        if(amount > 0){
            amount = 1;
        }else{
            amount = -1;
        }
        Log.v("KEYS", "amount#:"+amount);
        if(currentKeyset+amount > keysets.size()-1) {
            currentKeyset = 0;
            Log.v("KEYS", "Too big#:"+currentKeyset);
        }else if(currentKeyset+amount < 0){
            currentKeyset = keysets.size()-1;
            Log.v("KEYS", "too small#:"+currentKeyset);
        }else{
            Log.v("KEYS", "Increasing by#:"+amount);
            currentKeyset += amount;
        }
        keyboardKeys = keysets.get(currentKeyset).getKeys();
        maxKeyIndex=keyboardKeys.size()-1;
        currentKey = 0;
        keys.get(0).label = keyboardKeys.get(currentKey).label;
        kv.invalidateAllKeys();
        keys.get(7+currentKeyset).pressed = true;
        Log.v("KEYS", "KB#:"+currentKeyset);

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
