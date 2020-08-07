package com.utkarshkore.realnewsdaily.db

import androidx.room.TypeConverter
import com.utkarshkore.realnewsdaily.models.Source

/**
 * Created by Utkarsh Kore on 8/2/2020, Aug, 2020
 * UK Solutions Pvt. Ltd.
 * utkarshkore@gmail.com
 * 8693886401
 **/
class Converters {

    @TypeConverter
    fun fromSource(source: Source): String {
        return source.name
    }

    @TypeConverter
    fun toSource(name: String): Source{
        return Source(id = name, name = name)
    }
}