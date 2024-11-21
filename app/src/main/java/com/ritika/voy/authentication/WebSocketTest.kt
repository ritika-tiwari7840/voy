package com.ritika.voy.authentication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ritika.voy.R
import com.ritika.voy.api.WebSocketClient
import kotlinx.coroutines.launch

class WebSocketTest : Fragment() {

    private lateinit var webSocketClient: WebSocketClient
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var connectButton: Button
    private lateinit var reconnectButton: Button
    private lateinit var disconnectButton: Button
    private lateinit var receivedMessages: TextView
    private var URL = "ws://192.168.56.228:8765"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_web_socket_test, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        messageInput = view.findViewById(R.id.messageInput)
        sendButton = view.findViewById(R.id.sendButton)
        connectButton = view.findViewById(R.id.connectButton)
        reconnectButton = view.findViewById(R.id.reconnectButton)
        disconnectButton = view.findViewById(R.id.disconnectButton)
        receivedMessages = view.findViewById(R.id.receivedMessages)

        connectButton.setOnClickListener {
            webSocketClient = WebSocketClient(URL)
            observeMessages()
        }

        reconnectButton.setOnClickListener {
            webSocketClient.close(1000, "Reconnecting")
            webSocketClient = WebSocketClient(URL)
            observeMessages()
        }

        disconnectButton.setOnClickListener {
            webSocketClient.close(1000, "Disconnecting")
        }

        sendButton.setOnClickListener {
            val message = messageInput.text.toString()
            webSocketClient.sendMessage(message)
        }
    }

    private fun observeMessages() {
        lifecycleScope.launch {
            for (message in webSocketClient.messageChannel) {
                receivedMessages.append("\n$message")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocketClient.close(1000, "Fragment destroyed")
    }
}