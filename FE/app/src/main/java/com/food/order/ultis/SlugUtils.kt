package com.food.order.ultis

import java.text.Normalizer
import java.util.Locale

object SlugUtils {
    @JvmStatic
    fun toSlug(input: String): String {
        val norm = Normalizer.normalize(input.lowercase(Locale.getDefault()), Normalizer.Form.NFD)
        val noMarks = norm.replace("\\p{M}+".toRegex(), "")
        return noMarks
            .replace("[^a-z0-9]+".toRegex(), "-")
            .trim('-')
            .replace("-+".toRegex(), "-")
    }

    @JvmStatic
    fun stem(fileName: String): String = fileName.substringBeforeLast('.')
}