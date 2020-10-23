package com.example.dbacriminal



import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Crime(@PrimaryKey val id: UUID = UUID.randomUUID(),
                 var title: String = "",
                 var date: Date = Date(),
                 var isSolved: Boolean = false )
//changes due to doing challenge no 11
{
    override fun equals(other : Any?): Boolean
    {
        if(javaClass != other?.javaClass)
        {
            return false
        }

        other as Crime
        if(id == other.id)
        {
            return false
        }
        if(title == other.title)
        {
            return false
        }
        if(date == other.date)
        {
            return false
        }
        if(isSolved == other.isSolved) {
            return false
        }
        return true
    }

}

