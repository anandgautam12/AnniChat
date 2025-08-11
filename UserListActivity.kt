package com.anand.annichat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anand.annichat.adapter.UserAdapter
import com.anand.annichat.model.User
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class UserListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private val userList = ArrayList<User>()

    private val baseUrl = "http://10.22.64.51/annichatapi/" // ✅ Your backend IP here

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        // Set custom title
        val appTitle = findViewById<TextView>(R.id.appTitle)
        appTitle.text = "Anni Chatting Zone"

       recyclerView = findViewById(R.id.recyclerViewUsers)
        recyclerView.layoutManager = LinearLayoutManager(this)
        userAdapter = UserAdapter(userList) { selectedUser ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("receiver_id", selectedUser.id)
            intent.putExtra("receiver_name", selectedUser.name)
            startActivity(intent)
        }
        recyclerView.adapter = userAdapter

        fetchUsers()
    }

    private fun fetchUsers() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("${baseUrl}get_users.php")  // ✅ PHP file to return users
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@UserListActivity, "Failed to load users", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { jsonString ->
                    try {
                        val jsonArray = JSONArray(jsonString)
                        userList.clear()

                        for (i in 0 until jsonArray.length()) {
                            val userObj = jsonArray.getJSONObject(i)
                            val user = User(
                                id = userObj.getString("id"),
                                name = userObj.getString("name")
                            )
                            userList.add(user)
                        }

                        runOnUiThread {
                            userAdapter.notifyDataSetChanged()
                        }

                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this@UserListActivity, "Error parsing users", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }
}