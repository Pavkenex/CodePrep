package com.codeprep.app.data.local

import androidx.room.TypeConverter
import com.codeprep.app.data.model.LessonContentBlock
import com.codeprep.app.data.model.LocalizedText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.Instant

class Converters {
    private val gson = Gson()

    // Za List<String> (keyPoints, commonMistakes)
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromLocalizedText(value: LocalizedText?): String {
        return gson.toJson(value ?: LocalizedText())
    }

    @TypeConverter
    fun toLocalizedText(value: String): LocalizedText {
        return gson.fromJson(value, LocalizedText::class.java) ?: LocalizedText()
    }

    @TypeConverter
    fun fromLocalizedTextList(value: List<LocalizedText>?): String {
        return gson.toJson(value ?: emptyList<LocalizedText>())
    }

    @TypeConverter
    fun toLocalizedTextList(value: String): List<LocalizedText> {
        val listType = object : TypeToken<List<LocalizedText>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromLessonContent(value: LessonContentBlock?): String {
        return gson.toJson(value ?: LessonContentBlock())
    }

    @TypeConverter
    fun toLessonContent(value: String): LessonContentBlock {
        return gson.fromJson(value, LessonContentBlock::class.java) ?: LessonContentBlock()
    }

    // Za List<CodeSnippet>
    @TypeConverter
    fun fromCodeSnippetList(value: List<CodeSnippet>?): String {
        return gson.toJson(value ?: emptyList<CodeSnippet>())
    }

    @TypeConverter
    fun toCodeSnippetList(value: String): List<CodeSnippet> {
        val listType = object : TypeToken<List<CodeSnippet>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    @TypeConverter
    fun dateToTimestamp(instant: Instant?): Long? {
        return instant?.toEpochMilli()
    }
}
