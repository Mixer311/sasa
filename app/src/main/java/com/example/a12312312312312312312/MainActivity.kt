package com.example.a12312312312312312312

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private val REQUEST_ADD_EVENT = 1

    private lateinit var addButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var eventAdapter: EventAdapter
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addButton = findViewById(R.id.addButton)
        recyclerView = findViewById(R.id.recyclerView)

        databaseHelper = DatabaseHelper(this)

        eventAdapter = EventAdapter()
        recyclerView.adapter = eventAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadEvents()

        addButton.setOnClickListener {
            openAddEventActivity()
        }
    }

    private fun openAddEventActivity() {
        val intent = Intent(this, AddEventActivity::class.java)
        startActivityForResult(intent, REQUEST_ADD_EVENT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ADD_EVENT && resultCode == Activity.RESULT_OK) {
            val title = data?.getStringExtra("title")
            val author = data?.getStringExtra("author")
            val imageByteArray = data?.getByteArrayExtra("image")

            val event = EventEntity(null, title, author, imageByteArray)
            saveEvent(event)

            loadEvents()
        }
    }

    private fun saveEvent(event: EventEntity) {
        val db = databaseHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_TITLE, event.title)
            put(DatabaseHelper.COLUMN_AUTHOR, event.author)
            put(DatabaseHelper.COLUMN_IMAGE, event.image)
        }
        db.insert(DatabaseHelper.TABLE_EVENTS, null, values)
        db.close()
    }

    @SuppressLint("Range")
    private fun loadEvents() {
        val db = databaseHelper.readableDatabase
        val cursor: Cursor = db.query(
            DatabaseHelper.TABLE_EVENTS,
            null,
            null,
            null,
            null,
            null,
            null
        )

        val eventList: MutableList<EventEntity> = mutableListOf()
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID))
            val title = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_TITLE))
            val author = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_AUTHOR))
            val image = cursor.getBlob(cursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE))

            val event = EventEntity(id, title, author, image)
            eventList.add(event)
        }

        cursor.close()
        db.close()

        eventAdapter.setData(eventList)
    }

    inner class EventAdapter : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {
        var eventList: MutableList<EventEntity> = mutableListOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_event, parent, false)
            return EventViewHolder(view)
        }

        override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
            val event = eventList[position]
            holder.bind(event)
            holder.deleteButton.setOnClickListener {
                deleteEvent(position)
            }
        }

        override fun getItemCount(): Int {
            return eventList.size
        }

        fun setData(events: List<EventEntity>) {
            eventList.clear()
            eventList.addAll(events)
            notifyDataSetChanged()
        }

        inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val backgroundImageView: ImageView =
                itemView.findViewById(R.id.backgroundImageView)
            private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
            private val authorTextView: TextView = itemView.findViewById(R.id.authorTextView)
            val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

            fun bind(event: EventEntity) {
                if (event.title.isNullOrEmpty()) {
                    titleTextView.visibility = View.GONE
                } else {
                    titleTextView.visibility = View.VISIBLE
                    titleTextView.text = event.title
                }

                if (event.author.isNullOrEmpty()) {
                    authorTextView.visibility = View.GONE
                } else {
                    authorTextView.visibility = View.VISIBLE
                    authorTextView.text = event.author.toString()
                }

                val imageBitmap = BitmapFactory.decodeByteArray(
                    event.image,
                    0,
                    event.image?.size ?: 0
                )
                backgroundImageView.setImageBitmap(imageBitmap)

                deleteButton.visibility = View.VISIBLE
            }
        }
    }
    data class EventEntity(
        val id: Long?,
        val title: String?,
        val author: String?,
        val image: ByteArray?
    )

    private fun deleteEvent(position: Int) {
        val event = eventAdapter.eventList[position]
        val db = databaseHelper.writableDatabase
        db.delete(
            DatabaseHelper.TABLE_EVENTS,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(event.id.toString())
        )
        db.close()

        loadEvents()
    }


}


