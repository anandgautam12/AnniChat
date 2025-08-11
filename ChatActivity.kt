package com.anand.annichat

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.anand.annichat.utils.SessionManager // ✅ ADD THIS LINE
import org.json.JSONArray
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import com.anand.annichat.models.ChatMessage

class ChatActivity : AppCompatActivity() {

    private lateinit var messageListView: ListView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var imageButton: ImageButton

    private lateinit var receiverId: String
    private lateinit var senderId: String

    private val messages = ArrayList<String>()
    private lateinit var adapter: ArrayAdapter<String>

    private val IMAGE_PICK_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        messageListView = findViewById(R.id.messageListView)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)
        imageButton = findViewById(R.id.imageButton)

        val sessionManager = SessionManager(this) // ✅ Create session manager
        senderId = sessionManager.getUserId() ?: "" // ✅ Get sender id
        receiverId = intent.getStringExtra("receiver_id") ?: ""

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, messages)
        messageListView.adapter = adapter

        sendButton.setOnClickListener {
            val message = messageEditText.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                messageEditText.text.clear()
            }
        }

        imageButton.setOnClickListener {
            pickImageFromGallery()
        }

        loadMessages()
    }

    private fun sendMessage(message: String) {
        Thread {
            val url = URL("http://10.22.64.51/annichatapi/send_message.php")
            val postData = "sender_id=$senderId&receiver_id=$receiverId&message=$message&type=text"
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.outputStream.write(postData.toByteArray())
            conn.inputStream.bufferedReader().readText()

            runOnUiThread {
                messages.add("You: $message")
                adapter.notifyDataSetChanged()
            }
        }.start()
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data
            imageUri?.let {
                sendImage(it)
            }
        }
    }

    private fun sendImage(imageUri: Uri) {
        Thread {
            val url = URL("http://10.22.64.51/annichatapi/send_image.php")
            val conn = url.openConnection() as HttpURLConnection
            conn.doOutput = true
            conn.requestMethod = "POST"
            val boundary = "" + System.currentTimeMillis() + ""
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=$boundary")

            val output = DataOutputStream(conn.outputStream)
            val filePath = getRealPathFromURI(imageUri)
            val file = File(filePath)
            val fileName = file.name

            output.writeBytes("--$boundary\r\n")
            output.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"$fileName\"\r\n\r\n")

            val fileInput = FileInputStream(file)
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (fileInput.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead)
            }

            output.writeBytes("\r\n--$boundary--\r\n")
            output.flush()
            fileInput.close()
            output.close()

            conn.inputStream.bufferedReader().readText()

            runOnUiThread {
                messages.add("You sent an image 📷")
                adapter.notifyDataSetChanged()
            }
        }.start()
    }

    private fun getRealPathFromURI(contentUri: Uri): String {
        var result = ""
        val cursor = contentResolver.query(contentUri, null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            result = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
            cursor.close()
        }
        return result
    }

    private fun loadMessages() {
        Thread {
            val url = URL("http://10.22.64.51/annichatapi/get_messages.php?sender_id=$senderId&receiver_id=$receiverId")
            val response = url.readText()
            val jsonArray = JSONArray(response)

            messages.clear()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val from = obj.getString("sender_id")
                val type = obj.getString("type")
                val msg = obj.getString("message")

                if (type == "image") {
                    messages.add(if (from == senderId) "You sent image 📷" else "They sent image 📷")
                } else {
                    messages.add(if (from == senderId) "You: $msg" else "Them: $msg")
                }
            }

            runOnUiThread {
                adapter.notifyDataSetChanged()
            }
        }.start()
    }
}