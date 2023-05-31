package com.example.a12312312312312312312

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream

class AddEventActivity : AppCompatActivity() {

    private val REQUEST_IMAGE_PICK = 1

    private lateinit var imageView: ImageView
    private lateinit var titleEditText: EditText
    private lateinit var authorEditText: EditText
    private lateinit var addButton: Button
    private lateinit var button: Button
    private var selectedImageBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)

        imageView = findViewById(R.id.imageView)
        titleEditText = findViewById(R.id.titleEditText)
        authorEditText = findViewById(R.id.authorEditText)
        addButton = findViewById(R.id.addButton1)
        button = findViewById(R.id.button)

        addButton.isEnabled = false

        titleEditText.addTextChangedListener(textWatcher)
        authorEditText.addTextChangedListener(textWatcher)

        addButton.setOnClickListener {
            addEvent()
        }

        button.setOnClickListener {
            openGallery()
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val title = titleEditText.text.toString().trim()
            val author = authorEditText.text.toString().trim()

            addButton.isEnabled = !title.isEmpty() && !author.isEmpty()
        }

        override fun afterTextChanged(s: Editable?) {}
    }

    private fun addEvent() {
        val title = titleEditText.text.toString()
        val author = authorEditText.text.toString()

        val resultIntent = Intent()
        resultIntent.putExtra("title", title)
        resultIntent.putExtra("author", author)
        val selectedImageByteArray = ByteArrayOutputStream()
        selectedImageBitmap?.compress(Bitmap.CompressFormat.PNG, 100, selectedImageByteArray)
        resultIntent.putExtra("image", selectedImageByteArray.toByteArray())
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            val selectedImageUri = data?.data
            selectedImageBitmap = BitmapFactory.decodeStream(selectedImageUri?.let {
                contentResolver.openInputStream(
                    it
                )
            })
            imageView.setImageBitmap(selectedImageBitmap)
        }
    }
}
