package com.mgt.zalo.ui.sign_up.validate_phone

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_validate_phone.*
import kotlinx.android.synthetic.main.fragment_validate_phone.backImgView
import java.util.concurrent.TimeUnit

class ValidatePhoneFragment : BaseFragment() {
    private lateinit var phone: String

    override fun applyArguments(args: Bundle) {
        phone = "+16505551998"//args.getString(ARG_1)!!
    }

    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View {
        return inflater.inflate(R.layout.fragment_validate_phone, container, false)
    }

    override fun onBindViews() {
        descTV2.text = String.format(getString(R.string.description_validate_code_sent), phone)

        resendButton.setOnClickListener(this)
        backImgView.setOnClickListener(this)
    }

    override fun onViewsBound() {
        FirebaseAuth.getInstance().firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber(phone, code)
        sendVerificationCode()
    }

    private val code = "199811"
    private val callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(p0: PhoneAuthCredential) {
            Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT).show()
        }

        override fun onVerificationFailed(p0: FirebaseException) {
            Toast.makeText(requireContext(), "Fail", Toast.LENGTH_SHORT).show()
        }

        override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
            startTimer()
        }

        override fun onCodeAutoRetrievalTimeOut(p0: String) {
            setResendButtonEnable(true)
        }
    }

    private fun sendVerificationCode() {
        setResendButtonEnable(false)
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone, // Phone number to verify
                VERIFICATION_TIMEOUT, // Timeout duration
                TimeUnit.SECONDS, // Unit of timeout
                activity(), // Activity (for callback binding)
                callback)
    }

    private fun setResendButtonEnable(isEnabled: Boolean) {
        resendButton.apply {
            setTextColor(if (isEnabled) {
                R.color.lightPrimary
            } else {
                R.color.strongGray
            })
            this.isEnabled = isEnabled
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private var startTime = VERIFICATION_TIMEOUT.toInt()

    private fun startTimer() {
        timerTextView.visibility = View.VISIBLE
        startTime = VERIFICATION_TIMEOUT.toInt()
        countDown()
    }

    private fun countDown() {
        timerTextView.text = utils.getVideoDurationFormat(startTime--)
        handler.postDelayed({
            if (startTime > 0) {
                countDown()
            } else {
                timerTextView.visibility = View.GONE
            }
        }, 1000)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.resendButton -> sendVerificationCode()
            R.id.backImgView -> onBackPressed()
        }
    }

    companion object {
        const val VERIFICATION_TIMEOUT = 30L //secs
    }
}