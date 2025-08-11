package com.anand.annichat
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.*
import java.util.*


class ProfileActivity : AppCompatActivity() {

    private lateinit var imageViewProfile: ImageView
    private lateinit var editTextName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextDob: EditText
    private lateinit var editTextAge: EditText
    private lateinit var buttonSave: Button
    private lateinit var buttonSelectImage: Button

    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1
    private val userId = "1" // Replace with actual user ID if needed

    private val baseUrl = "http://10.22.64.51/annichatapi/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        imageViewProfile = findViewById(R.id.imageViewProfile)
        editTextName = findViewById(R.id.editTextName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextDob = findViewById(R.id.editTextDob)
        editTextAge = findViewById(R.id.editTextAge)
        buttonSave = findViewById(R.id.buttonSave)
        buttonSelectImage = findViewById(R.id.buttonSelectImage)

        buttonSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        buttonSave.setOnClickListener {
            saveProfile()
        }

        loadProfile()
    }

    private fun loadProfile() {
        val client = OkHttpClient()
        val formBody = FormBody.Builder()
            .add("user_id", userId)
            .build()

        val request = Request.Builder()
            .url("${baseUrl}get_profile.php")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@ProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val res = response.body?.string()
                if (res != null) {
                    val json = JSONObject(res)
                    if (json.getBoolean("success")) {
                        val profile = json.getJSONObject("profile")
                        runOnUiThread {
                            editTextName.setText(profile.getString("name"))
                            editTextEmail.setText(profile.getString("email"))
                            editTextDob.setText(profile.getString("dob"))
                            editTextAge.setText(profile.getString("age"))
                            val imageUrl = profile.getString("profile_image")
                            if (imageUrl.isNotEmpty()) {
                                Thread {
                                    try {
                                        val input = java.net.URL(imageUrl).openStream()
                                        val bitmap = BitmapFactory.decodeStream(input)
                                        runOnUiThread {
                                            imageViewProfile.setImageBitmap(bitmap)
                                        }
                                    } catch (_: Exception) {}
                                }.start()
                            }
                        }
                    }
                }
            }
        })
    }

    private fun saveProfile() {
        val name = editTextName.text.toString()
        val email = editTextEmail.text.toString()
        val dob = editTextDob.text.toString()
        val age = editTextAge.text.toString()

        val client = OkHttpClient()
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        builder.addFormDataPart("user_id", userId)
        builder.addFormDataPart("name", name)
        builder.addFormDataPart("email", email)
        builder.addFormDataPart("dob", dob)
        builder.addFormDataPart("age", age)

        selectedImageUri?.let {
            val file = File(getPathFromUri(it))
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            builder.addFormDataPart("profile_image", file.name, requestFile)
        }

        val requestBody = builder.build()
        val request = Request.Builder()
            .url("${baseUrl}save_profile.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@ProfileActivity, "Failed to save profile", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val res = response.body?.string()
                val json = JSONObject(res)
                val success = json.getBoolean("success")
                val message = json.getString("message")
                runOnUiThread {
                    Toast.makeText(this@ProfileActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun getPathFromUri(uri: Uri): String {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        val path = columnIndex?.let { cursor.getString(it) }
        cursor?.close()
        return path ?: ""
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            imageViewProfile.setImageURI(selectedImageUri)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}