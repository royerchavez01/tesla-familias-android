package pe.tesla.familias

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

// Estas pruebas no cambian la información del estudiante.
class PruebasDispositivoView(context: Context) : LinearLayout(context), SensorEventListener {
    private val sensorManager = context.getSystemService(SensorManager::class.java)
    private val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
    private val lectura = TextView(context)
    private val escala = resources.displayMetrics.density

    init {
        orientation = VERTICAL
        addView(TextView(context).apply {
            text = "Este apartado permite comprobar componentes del teléfono sin modificar los datos de la demostración."
            textSize = 16f
            setPadding(0, 12, 0, 24)
        })
        addView(Button(context).apply {
            text = "Probar sonido"
            isAllCaps = false
            setOnClickListener {
                try {
                    val tono = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 60)
                    tono.startTone(ToneGenerator.TONE_PROP_ACK, 180)
                    Handler(Looper.getMainLooper()).postDelayed({ tono.release() }, 250)
                    Toast.makeText(context, "Sonido de prueba ejecutado", Toast.LENGTH_SHORT).show()
                } catch (_: RuntimeException) {
                    lectura.text = "No se pudo iniciar el sonido de prueba."
                }
            }
        }, LayoutParams(-1, (56 * escala).toInt()))
        addView(TextView(context).apply {
            text = "El tono depende del volumen de notificaciones.\n\nLienzo de prueba: toca o arrastra para mover el punto."
            textSize = 16f
            setPadding(0, 20, 0, 20)
        })
        addView(LienzoPrueba(context), LayoutParams(-1, (160 * escala).toInt()))
        lectura.text = if (sensor == null) "Sensor de proximidad no disponible en este dispositivo." else "Sensor de proximidad disponible. Acerca y aleja la mano para observar el valor."
        lectura.textSize = 16f
        lectura.setPadding(0, 24, 0, 20)
        addView(lectura)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        sensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
    }

    override fun onDetachedFromWindow() {
        sensorManager.unregisterListener(this)
        super.onDetachedFromWindow()
    }

    override fun onSensorChanged(event: SensorEvent) {
        lectura.text = "Sensor de proximidad\nValor recibido: ${event.values[0]}\nAcerca y aleja la mano para comprobar el cambio."
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }
}

class LienzoPrueba(context: Context) : View(context) {
    private val pincel = Paint(Paint.ANTI_ALIAS_FLAG)
    private var puntoX = -1f
    private var puntoY = -1f

    init {
        contentDescription = "Lienzo táctil de prueba. Toca para mover el punto."
        isClickable = true
        isFocusable = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.rgb(229, 235, 247))
        pincel.color = Color.rgb(20, 35, 77)
        val x = if (puntoX < 0) width / 2f else puntoX
        val y = if (puntoY < 0) height / 2f else puntoY
        canvas.drawCircle(x, y, 14 * resources.displayMetrics.density, pincel)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        parent.requestDisallowInterceptTouchEvent(event.action != MotionEvent.ACTION_UP && event.action != MotionEvent.ACTION_CANCEL)
        puntoX = event.x.coerceIn(0f, width.toFloat())
        puntoY = event.y.coerceIn(0f, height.toFloat())
        contentDescription = "Punto del lienzo en ${puntoX.toInt()}, ${puntoY.toInt()}"
        invalidate()
        if (event.action == MotionEvent.ACTION_UP) performClick()
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
