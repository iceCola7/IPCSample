package com.cxz.ipcsample

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cxz.ipcsample.entity.Message
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val TAG = "MainAIDL"

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: android.os.Message) {
            super.handleMessage(msg)
            val bundle = msg.data
            bundle.classLoader = Message::class.java.classLoader
            val message = bundle.getParcelable<Message>("message")
            Log.d(TAG, "client handleMessage = $message")
            postDelayed(Runnable {
                Toast.makeText(this@MainActivity, "${message?.content}", Toast.LENGTH_SHORT).show()
            }, 3000)
        }
    }

    private var connectionServiceProxy: IConnectionService? = null
    private var messageServiceProxy: IMessageService? = null
    private var serviceManagerProxy: IServiceManager? = null

    // 服务端Messenger
    private var messengerProxy: Messenger? = null

    // 客户端Messenger
    private val clientMessenger = Messenger(handler)

    private val serviceConnection = object : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "服务断开连接")
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "服务连接成功")
            // connectionServiceProxy = IConnectionService.Stub.asInterface(service)
            serviceManagerProxy = IServiceManager.Stub.asInterface(service)
            connectionServiceProxy = IConnectionService.Stub
                .asInterface(serviceManagerProxy?.getService(IConnectionService::class.java.simpleName))
            messageServiceProxy = IMessageService.Stub
                .asInterface(serviceManagerProxy?.getService(IMessageService::class.java.simpleName))
            messengerProxy =
                Messenger(serviceManagerProxy?.getService(Messenger::class.java.simpleName))
        }
    }

    private var messageReceiveListener: MessageReceiveListener =
        object : MessageReceiveListener.Stub() {
            override fun onReceiveMessage(message: Message?) {
                Log.d(TAG, "onReceiveMessage = $message")
                handler.post {
                    Toast.makeText(this@MainActivity, "${message?.content}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initListeners()

        testAIDL()

    }

    private fun testAIDL() {
        val intent = Intent(this, RemoteService::class.java)
        bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE)
    }

    private fun initListeners() {
        btn_connect.setOnClickListener {
            connectionServiceProxy?.connect()
        }
        btn_disconnect.setOnClickListener {
            connectionServiceProxy?.disconnect()
        }
        btn_is_connected.setOnClickListener {
            val bConnected = connectionServiceProxy?.isConnected
            Toast.makeText(this, "$bConnected", Toast.LENGTH_SHORT).show()
        }

        btn_send_message.setOnClickListener {
            val message = Message().apply {
                content = "message from main"
            }
            messageServiceProxy?.sendMessage(message)
            Log.d(TAG, "sendMessage = $message")
        }
        btn_register_message.setOnClickListener {
            messageServiceProxy?.registerMessageReceiveListener(messageReceiveListener)
        }
        btn_unregister_message.setOnClickListener {
            messageServiceProxy?.unRegisterMessageReceiveListener(messageReceiveListener)
        }

        btn_messenger.setOnClickListener {
            val message = Message().apply {
                content = "message from main by messenger"
            }
            val bundle = Bundle().apply {
                putParcelable("message", message)
            }
            val data = android.os.Message().apply {
                replyTo = clientMessenger
                data = bundle
            }
            messengerProxy?.send(data)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }
}