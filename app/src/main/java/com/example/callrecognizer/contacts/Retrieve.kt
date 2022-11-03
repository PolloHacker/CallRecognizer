package com.example.callrecognizer.contacts

import android.content.Context
import com.example.callrecognizer.getNamePhoneDetails

fun getData(context : Context) : MutableList<Contact> {
    val files: Array<String> = context.fileList()

    val results = mutableListOf<Contact>()

    if(files.isEmpty()) {
        context.openFileOutput("contacts.txt", Context.MODE_PRIVATE).use { output ->
            val list = getNamePhoneDetails(context)
            list.distinctBy { it.number }.forEach {
                output.write(serialize(it).toByteArray())
                output.write("\n".toByteArray())
            }
        }
    }
    context.openFileInput("contacts.txt").bufferedReader().forEachLine { line ->
        results.add(deserialize(line))
    }
    return results
}