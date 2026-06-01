package com.example.movieparadiso.ui

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.movieparadiso.databinding.ActivityQrCodeBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

class QrCodeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrCodeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityQrCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FullscreenHelper.enableFullscreen(this)

        val qrText = intent.getStringExtra("qr_text")

        if (qrText.isNullOrBlank()) {
            Toast.makeText(this, "No QR data found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.tvQrInfo.text = qrText
        binding.ivQrCode.setImageBitmap(generateQrBitmap(qrText))

        binding.btnCloseQr.setOnClickListener {
            finish()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            FullscreenHelper.enableFullscreen(this)
        }
    }

    private fun generateQrBitmap(content: String): Bitmap {
        val size = 768
        val bitMatrix = QRCodeWriter().encode(
            content,
            BarcodeFormat.QR_CODE,
            size,
            size
        )

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)

        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(
                    x,
                    y,
                    if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                )
            }
        }

        return bitmap
    }
}