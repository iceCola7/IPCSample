// IConnectionService.aidl
package com.cxz.ipcsample;

// Declare any non-default types here with import statements

// 连接服务
interface IConnectionService {

    // oneway关键字：没有返回值，不管子进程怎么实现
    oneway void connect();

    void disconnect();

    boolean isConnected();

}