package com.example.callrecognizer.contacts

import com.google.gson.Gson

data class Contact(
    val id : String ,
    val name : String,
    val number : String
    ) {

    override fun toString(): String {
        return "Subject(id=$id, name=$name, number=$number)"
    }
}

fun serialize(subject: Contact) : String {
    val gson = Gson()

    return gson.toJson(subject)
}

fun deserialize(subject: String) : Contact {
    val gson = Gson()

    return gson.fromJson(subject, Contact::class.java)
}