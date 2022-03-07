package com.cxz.ipcsample

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import android.widget.Toast
import com.cxz.ipcsample.entity.Message
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class RemoteService : Service() {

    private val TAG = "RemoteAIDL"

    private var bConnected = false

    private val handler: Handler = object: Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: android.os.Message) {
            super.handleMessage(msg)

            val bundle = msg.data
            bundle.classLoader = Message::class.java.classLoader
            val message = bundle.getParcelable<Message>("message")
            Log.d(TAG, "handleMessage = $message")
            Toast.makeText(this@RemoteService, "${message?.content}", Toast.LENGTH_SHORT).show()

            // reply to
            val clientMessenger = msg.replyTo
            val reply = Message().apply {
                content = "message from remote by messenger"
            }
            val bundle2 = Bundle().apply {
                putParcelable("message", reply)
            }
            val data = android.os.Message().apply {
                data = bundle2
            }
            clientMessenger?.send(data)
        }
    }

    // private val messageReceiveListenerList = mutableListOf<MessageReceiveListener>()
    // 跨进程回调集合
    private val remoteCallbackList = RemoteCallbackList<MessageReceiveListener>()

    private var scheduledThreadPoolExecutor: ScheduledThreadPoolExecutor? = null

    private var scheduledFuture: ScheduledFuture<*>? = null

    private var messenger = Messenger(handler)

    private var connectService: IConnectionService = object : IConnectionService.Stub() {

        override fun connect() {
            Thread.sleep(2000)
            bConnected = true
            handler.post {
                Toast.makeText(this@RemoteService, "connect", Toast.LENGTH_SHORT).show()
            }
            scheduledFuture = scheduledThreadPoolExecutor?.scheduleAtFixedRate(Runnable {
                val size = remoteCallbackList.beginBroadcast()
                for (i in 0 until size) {
                    val messageReceiveListener = remoteCallbackList.getBroadcastItem(i)
                    val message = Message().apply {
                        content = "this message form remote"
                        isSendSuccess = bConnected
                    }
                    messageReceiveListener.onReceiveMessage(message)
                }
                remoteCallbackList.finishBroadcast()
            }, 5000, 5000, TimeUnit.MILLISECONDS)
        }

        override fun disconnect() {
            bConnected = false
            scheduledFuture?.cancel(true)
            handler.post {
                Toast.makeText(this@RemoteService, "disconnect", Toast.LENGTH_SHORT).show()
            }
        }

        override fun isConnected(): Boolean {
            return bConnected
        }
    }

    private var messageService: IMessageService = object : IMessageService.Stub() {

        override fun sendMessage(message: Message?) {
            message?.isSendSuccess = bConnected
            Log.d(TAG, "sendMessage = $message")
            handler.post {
                Toast.makeText(this@RemoteService, "${message?.content}", Toast.LENGTH_SHORT).show()
            }
        }

        /**
         * 注意：
         * 注册的messageReceiveListener和取消注册的messageReceiveListener来到remote进程其实是两个不同的对象
         * 因为messageReceiveListener对象来到remote进程需要经过序列化和反序列化，这两个过程创建了两个新的对象
         *
         * 需要使用RemoteCallbackList替代ArrayList来存储messageReceiveListener对象
         */
        override fun registerMessageReceiveListener(messageReceiveListener: MessageReceiveListener?) {
            if (messageReceiveListener != null) {
                remoteCallbackList.register(messageReceiveListener)
            }
        }

        override fun unRegisterMessageReceiveListener(messageReceiveListener: MessageReceiveListener?) {
            if (messageReceiveListener != null) {
                remoteCallbackList.unregister(messageReceiveListener)
            }
        }
    }

    private var serviceManager: IServiceManager = object : IServiceManager.Stub() {

        override fun getService(serviceName: String?): IBinder? {
            if (IConnectionService::class.java.simpleName == serviceName) {
                return connectService.asBinder()
            } else if (IMessageService::class.java.simpleName == serviceName) {
                return messageService.asBinder()
            } else if (Messenger::class.java.simpleName == serviceName) {
                return messenger.binder
            }
            return null
        }
    }

    override fun onCreate() {
        super.onCreate()
        scheduledThreadPoolExecutor = ScheduledThreadPoolExecutor(1)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return serviceManager.asBinder()
    }
}