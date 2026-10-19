package pe.tesla.familias

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import android.window.OnBackInvokedDispatcher
import android.widget.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var datos: DatosDemo
    private lateinit var content: LinearLayout
    private var screen = "Inicio"
    private var pendingNotice: Notice? = null
    private val navy = Color.rgb(20, 35, 77)
    private val muted = Color.rgb(91, 105, 128)
    private val dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        datos = DatosDemo(this)
        screen = savedInstanceState?.getString("screen") ?: "Inicio"
        pendingNotice = datos.notices().firstOrNull { it.id == savedInstanceState?.getString("pendingNotice") }
        if (intent.getBooleanExtra("openNotices", false) && savedInstanceState == null) screen = "Notificaciones"
        if (datos.loggedIn) {
            show(screen)
            intent.removeExtra("openNotices")
        } else login()
        if (Build.VERSION.SDK_INT >= 33) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT) {
                volver()
            }
        }
        if (savedInstanceState != null && screen == "Administrador demo") {
            content.findViewWithTag<EditText>("amount")?.setText(savedInstanceState.getString("amount"))
            content.findViewWithTag<EditText>("due")?.setText(savedInstanceState.getString("due"))
            content.findViewWithTag<Spinner>("attendance")?.setSelection(savedInstanceState.getInt("attendance"))
            content.findViewWithTag<CheckBox>("paid")?.isChecked = savedInstanceState.getBoolean("paid")
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("screen", screen)
        outState.putString("pendingNotice", pendingNotice?.id)
        outState.putString("amount", content.findViewWithTag<EditText>("amount")?.text?.toString())
        outState.putString("due", content.findViewWithTag<EditText>("due")?.text?.toString())
        outState.putInt("attendance", content.findViewWithTag<Spinner>("attendance")?.selectedItemPosition ?: 0)
        outState.putBoolean("paid", content.findViewWithTag<CheckBox>("paid")?.isChecked ?: false)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (datos.loggedIn && intent.getBooleanExtra("openNotices", false)) {
            show("Notificaciones")
            intent.removeExtra("openNotices")
        }
    }

    private fun base(title: String, back: Boolean = true) {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(Color.rgb(246, 248, 252)) }
        // Deja espacio para las barras del teléfono.
        root.setOnApplyWindowInsetsListener { view, insets ->
            if (Build.VERSION.SDK_INT >= 30) {
                val bars = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.ime())
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            } else {
                @Suppress("DEPRECATION")
                view.setPadding(insets.systemWindowInsetLeft, insets.systemWindowInsetTop, insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
            }
            insets
        }
        val header = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(16), dp(12), dp(16), dp(12)) }
        if (back) header.addView(Button(this).apply {
            text = "‹"
            textSize = 28f
            contentDescription = "Volver a Inicio"
            setTextColor(navy)
            backgroundTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
            setOnClickListener { show("Inicio") }
        }, LinearLayout.LayoutParams(dp(56), dp(52)))
        header.addView(label(title, 23, true), LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(header)
        val scroll = ScrollView(this).apply { isFillViewport = true }
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(12), dp(20), dp(24)) }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        root.requestApplyInsets()
        content.addView(label("MODO DEMOSTRACIÓN · Datos ficticios", 12).apply { setTextColor(muted) })
        gap()
    }
    private fun label(text: String, size: Int = 16, bold: Boolean = false) = TextView(this).apply {
        this.text = text; textSize = size.toFloat(); setTextColor(navy)
        if (bold) setTypeface(typeface, Typeface.BOLD)
        setPadding(0, dp(4), 0, dp(4))
    }
    private fun gap() { content.addView(Space(this), LinearLayout.LayoutParams(1, dp(16))) }
    private fun box(): LinearLayout {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(dp(18), dp(16), dp(18), dp(16))
            background = GradientDrawable().apply { setColor(Color.WHITE); cornerRadius = dp(18).toFloat(); setStroke(dp(1), Color.rgb(213, 222, 238)) }
        }
        content.addView(box, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) })
        return box
    }
    private fun button(text: String, action: () -> Unit) {
        content.addView(Button(this).apply {
            this.text = text; isAllCaps = false; textSize = 16f; setTextColor(Color.WHITE)
            backgroundTintList = android.content.res.ColorStateList.valueOf(navy)
            setOnClickListener { action() }
        }, LinearLayout.LayoutParams(-1, dp(58)).apply { bottomMargin = dp(10) })
    }
    private fun input(hint: String, type: Int = InputType.TYPE_CLASS_TEXT): EditText = EditText(this).apply {
        this.hint = hint; contentDescription = hint; inputType = type; setSingleLine(true); textSize = 17f
        content.addView(this, LinearLayout.LayoutParams(-1, dp(60)).apply { bottomMargin = dp(12) })
    }
    private fun toast(text: String) { Toast.makeText(this, text, Toast.LENGTH_LONG).show() }
    private fun money(cents: Long) = "S/ " + BigDecimal.valueOf(cents, 2).setScale(2).toPlainString()
    private fun detail(title: String, body: String) { AlertDialog.Builder(this).setTitle(title).setMessage(body).setPositiveButton("Cerrar", null).show() }

    private fun login() {
        screen = "Acceso"
        base("Tesla Familias", false)
        content.addView(label("Tesla", 44, true))
        content.addView(ImageView(this).apply {
            setImageResource(R.drawable.ic_tesla)
            contentDescription = "Icono de Tesla Familias"
        }, LinearLayout.LayoutParams(dp(60), dp(60)))
        content.addView(label("ACADEMIA PREUNIVERSITARIA", 13, true))
        gap()
        content.addView(label("Información de tu estudiante en un solo lugar", 22, true))
        gap()
        val user = input("DNI / usuario")
        val password = input("Contraseña", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        val error = label("", 14).apply { setTextColor(Color.rgb(166, 30, 44)); accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE }
        content.addView(error)
        button("Ingresar") {
            if (user.text.toString().trim() == "demo" && password.text.toString() == "tesla123") enter()
            else error.setText(R.string.error_acceso)
        }
        button("Acceso de demostración") { enter() }
        content.addView(label("Usuario: demo · Contraseña: tesla123", 14))
        gap()
        content.addView(label("DISCIPLINA HOY, GRANDES MAÑANAS", 12, true))
    }
    private fun enter() {
        datos.loggedIn = true
        val destination = if (intent.getBooleanExtra("openNotices", false)) "Notificaciones" else "Inicio"
        intent.removeExtra("openNotices")
        show(destination)
    }
    private fun show(destination: String) {
        getSystemService(InputMethodManager::class.java).hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        if (!datos.loggedIn) { login(); return }
        screen = destination
        base(if (destination == "Inicio") "Tesla Familias" else destination, destination != "Inicio")
        when (destination) {
            "Inicio" -> home()
            "Asistencias" -> attendance()
            "Obligaciones" -> obligations()
            "Notificaciones" -> notices()
            "Perfil" -> profile()
            "Administrador demo" -> admin()
            "Pruebas del dispositivo" -> content.addView(PruebasDispositivoView(this))
            else -> { screen = "Inicio"; home() }
        }
    }
    private fun home() {
        box().apply { addView(label("Juan Pérez", 25, true)); addView(label("Ciclo Preuniversitario · Estudiante ficticio", 15)) }
        box().apply { addView(label("ASISTENCIA DE HOY", 13, true)); addView(label(datos.todayStatus(), 28, true)); addView(label(LocalDate.now().format(dateFormat), 14)) }
        acceso("Asistencias", "Consulta el historial del estudiante") { show("Asistencias") }
        acceso("Obligaciones", "Pendiente: ${money(if (datos.obligation().paid) 0 else datos.obligation().amount)}") { show("Obligaciones") }
        val unread = datos.notices().count { !it.read }
        acceso("Notificaciones", if (unread == 1) "1 aviso sin leer" else "$unread avisos sin leer") { show("Notificaciones") }
        box().apply { addView(label("Último aviso", 18, true)); addView(label(datos.notices().firstOrNull()?.title ?: "Sin avisos registrados")) }
        acceso("Perfil", "Apoderado, estudiante y sesión") { show("Perfil") }
        acceso("Administrador de demostración", "Cambiar datos y probar avisos") { show("Administrador demo") }
    }
    private fun acceso(title: String, subtitle: String, action: () -> Unit) {
        box().apply {
            addView(label("$title  ›", 20, true))
            addView(label(subtitle, 14).apply { setTextColor(muted) })
            isFocusable = true
            setOnClickListener { action() }
        }
    }
    private fun attendance() {
        content.addView(label("Juan Pérez · Historial", 21, true)); gap()
        datos.attendance().forEach { record ->
            box().apply {
                addView(label(record.date.format(dateFormat), 20, true))
                addView(label(record.date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale.forLanguageTag("es-PE")), 14))
                addView(label(record.status, 20, true).apply { setTextColor(when(record.status) { "Presente" -> Color.rgb(0, 117, 77); "Tardanza" -> Color.rgb(144, 92, 0); else -> Color.rgb(175, 30, 44) }) })
                contentDescription = "${record.date.format(dateFormat)}, ${record.status}. Ver detalle"
                isFocusable = true
                setOnClickListener { detail("Registro de asistencia", "Juan Pérez\n${record.date.format(dateFormat)}\nEstado: ${record.status}\nDatos ficticios. Hora de ingreso no registrada.") }
            }
        }
    }
    private fun obligations() {
        val row = datos.obligation()
        box().apply { addView(label("Pendiente total", 17)); addView(label(money(if (row.paid) 0 else row.amount), 34, true)) }
        box().apply {
            addView(label("Cuota de demostración", 21, true)); addView(label("Vence: ${row.due.format(dateFormat)}"))
            addView(label(money(row.amount), 26, true)); addView(label(if (row.paid) "Pagada" else "Pendiente", 17, true))
            isFocusable = true
            setOnClickListener { detail("Detalle de obligación", "Cuota de demostración\nImporte: ${money(row.amount)}\nVencimiento: ${row.due.format(dateFormat)}\nEstado: ${if (row.paid) "Pagada" else "Pendiente"}\nConsulta informativa. No procesa pagos.") }
        }
        content.addView(label("Consulta informativa. Esta aplicación no procesa pagos.", 14))
    }
    private fun notices() {
        val rows = datos.notices()
        if (rows.isEmpty()) content.addView(label("No hay avisos registrados."))
        rows.forEach { row ->
            box().apply {
                addView(label((if (!row.read) "● " else "") + row.title, 21, true))
                addView(label(row.body)); addView(label(LocalDateTime.parse(row.time).format(DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm")), 13))
                addView(label(if (row.read) "Leído" else "Sin leer · Toca para abrir", 13))
                isFocusable = true
                setOnClickListener { datos.markRead(row.id); show("Notificaciones"); detail(row.title, row.body) }
            }
        }
    }
    private fun profile() {
        box().apply { addView(label("María Pérez", 25, true)); addView(label("Apoderada ficticia · Usuario demo")); addView(label("Estudiante: Juan Pérez\nCiclo Preuniversitario")) }
        button("Pruebas del dispositivo") { show("Pruebas del dispositivo") }
        button("Cerrar sesión") { datos.loggedIn = false; login() }
    }
    private fun admin() {
        content.addView(label("Juan Pérez", 24, true))
        content.addView(label("Los cambios afectan únicamente los datos ficticios de este dispositivo.", 14))
        gap(); content.addView(label("Estado de asistencia", 18, true))
        val states = listOf("Presente", "Tardanza", "Inasistencia")
        val spinner = Spinner(this).apply { tag = "attendance"; contentDescription = "Estado de asistencia"; adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, states); setSelection(states.indexOf(datos.todayStatus()).coerceAtLeast(0)) }
        content.addView(spinner, LinearLayout.LayoutParams(-1, dp(56)))
        button("Guardar asistencia") { datos.saveAttendance(spinner.selectedItem.toString()); toast("Asistencia guardada") }
        button("Generar aviso de asistencia") {
            val row = datos.addNotice("Asistencia: ${datos.todayStatus()}", "Juan Pérez: ${datos.todayStatus()}. Aviso local de demostración.")
            postNotification(row); toast("Aviso guardado en el historial")
        }
        gap(); content.addView(label("Editar obligación", 20, true))
        val obligation = datos.obligation()
        content.addView(label("Importe en soles", 14, true))
        val amount = input("Importe en soles", InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL).apply { tag = "amount"; setText(BigDecimal.valueOf(obligation.amount, 2).toPlainString()) }
        content.addView(label("Vencimiento (AAAA-MM-DD)", 14, true))
        val due = input("Vencimiento: AAAA-MM-DD").apply { tag = "due"; setText(obligation.due.toString()) }
        val paid = CheckBox(this).apply { tag = "paid"; setText(R.string.obligacion_pagada); isChecked = obligation.paid }
        content.addView(paid)
        button("Guardar obligación") {
            try {
                val cents = BigDecimal(amount.text.toString().replace(',', '.')).setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).longValueExact()
                val date = LocalDate.parse(due.text.toString().trim())
                datos.saveObligation(cents, date, paid.isChecked); toast("Obligación guardada")
            } catch (_: Exception) { detail("Revisa los datos", "Ingresa un importe entre 0 y 999,999.99, con máximo dos decimales, y una fecha válida en formato AAAA-MM-DD.") }
        }
        gap()
        button("Restablecer datos de demostración") {
            AlertDialog.Builder(this).setTitle("¿Restablecer demo?").setMessage("Se eliminarán los cambios y se recuperarán los datos ficticios iniciales.")
                .setNegativeButton("Cancelar", null).setPositiveButton("Restablecer") { _, _ -> datos.reset(); getSystemService(NotificationManager::class.java).cancelAll(); show("Administrador demo"); toast("Demo restablecida") }.show()
        }
    }
    private fun postNotification(notice: Notice) {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            pendingNotice = notice
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 10)
            return
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel("demo", "Avisos de demostración", NotificationManager.IMPORTANCE_DEFAULT))
        val intent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java).putExtra("openNotices", true).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        manager.notify(notice.id.hashCode(), Notification.Builder(this, "demo").setSmallIcon(R.drawable.ic_aviso)
            .setContentTitle(notice.title).setContentText(notice.body).setContentIntent(intent).setAutoCancel(true).build())
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 10) {
            val notice = pendingNotice
            pendingNotice = null
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED && notice != null) postNotification(notice)
            else toast("El aviso permanece en el historial aunque las notificaciones estén desactivadas.")
        }
    }
    private fun volver() {
        if (datos.loggedIn && screen != "Inicio") show("Inicio") else finish()
    }
    @Deprecated("Se usa en Android 8 a 12")
    @SuppressLint("GestureBackNavigation") // Android 13 o superior usa el callback registrado en onCreate.
    override fun onBackPressed() { volver() }
}
