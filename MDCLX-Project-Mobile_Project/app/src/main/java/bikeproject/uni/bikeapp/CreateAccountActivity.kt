package bikeproject.uni.bikeapp

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Build
import android.os.Bundle
import androidx.core.app.NavUtils
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_create_account.*
import java.lang.ref.WeakReference

class CreateAccountActivity : AppCompatActivity()
{
    companion object
    {
        private lateinit var activityRef: WeakReference<CreateAccountActivity>

        val ref: CreateAccountActivity? get() = activityRef.get()
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        // Set up the login form.
        registerButton.setOnClickListener { attemptCreateAccount() }

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        activityRef            = WeakReference(this)
        Net.currentActivityRef = WeakReference(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when (item.itemId)
        {
            android.R.id.home ->
            {
                NavUtils.navigateUpFromSameTask(this)
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    // Try to login if there is not already an authentication task running
    private fun attemptCreateAccount()
    {
        // Reset errors.
        emailTextBox.error    = null
        usernameTextBox.error = null
        passwordTextBox.error = null

        // Store values at the time of the login attempt.
        val emailString    = emailTextBox   .text.toString()
        val usernameString = usernameTextBox.text.toString()
        val passwordString = passwordTextBox.text.toString()

        fun reportError(view: EditText, message: String)
        {
            view.error = message
            view.requestFocus()
        }

        when
        {
            emailString.isEmpty() ->
            {
                reportError(emailTextBox, getString(R.string.error_field_required))
                return
            }

            usernameString.isEmpty() ->
            {
                reportError(usernameTextBox, getString(R.string.error_field_required))
                return
            }

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

            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            else ->
            {
                showProgress(true)

                Net.socket.emit(NetEvent.CreateAccount.identifier, emailString, passwordString, usernameString)
            }
        }

    }

    fun onCreateAccountResult(emailInUseAlready: Boolean, gamertagInUseAlready: Boolean)
    {
        showProgress(false)

        val success = !emailInUseAlready && !gamertagInUseAlready

        when
        {
            emailInUseAlready ->
            {
                emailTextBox.error = "Email already in use"
                emailTextBox.requestFocus()
            }

            gamertagInUseAlready ->
            {
                usernameTextBox.error = "Account name already in use"
                usernameTextBox.requestFocus()
            }

            success ->
            {
                Toast.makeText(applicationContext, "Account created successfully.", Toast.LENGTH_LONG).show()

                startActivity<LoginActivity>()
            }
        }
    }

    //Shows the progress UI and hides the login form.
    private fun showProgress(show: Boolean)
    {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
        {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

            form.visibility = if (show) View.GONE else View.VISIBLE
            form.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 0 else 1).toFloat())
                    .setListener(object : AnimatorListenerAdapter()
                    {
                        override fun onAnimationEnd(animation: Animator)
                        {
                            form.visibility = if (show) View.GONE else View.VISIBLE
                        }
                    })

            progressBar.visibility = if (show) View.VISIBLE else View.GONE
            progressBar.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 1 else 0).toFloat())
                    .setListener(object : AnimatorListenerAdapter()
                    {
                        override fun onAnimationEnd(animation: Animator)
                        {
                            progressBar.visibility = if (show) View.VISIBLE else View.GONE
                        }
                    })
        }
        else
        {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressBar.visibility = if (show) View.VISIBLE else View.GONE
            form.visibility        = if (show) View.GONE    else View.VISIBLE
        }
    }
}
