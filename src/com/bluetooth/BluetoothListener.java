
package com.bluetooth;

/**
 * The listener interface for receiving bluetooth events. The class that is
 * interested in processing a bluetooth event implements this interface, and the
 * object created with that class is registered with a component using the
 * component's <code>addBluetoothListener</code> method. When the bluetooth
 * event occurs, that object's appropriate method is invoked.
 * 
 * @see BluetoothEvent
 */
public interface BluetoothListener {

    /**
     * On headset disconnected.
     */
    void onHeadsetDisconnected();

    /**
     * On headset connected.
     */
    void onHeadsetConnected();

    /**
     * On sco audio disconnected.
     */
    void onScoAudioDisconnected();

    /**
     * On sco audio connected.
     */
    void onScoAudioConnected();
}
