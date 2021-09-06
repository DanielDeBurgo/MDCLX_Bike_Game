package bikeproject.uni.bikeapp

import android.app.Activity
import android.content.Intent
import kotlin.math.floor


inline infix fun <reified T> T.dot(proc: T.() -> Unit) : T { proc(); return this }

inline fun <reified T : Activity> startActivity() = Net.currentActivity!!.startActivity(Intent(Net.currentActivity!!, T::class.java))

inline fun <reified T : Activity, reified R : Activity> T.startActivityForResult(code: Int)
{
    val i = Intent(this, R::class.java)
    startActivityForResult(i, code)
}

fun toMuguColor(colorInt: Int): String
{
    //@Note: and-ing by 0xff (255) it will mask off all the bits except the lower 8 bits
    val R = (colorInt shr 16 and 0xff) / 255f
    val G = (colorInt shr 8 and 0xff) / 255f
    val B = (colorInt and 0xff) / 255f
    return "(R=$R, G=$G, B=$B, A=1)"
}

//"(R=1,G=1,B=1,A=1)"
fun parseColor(colorString: String): Int
{
    val colorStringSplit = colorString.split("=")

    val alpha = 0xff shl 24
    val red   = floor(colorStringSplit[1].split(",")[0].toDouble() * 255.0).toInt() shl 16
    val green = floor(colorStringSplit[2].split(",")[0].toDouble() * 255.0).toInt() shl 8
    val blue  = floor(colorStringSplit[3].split(",")[0].toDouble() * 255.0).toInt()

    return alpha + red + green + blue
}

inline fun <reified T : Activity> T.scope(crossinline proc: T.() -> Unit) = proc()
