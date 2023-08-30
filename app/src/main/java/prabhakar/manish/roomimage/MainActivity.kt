package prabhakar.manish.roomimage

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var appDatabase: AppDatabase
    private lateinit var imageView :ImageView
    private lateinit var textView : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.textView)
        imageView = findViewById(R.id.imageView)

        // Initialize Room database instance
        appDatabase = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "image_database"
        ).build()

        // Choose image and insert into database
        chooseImageAndInsertIntoDatabase()
        loadImageAndDisplay()
    }

    private fun chooseImageAndInsertIntoDatabase() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }

    private fun loadImageAndDisplay() {
        lifecycleScope.launch {
            val imageEntity = withContext(Dispatchers.IO) {
                appDatabase.imageDao().getImageById(1)
            }

            if (imageEntity != null) {
                val imageBitmap = BitmapFactory.decodeByteArray(
                    imageEntity.imageBytes,
                    0,
                    imageEntity.imageBytes.size
                )

                withContext(Dispatchers.Main) {
                    imageView.setImageBitmap(imageBitmap)
                    textView.text = "Image ID: ${imageEntity.id}"
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data
            if (selectedImageUri != null) {
                val imageBitmap = loadBitmapFromUri(selectedImageUri)
                if (imageBitmap != null) {
                    val imageByteArray = convertBitmapToByteArray(imageBitmap)
                    insertImageIntoDatabase(imageByteArray)
                }
            }
        }
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        return outputStream.toByteArray()
    }

    private fun insertImageIntoDatabase(imageByteArray: ByteArray) {
        CoroutineScope(Dispatchers.IO).launch {
            val imageEntity = ImageEntity(imageBytes = imageByteArray)
            appDatabase.imageDao().insertImage(imageEntity)
        }
    }

    companion object {
        private const val PICK_IMAGE_REQUEST_CODE = 123
    }
}
