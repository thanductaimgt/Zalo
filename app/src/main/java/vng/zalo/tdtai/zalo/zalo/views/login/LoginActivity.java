package vng.zalo.tdtai.zalo.zalo.views.login;

import androidx.appcompat.app.AppCompatActivity;
import vng.zalo.tdtai.zalo.R;
import vng.zalo.tdtai.zalo.zalo.ZaloApplication;
import vng.zalo.tdtai.zalo.zalo.models.UserInfo;
import vng.zalo.tdtai.zalo.zalo.utils.Constants;
import vng.zalo.tdtai.zalo.zalo.views.home.HomeActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private TextInputEditText phoneTextInputEditText;
    private TextInputEditText passTextInputEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        phoneTextInputEditText = findViewById(R.id.phoneTextInputEditText);
        passTextInputEditText = findViewById(R.id.passTextInputEditText);
        ImageView clearTextButton = findViewById(R.id.clearTextImgButton);
        Button loginButton = findViewById(R.id.loginButton);

        clearTextButton.setOnClickListener(this);
        loginButton.setOnClickListener(this);

        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.label_log_in);

        Button swapButton = findViewById(R.id.swapButton);
        swapButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.clearTextImgButton:
                phoneTextInputEditText.setText("");
                break;
            case R.id.loginButton:
                String currentPhone = Objects.requireNonNull(phoneTextInputEditText.getText()).toString();
                String currentPass = Objects.requireNonNull(passTextInputEditText.getText()).toString();
                validateCredentials(currentPhone, currentPass);
                break;
            case R.id.swapButton:
                phoneTextInputEditText.setText(Objects.requireNonNull(phoneTextInputEditText.getText()).toString().equals("0123456789")? "0987654321" : "0123456789");
                break;
        }
    }

    void validateCredentials(String phone, String pass){
        ZaloApplication.getFirebaseInstance().collection(Constants.COLLECTION_USERS)
                .document(phone)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        ZaloApplication.currentUser = Objects.requireNonNull(task.getResult()).toObject(UserInfo.class);
                        assert ZaloApplication.currentUser != null;
                        ZaloApplication.currentUser.phone = phone;
                        Intent startLobbyIntent = new Intent(this, HomeActivity.class);
                        Toast.makeText(getApplicationContext(), "Log in with Internet", Toast.LENGTH_SHORT).show();
                        startActivity(startLobbyIntent);
//                    finish();
                    } else {
                        ZaloApplication.currentUser.phone = phone;
                        Toast.makeText(getApplicationContext(), "Log in WITHOUT Internet !!!", Toast.LENGTH_SHORT).show();
                        Intent startLobbyIntent = new Intent(this, HomeActivity.class);
                        startActivity(startLobbyIntent);
                        //                    finish();
                    }
                });

    }
}