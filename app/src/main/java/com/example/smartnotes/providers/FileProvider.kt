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

            //Crea un archivo temporal para la imagen (.jpg)
            val fileImage = File.createTempFile("img_",
                ".jpg", dirIma)

            //Define la autoridad (debe coincidir con el AndroidManifest)
            val auth = ctx.packageName + ".fileprovider"

            //Devuelve la URI segura
            return getUriForFile(ctx, auth, fileImage)
        }

        fun getAudioUri (ctx : Context): File {
            // Define el subdirectorio "audios" en el caché
            val dirAudio = File(ctx.cacheDir, "audios")
            dirAudio.mkdirs()

            // Crea un archivo temporal para el audio (.m4a común y ligero)
            val fileAudio = File.createTempFile("audio_", ".m4a", dirAudio)

            // Devolvemos el objeto File. La conversión a URI segura (content://)
            // se hará DENTRO del ViewModel, justo después de detener la grabación.
            return fileAudio
        }
    }
}