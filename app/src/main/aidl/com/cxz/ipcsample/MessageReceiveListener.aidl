// MessageReceiveListener.aidl
package com.cxz.ipcsample;
import com.cxz.ipcsample.entity.Message;

// Declare any non-default types here with import statements

interface MessageReceiveListener {

    void onReceiveMessage(in Message message);

}