package com.example.myapplication

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.TypedValue
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.Toast
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextRecognizer
import com.google.android.material.floatingactionbutton.FloatingActionButton


class FloatingButtonService : Service() {

    private var mWindowManager: WindowManager? = null
    private var mFloatingButton: View? = null
    private var mPopupWindow: PopupWindow? = null
    private var isShowingImage = false
    private var isPopupShowing = false

    private fun rotate(view: View) {
        view.animate()
            .rotationBy(180f)
            .setDuration(500)
            .start()
    }



    @SuppressLint("InflateParams")

    override fun onCreate() {
        super.onCreate()

        // Infla el diseño del botón flotante
        mFloatingButton = LayoutInflater.from(this).inflate(R.layout.floating_button, null)

        // Crea un LayoutParams para el botón flotante
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        // Crea un MarginLayoutParams para los márgenes del botón flotante
        val marginParams = ViewGroup.MarginLayoutParams(params)
        marginParams.rightMargin = dpToPx(16)
        marginParams.bottomMargin = dpToPx(16)
        params.width = dpToPx(70)
        params.height = dpToPx(70)

        // Asigna los márgenes al botón flotante
        mFloatingButton?.layoutParams = marginParams

        // Agrega el botón flotante a la ventana
        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        mWindowManager?.addView(mFloatingButton, params)

        // Agrega un listener para el botón flotante
        mFloatingButton?.setOnTouchListener(object : View.OnTouchListener {

            private var x: Int = 0
            private var y: Int = 0
            private var initialX: Int = 0
            private var initialY: Int = 0
            private var isMoving = false

            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Obtén las coordenadas iniciales del botón
                        initialX = params.x
                        initialY = params.y

                        // Obtén la posición del dedo en la pantalla
                        x = event.rawX.toInt()
                        y = event.rawY.toInt()

                        isMoving = false
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        // Calcula la diferencia en la posición del dedo desde que se tocó la pantalla
                        val dx = event.rawX.toInt() - x
                        val dy = event.rawY.toInt() - y

                        // Actualiza las coordenadas del botón para moverlo
                        params.x = initialX + dx
                        params.y = initialY + dy

                        // Actualiza la posición del botón en la ventana
                        mWindowManager?.updateViewLayout(mFloatingButton, params)

                        isMoving = true
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isMoving) {
                            // Si el botón fue movido, consume el evento para evitar que se haga click en el botón
                            return true
                        } else {
                            // Si el botón no ha sido movido, muestra la ventana emergente
                            if (!isPopupShowing) {
                                mFloatingButton?.setBackgroundResource(R.drawable.ic_launcher_background)
                                rotate(mFloatingButton!!)
                                isPopupShowing = true
                                showPopup()
                            } else {
                                mPopupWindow?.dismiss()
                                mPopupWindow = null
                                isPopupShowing = false
                            }
                        }
                    }
                }
                return false
            }
        })

    }

    private fun showPopup() {
        // Define el contenido de la ventana emergente
        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_layout, null)
        val imageView = popupView.findViewById<ImageView>(R.id.image_view)
        val closeButton = popupView.findViewById<Button>(R.id.close_button)

        // Crea la ventana emergente
        mPopupWindow = PopupWindow(popupView, 500, 500)

        // Muestra la ventana emergente
        mPopupWindow?.showAtLocation(mFloatingButton, Gravity.CENTER, 0, 0)

        // Agrega un OnClickListener para el botón Close
        closeButton.setOnClickListener {
            // Cierra el PopupWindow
            mPopupWindow?.dismiss()
            mPopupWindow = null

            // Rota el botón flotante
            rotate(mFloatingButton!!)

            // Restaura la imagen del botón flotante
            mFloatingButton?.setBackgroundResource(R.drawable.opencamera)
            isShowingImage = false
        }

        // Agrega un OnClickListener para la imagen
        imageView.setOnClickListener {
            // Rota la imagen 180 grados
            rotate(imageView)

            // Agrega un Handler para revertir la rotación después de 2 segundos
            Handler().postDelayed({
                rotate(imageView)
            }, 2000)
        }

        mFloatingButton?.setOnClickListener {
            // Cierra el PopupWindow
            mPopupWindow?.dismiss()
            mPopupWindow = null

            // Rota el botón flotante
            rotate(mFloatingButton!!)

            // Restaura la imagen del botón flotante
            mFloatingButton?.setBackgroundResource(R.drawable.opencamera)
            isShowingImage = false
        }

        mPopupWindow?.setOnDismissListener {
            // Restaura la imagen del botón flotante
            mFloatingButton?.setBackgroundResource(R.drawable.opencamera)
            isShowingImage = false

            // Rota el botón flotante
            rotate(mFloatingButton!!)
        }
    }




    override fun onDestroy() {
        super.onDestroy()

        // Elimina el botón flotante de la ventana
        if (mFloatingButton != null) {
            mWindowManager?.removeView(mFloatingButton)
            mFloatingButton = null
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        // No se utiliza en este ejemplo
        return null
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics).toInt()
    }
}