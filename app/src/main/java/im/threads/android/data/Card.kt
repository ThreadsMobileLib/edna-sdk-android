package im.threads.android.data

import android.os.Parcelable
import androidx.core.util.ObjectsCompat
import kotlinx.parcelize.Parcelize

@Parcelize
data class Card(
    val userId: String,
    val clientData: String,
    val appMarker: String,
    val clientIdSignature: String,
    val authToken: String,
    val authSchema: String
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val card = other as Card
        return ObjectsCompat.equals(userId, card.userId)
    }

    override fun hashCode(): Int {
        return userId.hashCode()
    }
}
