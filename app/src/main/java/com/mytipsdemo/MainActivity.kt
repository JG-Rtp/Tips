package com.mytipsdemo

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.mytipsdemo.databinding.ActivityMainBinding
import io.github.kawis.tips.TipsDialogBuilder
import io.github.kawis.tips.TipsDialogPosition
import io.github.kawis.tips.TipsToast
import io.github.kawis.tips.TipsToastBuilder
import io.github.kawis.tips.TipsToastPosition

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false

        binding.top.setOnClickListener {
            TipsDialogBuilder(this)
                .setTitle("位置")
                .setMessage("TOP / 顶部")
                .setPosition(TipsDialogPosition.TOP)
                .setIconVisible(true)
                .setNegativeButton("NO") {
                    // TODO: NO
                }
                .setPositiveButton("YES") {
                    // TODO: YES
                }
                .show()
        }

        binding.CENTER.setOnClickListener {
            TipsDialogBuilder(this)
                .setTitle("位置")
                .setMessage("CENTER / 中间")
                .setPosition(TipsDialogPosition.CENTER)
                .setIconVisible(true)
                .setNegativeButton("NO") {
                    // TODO: NO
                }
                .setPositiveButton("YES") {
                    // TODO: YES
                }
                .show()
        }

        binding.BOTTOM.setOnClickListener {
            TipsDialogBuilder(this)
                .setTitle("位置")
                .setMessage("BOTTOM / 底部")
                .setPosition(TipsDialogPosition.BOTTOM)
                .setNegativeButton("NO") {
                    // TODO: NO
                }
                .setPositiveButton("YES") {
                    // TODO: YES
                }
                .show()
        }

        binding.toastBottom.setOnClickListener {
            TipsToastBuilder(this)
                .setView(R.layout.taost)
                .setPosition(TipsToastPosition.BOTTOM)
                .setBottomOffsetDp(60f)
                .setStackEnabled(true)
                .setDuration(2000L)
                .show()
        }

        binding.toastCenter.setOnClickListener {
            TipsToastBuilder(this)
                .setTitle("TipsToast")
                .setMessage("This is a TipsToastBuilder demo")
                .setPosition(TipsToastPosition.CENTER)
                .setStackEnabled(true)
                .setDuration(2000L)
                .show()
        }

        binding.toastTop.setOnClickListener {
            TipsToastBuilder(this)
                .setTitle("TipsToast")
                .setMessage("This is a TOP TipsToast demo")
                .setPosition(TipsToastPosition.TOP)
                .setStackEnabled(true)
                .setDuration(2000L)
                .show()
        }
    }
}