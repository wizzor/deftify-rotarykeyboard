package me.parviainen.visa.simplekeyboard;

import android.app.Activity;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.Pair;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import android.media.AudioManager;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;


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
    private List<Keyboard.Key> keys;
    private ArrayList<Pair<String, Integer>> keylist;
    private Integer currentKey;
    private Integer maxKeyIndex;
    private Integer mainKey = 3;

    private boolean caps = false;

    private Intent startIntent;
    private int startId;

    private String TAG = "ROTARYKB";

    @Override
    public View onCreateInputView() {
        kv = (KeyboardView)getLayoutInflater().inflate(R.layout.keyboard, null);
        keyboard = new Keyboard(this, R.xml.rotary);
        keysets = new ArrayList<Keyboard>();
        keysets.add(new Keyboard(this, R.xml.qwerty, R.integer.numeric));
        keysets.add(new Keyboard(this, R.xml.qwerty, R.integer.alpha));
        keysets.add(new Keyboard(this, R.xml.qwerty, R.integer.symbol));

        currentKeyset = 1;
        keyboardKeys = keysets.get(currentKeyset).getKeys();
        keys = keyboard.getKeys();
        keys.get(7+currentKeyset).pressed = true;

        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);

        currentKey = 0;
        maxKeyIndex=keyboardKeys.size()-1;
        updateKeys();
        Log.v(TAG, "Keyboard Service Started");
        return kv;
    }

    public void onStartInputView(EditorInfo attribute, boolean restarting){
        Log.v(TAG, "Keyboard Input Started");
        Intent intent = new Intent("me.parviainen.visa.simplekeyboard.KeyboardStateBroadcast");
        intent.putExtra("state", "ACTIVE");
        sendBroadcast(intent);
        super.onStartInputView(attribute, restarting);
    }

    public void onFinishInput(){
        Log.v(TAG, "Keyboard Input Finished");
        Intent intent = new Intent("me.parviainen.visa.simplekeyboard.KeyboardStateBroadcast");
        intent.putExtra("state", "INACTIVE");
        sendBroadcast(intent);
        super.onFinishInput();
    }


    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        sendKeycode(primaryCode);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startIdInput){
        startIntent = intent;
        startId = startIdInput;
        return super.onStartCommand(intent, flags, startId);
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

    private void playClick(){
        Log.v("Playing", "Click");
        AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
        am.playSoundEffect(AudioManager.FX_KEY_CLICK);

    }

    private ArrayList cycledArraySegment(List array, int start, int count){
        ArrayList output = new ArrayList();
        int maxArrayIndex = array.size()-1;
        Log.v("KEYS", "Start point:" +start + " Count:" +count);

        if(start > maxArrayIndex){
            return output;
        }else{
            int i = 0;
            if(start<0){
                for(i=maxArrayIndex+start+1; i<=maxArrayIndex; i++){
                    if(count>0) {
                        output.add(array.get(i));
                        count--;
                    } else {
                        break;
                    }
                }
                start = 0;
            }
            Log.v("KEYS", "Entering adder loop, with start: " +start);
            for (i=start; i<=maxArrayIndex; i++){

                if(count > 0) {
                    Log.v("KEYS", "Adding element: " +i);
                    output.add(array.get(i));
                    count -= 1;
                } else {
                    break;
                }

            }
            for (i=0; i<count; i++){
                output.add(array.get(i));
            }

            return output;
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
        keys = keyboard.getKeys();
        switch(keyCode){
            case 9: {
                incrementCurrentKey();
                playClick();
                Log.v("KEYS", "CODE"+keyCode+"CUR:"+keyboardKeys.get(currentKey).codes[0]);
                break;
            }
            case 8: {
                decrementCurrentKey();
                playClick();
                Log.v("KEYS", "CODE"+keyCode+"CUR:"+keyboardKeys.get(currentKey).codes[0]);
                break;
            }
            case 66: {
                sendKeycode(keyboardKeys.get(currentKey).codes[0]);
                Log.v("KEYS", "CODE"+keyCode+"CUR:"+keyboardKeys.get(currentKey).codes[0]);
                break;
            }
            case 19:{
                Log.v("KEYS", "CODE"+keyCode+"CUR:"+keyboardKeys.get(currentKey).codes[0]);
                Log.v("KEYS", "This should enable suggestions");
                break;
            }
            case 20:{
                Log.v("KEYS", "CODE"+keyCode+"CUR:"+keyboardKeys.get(currentKey).codes[0]);
                Log.v("KEYS", "This should close the keyboard");
                sendKeycode(-4);
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
            default:{
                Log.v("KEYS", "Unrecognized command:"+keyCode);
                return super.onKeyDown(keyCode, event);
            }
        }

        updateKeys();




        kv.invalidateKey(mainKey);
        return true;

    }

    private Integer incrementCurrentKey(){
        if(currentKey+1 > maxKeyIndex){
            currentKey = 0;
            Log.v("KEYS", "Current Key"+currentKey);
            return currentKey;
        }else{
            currentKey += 1;
            Log.v("KEYS", "Current Key"+currentKey);
            return currentKey;
        }
    }

    private Integer decrementCurrentKey(){
        if(currentKey-1 < 0){
            currentKey = maxKeyIndex;
            Log.v("KEYS", "Current Key"+currentKey);
            return currentKey;
        }else{
            currentKey -= 1;
            Log.v("KEYS", "Current Key"+currentKey);
            return currentKey;
        }
    }

    private void updateKeys(){
        Log.v("KEYS", "Updated Keys#:"+currentKeyset);

        int minKey = 0;
        int maxKey = 6;
        keys = keyboard.getKeys();
        ArrayList<Keyboard.Key> newKeys = cycledArraySegment(keyboardKeys, currentKey-3, 7);

        Log.v("KEYS", "New Keys#:"+newKeys.size());

        String listString = "";

        for (Keyboard.Key s : newKeys)
        {
            listString += s.label + " ";
        }

        Log.v("KEYS", "KEYS ARE HERE" + listString);


        for(int i=minKey; i<=maxKey; i++){
            keys.get(i).label = newKeys.get(i).label;
        }
        kv.invalidateAllKeys();

    }

    private void switchKeyboard(int amount){
        Log.v("KEYS", "KB#:"+currentKeyset);
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
        try {
            switch (primaryCode) {
                case Keyboard.KEYCODE_DELETE:
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
                    char code = (char) primaryCode;
                    if (Character.isLetter(code) && caps) {
                        code = Character.toUpperCase(code);
                    }
                    ic.commitText(String.valueOf(code), 1);
            }
        }catch(Throwable e){
            Log.v("KEYS", "Something went wrong");
        }
    }


}
