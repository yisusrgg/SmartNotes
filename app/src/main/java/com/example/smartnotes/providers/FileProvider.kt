package com.example.smartnotes.providers

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.smartnotes.R
import java.io.File

class FileProvider : FileProvider(
    R.xml.file_paths,
) {
    companion object{
        fun getImageUri (ctx : Context): Uri {
            // 1. Define el subdirectorio "images" en el caché
            val dirIma = File(ctx.cacheDir, "images")
            dirIma.mkdirs() // Crea el directorio si no existe

            // 2. Crea un archivo temporal para la imagen (.jpg)
            val fileImage = File.createTempFile("img_",
                ".jpg", dirIma)

            // 3. Define la autoridad (debe coincidir con el AndroidManifest)
            val auth = ctx.packageName + ".fileprovider"

            // 4. Devuelve la URI segura
            return getUriForFile(ctx, auth, fileImage)
        }
    }
}