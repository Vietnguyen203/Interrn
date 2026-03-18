package com.food.order.data.response

import com.food.order.data.model.UserModel
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class UserResponse(
    @SerializedName("uid") val uid: String?,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("role") val role: String?,
    @SerializedName("server") val server: String?,
    @SerializedName("birthday") val birthday: String?,
    @SerializedName("gender") val gender: String?,
    @SerializedName("createdDate") val createdDate: String?,
    @SerializedName("created_by") val createdBy: String?
) : Serializable {
    fun toUserModel(): UserModel {
        return UserModel(
            employeeId = uid ?: "UNKNOWN_ID",
            displayName = fullName ?: "No Name",
            role = role ?: "USER",
            server = server ?: "HCM",
            birthday = birthday,
            email = email,
            phoneNumber = phoneNumber,
            gender = gender,
            createdAt = createdDate,
            createdBy = createdBy
        )
    }
}
