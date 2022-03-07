// IServiceManager.aidl
package com.cxz.ipcsample;

// Declare any non-default types here with import statements

interface IServiceManager {

    IBinder getService(String serviceName);

}