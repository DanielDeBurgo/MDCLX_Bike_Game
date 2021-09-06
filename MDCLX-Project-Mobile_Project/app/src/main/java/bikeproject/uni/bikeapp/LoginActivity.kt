package bikeproject.uni.bikeapp

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*
import pub.devrel.easypermissions.EasyPermissions
import java.lang.ref.WeakReference

class LoginActivity : AppCompatActivity()
{
    companion object
    {
        private lateinit var activityRef: WeakReference<LoginActivity>

        val ref: LoginActivity? get() = activityRef.get()
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        EasyPermissions.requestPermissions(this, "In order to scan the QR code", 1, Manifest.permission.CAMERA)

        // Set up the login form.
        signInButton.setOnClickListener { attemptLogin() }

        registerButton.setOnClickListener { startActivity<CreateAccountActivity>() }

        activityRef            = WeakReference(this)
        Net.currentActivityRef = WeakReference(this)

        ipTextBox.setText("10.41.181.128")
        ipButton.setOnClickListener {
            Net.connect(ipTextBox.text.toString())
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun attemptLogin()
    {
        // Reset errors.
        usernameTextBox.error = null
        passwordTextBox.error = null

        // Store values at the time of the login attempt.
        val usernameString = usernameTextBox.text.toString()
        val passwordString = passwordTextBox.text.toString()

        fun reportError(view: EditText, message: String)
        {
            view.error = message
            view.requestFocus()
        }

        // Handle errors
        when
        {
            usernameString.isEmpty() ->
            {
                reportError(usernameTextBox, getString(R.string.error_field_required))
                return
            }

            // Check for a valid password, if the user entered one.
            passwordString.isEmpty() ->
            {
                reportError(passwordTextBox, getString(R.string.error_field_required))
                return
            }

            passwordString.length < 4 ->
            {
                reportError(passwordTextBox, getString(R.string.error_invalid_password))
                return
            }
        }

        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        showProgress(true)

        Net.socket.emit(NetEvent.Login.identifier, usernameString, passwordString)
    }

    fun onLoginResult(success: Boolean, unknownUser: Boolean, isManager: Boolean)
    {
        showProgress(false)

        val userLoggedIn    = success  && !isManager
        val managerLoggedIn = success  && isManager
        val wrongUsername   = !success && unknownUser
        val wrongPassword   = !success && !unknownUser

        when
        {
            userLoggedIn    ->
            {
                Net.CurrentUserName = usernameTextBox.text.toString()
                startActivity<MainActivity>()
            }

            managerLoggedIn -> startActivity<ManagerActivity>() //@TODO: Go to manager activity

            wrongUsername   ->
            {
                usernameTextBox.error = getString(R.string.error_invalid_username)
                usernameTextBox.requestFocus()
            }

            wrongPassword   ->
            {
                passwordTextBox.error = getString(R.string.error_incorrect_password)
                passwordTextBox.requestFocus()
            }
        }
    }

    // Shows the progress UI and hides the login form.
    private fun showProgress(show: Boolean)
    {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
        {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

            loginForm.visibility = if (show) View.GONE else View.VISIBLE
            loginForm.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 0 else 1).toFloat())
                    .setListener(object : AnimatorListenerAdapter()
                    {
                        override fun onAnimationEnd(animation: Animator)
                        {
                            loginForm.visibility = if (show) View.GONE else View.VISIBLE
                        }
                    })

            loginProgressBar.visibility = if (show) View.VISIBLE else View.GONE
            loginProgressBar.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 1 else 0).toFloat())
                    .setListener(object : AnimatorListenerAdapter()
                    {
                        override fun onAnimationEnd(animation: Animator)
                        {
                            loginProgressBar.visibility = if (show) View.VISIBLE else View.GONE
                        }
                    })
        }
        else
        {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            loginProgressBar.visibility = if (show) View.VISIBLE else View.GONE
            loginForm.visibility        = if (show) View.GONE    else View.VISIBLE
        }
    }
}