package com.example.todo_app

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.SparseBooleanArray
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.isNotEmpty
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

var dataNumber=0

class MainActivity : AppCompatActivity() {
    //private StorageReference mStorageRef
    //private FirebaseStorage storage =FirebaseStorage.getInstance()



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val eventList = arrayListOf<String>()
        val listAdapter =
                ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, eventList)
        val listView: ListView = findViewById(R.id.listView)
        val addBtn: Button = findViewById(resources.getIdentifier("add_btn", "id", packageName))
        val deleteBtn: Button =
                findViewById(resources.getIdentifier("delete_btn", "id", packageName))
        val editBtn: Button = findViewById(resources.getIdentifier("edit_btn", "id", packageName))
        val addInput: LinearLayout =
                findViewById(resources.getIdentifier("text_add", "id", packageName))
        val imgBtn: ImageButton =
                findViewById(resources.getIdentifier("imageButton", "id", packageName))
        val inputText: EditText = findViewById(R.id.textInput)
        lateinit var position: SparseBooleanArray
        var editActive = false
        var pos2 = 0
        val dates = arrayListOf<String>()
        val dateTime = LocalDateTime.now()

        //read from Firebase database
        readDatabase(eventList,listAdapter,listView,editBtn)

        addBtn.setOnClickListener {
            if (addInput.visibility == View.VISIBLE) {
                addInput.visibility = View.GONE
            } else {
                addInput.visibility = View.VISIBLE
            }

        }

        deleteBtn.setOnClickListener {
            position = listView.checkedItemPositions
            if (position.isNotEmpty()) {
                val count = listView.count
                var item = count - 1
                if (count > 0) {
                    while (item >= 0) {
                        if (position.get(item)) {
                            dates.add(dateTime.format(DateTimeFormatter.ofPattern("M/d/y H:m:ss")) + ", deleted, " + eventList[item])
                            listAdapter.remove(eventList[item])
                        }
                        item--
                    }

                    //delete record from firebase database
                    deleteFromDatabase(position)

                    position.clear()
                    listAdapter.notifyDataSetChanged()
                    inputText.text.clear()
                    val line = dates[dates.size - 1]
                    Toast.makeText(
                            applicationContext,
                            line,
                            Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        editBtn.setOnClickListener {
            if(editActive){
                editActive=false
                editBtn.setBackgroundColor(Color.CYAN)
            }

            else if (listView.count > 0) {
                if (addInput.visibility == View.GONE) {
                    addInput.visibility = View.VISIBLE
                }
                position = listView.checkedItemPositions
                editBtn.setBackgroundColor(Color.MAGENTA)
                listView.choiceMode = ListView.CHOICE_MODE_SINGLE
                position.clear()
                editActive = true
            }
        }

        listView.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            if (editActive) {
                inputText.setText(eventList[position])
                pos2 = position
            }
        }



        imgBtn.setOnClickListener {
            if (editActive) {
                listView.choiceMode = ListView.CHOICE_MODE_SINGLE
                eventList.set(pos2,inputText.text.toString())
                editDatabase(pos2,inputText.text.toString())
                dates.add(dateTime.format(DateTimeFormatter.ofPattern("M/d/y H:m:ss")) + ", edited, " + inputText.text.toString())
                listView.adapter = listAdapter
                listAdapter.notifyDataSetChanged()
                inputText.text.clear()
                listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
                editActive = false
                editBtn.setBackgroundColor(Color.CYAN)
                val line = dates[dates.size - 1]
                Toast.makeText(
                        applicationContext,
                        line,
                        Toast.LENGTH_SHORT
                ).show()
            } else {
                //uploadToFirebase(inputText.text.toString())
                eventList.add(inputText.text.toString())
                uploadToDatabase(inputText.text.toString())

                dates.add(dateTime.format(DateTimeFormatter.ofPattern("M/d/y H:m:ss")) + ", added, " + inputText.text.toString())
                listView.adapter = listAdapter
                listAdapter.notifyDataSetChanged()
                inputText.text.clear()
                listAdapter.notifyDataSetChanged()
                editBtn.setBackgroundColor(Color.CYAN)
                val line = dates[dates.size - 1]
                Toast.makeText(
                        applicationContext,
                        line,
                        Toast.LENGTH_SHORT
                ).show()
            }
        }

    }



    private fun uploadToDatabase(note: String) {
//        dataNumber++
//        Log.d("datalog", dataNumber.toString())
        var database = FirebaseDatabase.getInstance()
        var myRef = database.reference
        var key:String=myRef.push().key.toString()
         database = FirebaseDatabase.getInstance()
         myRef = database.getReference().child(key)
        Log.d("data:  ", myRef.toString())
        myRef.push().setValue(note)

        myRef.setValue(note)
        Log.d("key   ",key)
        notesList.add(key)
    }


    var notesList = ArrayList<String>()

    private fun readDatabase(eventList:java.util.ArrayList<String>,listAdapter:ArrayAdapter<String>,listView:ListView,editBtn:Button) {


        eventList.clear()
        listAdapter.notifyDataSetChanged()
        val myRef = FirebaseDatabase.getInstance().reference


        myRef.get().addOnSuccessListener {
            Log.i("firebase", "Got value ${it.value}")


            for (dsp in it.getChildren()) {
                val notekey = dsp.key
                notesList.add(notekey!!) //add result into array list
            }

            Log.i("userlist:  :   ", notesList.toString())


            for (i in notesList) {
                var note: String = ""
                myRef.child(i).get().addOnSuccessListener() {
                    note = it.value.toString()
                    Log.i("got what?:  :   ", note)
                    eventList.add(note)
                    listView.adapter = listAdapter
                    listAdapter.notifyDataSetChanged()

                    listAdapter.notifyDataSetChanged()
                    editBtn.setBackgroundColor(Color.CYAN)
                }

            }

        }.addOnFailureListener {
            Log.e("firebase", "Got no data", it)
        }
    }

    private fun deleteFromDatabase(position: SparseBooleanArray){
        var toBeRemoved = ArrayList<String>()
        val myRef = FirebaseDatabase.getInstance().reference
        Log.d("booleanarray",position.toString())
        for (i in notesList) {
//            notesList.indexOf(i)
            if (position[notesList.indexOf(i)]) {
                Log.i("I ===?:  :   ", notesList.indexOf(i).toString())
                myRef.child(i).removeValue()
                toBeRemoved.add(i)

            }
        }
        for (i in toBeRemoved){
            notesList.remove(i)
        }
    }

    private fun editDatabase(position:Int, newNote:String){
        val myRef = FirebaseDatabase.getInstance().reference
        for (i in notesList) {
//            notesList.indexOf(i)
            if (position==notesList.indexOf(i)) {
                Log.i("I ===?:  :   ", notesList.indexOf(i).toString())
                myRef.child(i).setValue(newNote)


            }
        }

    }


}



