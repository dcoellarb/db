package com.dreambox.dreamboxstores;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.dreambox.dreamboxstores.Utils.Utils;
import com.google.gson.JsonElement;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity {
    public static final String BASE_URL = "http://dreambox.com.ec";

    private Activity activity;
    private EditText email;
    private EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        setContentView(R.layout.activity_login);

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                LoginInterface service = retrofit.create(LoginInterface.class);
                //TODO Remove hardcoded user and user edittexts
                Call<JsonElement> call = service.login(new Login("dcoellar@gmail.com", "PVISIG"));
                call.enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(Response<JsonElement> response, Retrofit retrofit) {
                        JsonElement errors = response.body().getAsJsonObject().get("error");
                        if (errors != null){
                            String toastMessage = "No se puede ingresar en este momento, por favor comuniquese con el Administrador del sistema.";
                            String tostInvalidMessage = "Usuario y/o clave incorrecta.";
                            Utils.validateRestErrors(activity,errors,false,tostInvalidMessage,toastMessage);
                        }else{
                            JsonElement codigo = response.body().getAsJsonObject().get("codigo");
                            if (codigo != null && !codigo.getAsString().isEmpty()){
                                SharedPreferences prefs = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString(getString(R.string.token_key),codigo.getAsString());
                                editor.commit();

                                Intent intent = new Intent(activity, ReservacionesListActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }else{
                                Log.e("ERROR","no token sent");

                                //Toast toast = Toast.makeText(activity, "No se puede ingresar en este momento, por favor comuniquese con el Administrador del sistema.", Toast.LENGTH_LONG);
                                //toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 10);
                                //toast.show();

                                //TODO - this is temporal due to service issues
                                Intent intent = new Intent(activity, ReservacionesListActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.e("ERROR",t.getMessage());
                    }
                });
            }
        });
    }

    public interface LoginInterface {
        @POST("/APP/login.php")
        Call<JsonElement> login(@Body Login user);
    }

    class Login{
        private String usuario;
        private String clave;

        public Login(String usuario, String clave) {
            this.usuario = usuario;
            this.clave = clave;
        }

        public String getUsuario() {
            return usuario;
        }

        public void setUsuario(String usuario) {
            this.usuario = usuario;
        }

        public String getClave() {
            return clave;
        }

        public void setClave(String clave) {
            this.clave = clave;
        }
    }
}

