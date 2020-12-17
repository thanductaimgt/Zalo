package com.mgt.zalo.ui.sign_up

import android.graphics.drawable.ColorDrawable
import android.telephony.PhoneNumberUtils
import android.text.TextUtils
import android.view.View
import androidx.core.widget.addTextChangedListener
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseActivity
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : BaseActivity() {
    override fun onBindViews() {
        setContentView(R.layout.activity_sign_up)

        phoneEditText.addTextChangedListener {
            if (TextUtils.isEmpty(it)) {
                phoneWarningTV.visibility = View.GONE
                clearPhoneImgView.visibility = View.GONE
            } else {
                clearPhoneImgView.visibility = View.VISIBLE
                if (PhoneNumberUtils.isGlobalPhoneNumber(it.toString())) {
                    phoneWarningTV.visibility = View.GONE
                } else {
                    phoneWarningTV.visibility = View.VISIBLE
                }
            }
            checkEnableContinueButton()
        }

        nameEditText.addTextChangedListener {
            if (TextUtils.isEmpty(it)) {
                clearNameImgView.visibility = View.GONE
            } else {
                clearNameImgView.visibility = View.VISIBLE
            }
            checkEnableContinueButton()
        }

        backImgView.setOnClickListener(this)
        clearNameImgView.setOnClickListener(this)
        clearPhoneImgView.setOnClickListener(this)
        continueButton.setOnClickListener(this)
    }

    private fun checkEnableContinueButton() {
        setContinueButtonEnable(!TextUtils.isEmpty(nameEditText.text) && PhoneNumberUtils.isGlobalPhoneNumber(phoneEditText.text.toString()))
    }

    private fun setContinueButtonEnable(isEnabled: Boolean) {
        continueButton.apply {
            background = ColorDrawable(if (isEnabled) {
                R.color.lightPrimary
            } else {
                R.color.colorMessageStroke
            })
            this.isEnabled = isEnabled
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.backImgView -> onBackPressed()
            R.id.clearNameImgView -> nameEditText.setText("")
            R.id.clearPhoneImgView -> phoneEditText.setText("")
            R.id.continueButton -> {
                if (continueButton.isEnabled) {
                    alertDialog.show(supportFragmentManager,
                            title = getString(R.string.label_validate_phone),
                            description = "${phoneEditText.text}\n\n${getString(R.string.description_validate_phone)}",
                            button1Text = getString(R.string.label_change),
                            button2Text = getString(R.string.label_confirm),
                            button2Action = {
                                zaloFragmentManager.addValidatePhoneFragment(phoneEditText.text.toString())
                            })
                }
            }
        }
    }
}