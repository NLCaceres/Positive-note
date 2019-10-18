package itp341.caceres.nicholas.positive_note.app.modelClasses

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

// Booleans starting with "is" will be have getter "isBlue()" and setter "setBlue"
// Two fixes: set as JvmField (which could lose some kotlin bonuses)
// or change name in Firestore to "blue" which aligns Java naming convention (but it can be either isBlue or blue down here)
@Parcelize
data class UserInfo(val fullName: String = "", // First Name and Surname
                    var userName: String = "", // ScreenName
                    var email: String = "",
                    var biography: String = "",
                    var helper: Boolean = false,
                    var private: Boolean = false,
                    var verified: Boolean = false,
                    val currentFeelings: UserFeelings? = null,
                    val currentLocation: UserLocation? = null) : Parcelable

@Parcelize
data class UserFeelings(var blue: Boolean = false,
                        var emotions: Boolean = false,
                        var looks: Boolean = false,
                        var relationship: Boolean = false,
                        var self: Boolean = false) : Parcelable

@Parcelize
data class UserLocation(var userCity: String? = null, // getLocality can return null for userCity
                        var userHash: String = "",
                        var userLatitude: Double = 0.0,
                        var userLongitude: Double = 0.0) : Parcelable

@Parcelize
data class UserMessage(val user: String = "", val message: String = "", val timeStamp: Date? = null, var favorited : Boolean = false) : Parcelable
// TODO: UserChatMessages! or simply add something related to whether it's a positive note or not
// Maybe all messages can be positive notes and therefore all have a boolean tied to if they are. Helps RView and messages in homescreen

@Parcelize
data class UserChatItem(val user: UserInfo? = null, val userRecentMessage: UserMessage? = null) : Parcelable