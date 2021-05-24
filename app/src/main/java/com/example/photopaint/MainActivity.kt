package com.example.photopaint

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.TextureView
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.example.photopaint.Constants.REQUEST_CAMERA
import com.example.photopaint.paintPhoto.paintPhoto
import com.example.photopaint.paintPhoto.paintPhoto2
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    private lateinit var paintFragment: paintPhoto
    private lateinit var cameraSession: CameraSession
    private lateinit var btnTakePhoto: Button
    private lateinit var textureView: TextureView
    private lateinit var constraint : ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textureView = findViewById(R.id.camera_prev)
        btnTakePhoto = findViewById(R.id.btn_take_photo)
        constraint = findViewById(R.id.layout)
        paintFragment = paintPhoto()


        cameraSession = CameraSession(this, textureView)


        btnTakePhoto.setOnClickListener{
             cameraSession.takePhoto()

        }

       /* val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        NavigationUI.setupWithNavController(bottomNavigationView, Navigation.findNavController(this, R.id.nav_host_fragment_container))
*/
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == REQUEST_CAMERA){

            if(grantResults[0] == PackageManager.PERMISSION_DENIED){

                Toast.makeText(this, "Нет разрешения к камере", Toast.LENGTH_LONG).show()
                finish()

            }

        }
    }

    fun changeFragment(v: View, cls: paintPhoto2)
    {
        supportFragmentManager.beginTransaction().replace(v.id, cls).commit()

    }

    override fun onPause() {
        super.onPause()
        cameraSession.sessionPause()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onResume() {
        super.onResume()
        cameraSession.sessionResume()
    }

}