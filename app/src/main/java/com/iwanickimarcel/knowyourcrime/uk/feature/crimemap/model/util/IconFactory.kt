package com.iwanickimarcel.knowyourcrime.uk.feature.crimemap.model.util

import com.iwanickimarcel.knowyourcrime.R

object IconFactory {
    fun getIconIndexBasedOnCategoryName(category: String): Int {
        var index = 0
        when (category) {
            "anti-social-behaviour" -> index = R.drawable.shout2
            "bicycle-theft" -> index = R.drawable.bicycle
            "burglary" -> index = R.drawable.burglar2
            "criminal-damage-arson" -> index = R.drawable.fire
            "drugs" -> index = R.drawable.meds
            "other-theft" -> index = R.drawable.thief2
            "possession-of-weapons" -> index = R.drawable.gun
            "public-order" -> index = R.drawable.vandalism
            "robbery" -> index = R.drawable.robbery
            "shoplifting" -> index = R.drawable.shoppingcart
            "theft-from-the-person" -> index = R.drawable.onlinerobbery
            "vehicle-crime" -> index = R.drawable.car
            "violent-crime" -> index = R.drawable.violentcrime
            "other-crime" -> index = R.drawable.other
        }
        return index
    }
}