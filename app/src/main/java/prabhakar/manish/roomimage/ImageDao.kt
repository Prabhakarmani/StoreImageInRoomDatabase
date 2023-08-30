package prabhakar.manish.roomimage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ImageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertImage(imageEntity: ImageEntity)

    @Query("SELECT * FROM images WHERE id = :id")
    fun getImageById(id: Long): ImageEntity?
}
