// IMessageService.aidl
package com.cxz.ipcsample;
import com.cxz.ipcsample.entity.Message;
import com.cxz.ipcsample.MessageReceiveListener;

// Declare any non-default types here with import statements

// 消息服务
interface IMessageService {

    // 实体类在作为参数时，要加 in/out/inout 关键字
    void sendMessage(inout Message message);

    void registerMessageReceiveListener(MessageReceiveListener messageReceiveListener);

    void unRegisterMessageReceiveListener(MessageReceiveListener messageReceiveListener);

}