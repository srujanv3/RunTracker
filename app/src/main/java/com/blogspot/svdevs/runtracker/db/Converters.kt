package com.blogspot.svdevs.runtracker.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class Converters {

    @TypeConverter
    fun fromBitmap(bmp: Bitmap): ByteArray {
        //convert bitmap to byte array
        val outputStream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG,100,outputStream)
        return outputStream.toByteArray()
    }

    @TypeConverter
    fun toBitmap(bytes: ByteArray): Bitmap {
        //convert byte array to bitmap
        return BitmapFactory.decodeByteArray(bytes,0,bytes.size)
    }
}