package com.dreambox.dreamboxstores;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.dreambox.dreamboxstores.Models.Reservacion;
import com.dreambox.dreamboxstores.Utils.Transform;
import com.dreambox.dreamboxstores.Utils.Utils;
import com.dreambox.dreamboxstores.Views.DatePickerFragment;
import com.dreambox.dreamboxstores.Views.TimePickerFragment;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Calendar;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.http.Body;
import retrofit.http.Header;
import retrofit.http.POST;

/**
 * Created by dcoellar on 10/16/15.
 */
public class ReservacionActivity extends AppCompatActivity{

    public static final String BASE_URL = "http://dreambox.com.ec";
    private static String TOAST_MESSAGE = "No se pudo obtener la reserva correspondiente a este codigo";
    private static String TOAST_MESSAGE_ACTUALIZAR = "No se pudo actualizar esta reserva";

    private TimePickerFragment timeFragment;
    private DatePickerFragment dateFragment;
    private String mode;
    private EditText codeText;
    private Activity activity;
    private Reservacion reservacion;
    private LinearLayout reservaContainer;
    private LinearLayout enterCodeContainer;
    private String token;
    private TextView reservacionTime;
    private TextView reservacionDate;
    private int reservation_year;
    private int reservation_month;
    private int reservation_day;
    private int reservation_hourOfDay;
    private int reservation_minute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservacion);
        activity = this;

        Bundle b = getIntent().getExtras();
        mode = b.getString("mode");

        SharedPreferences prefs = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        token = prefs.getString(getString(R.string.token_key), "");

        Calendar c = Calendar.getInstance();
        reservation_year = c.get(Calendar.YEAR);
        reservation_month = c.get(Calendar.MONTH);
        reservation_day = c.get(Calendar.DAY_OF_MONTH);
        reservation_hourOfDay = c.get(Calendar.HOUR_OF_DAY);
        reservation_minute = c.get(Calendar.MINUTE);

        codeText = (EditText)findViewById(R.id.reserva_enter_code);
        findViewById(R.id.reserva_validar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                String code = codeText.getText().toString();
                if (!code.isEmpty()) {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    ValidateInterface service = retrofit.create(ValidateInterface.class);
                    Call<JsonElement> call = service.validate(token, new ValidarReserva(code));
                    call.enqueue(new Callback<JsonElement>() {
                        @Override
                        public void onResponse(Response<JsonElement> response, Retrofit retrofit) {
                            try {
                                JsonElement errors = response.body().getAsJsonObject().get("error");
                                if (errors != null) {
                                    Utils.validateRestErrors(activity, errors, true, "", TOAST_MESSAGE);
                                } else {
                                    JsonElement datos = response.body().getAsJsonObject().get("datos");
                                    if (datos != null) {
                                        JsonArray datosArray = datos.getAsJsonArray();
                                        if (datosArray != null) {
                                            if (datosArray.size() > 0) {
                                                JsonObject datosObject = datosArray.get(0).getAsJsonObject();
                                                if (datosObject != null) {
                                                    reservacion = Transform.transformResponseObject(datosObject);
                                                    LoadReservacion(reservacion);

                                                    codeText.setEnabled(false);
                                                    view.setEnabled(false);

                                                    reservaContainer.setVisibility(View.VISIBLE);
                                                    Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_up_in);
                                                    reservaContainer.setAnimation(animation);
                                                    animation.start();
                                                } else {
                                                    showValidateError();
                                                }
                                            } else {
                                                showValidateError();
                                            }
                                        } else {
                                        }
                                    } else {
                                        showValidateError();
                                    }
                                }
                            } catch(Exception e) {
                                Log.e("ERROR",e.getMessage());
                                showValidateError();
                            }
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            Log.e("ERROR", t.getMessage());
                            showValidateError();
                        }
                    });
                }
            }
        });

        reservacionTime = (TextView)findViewById(R.id.reservacion_time_picker);
        reservacionTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog dialog = new TimePickerDialog(activity, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        reservation_hourOfDay = i;
                        reservation_minute = i1;

                        Calendar cal = Calendar.getInstance();
                        cal.setTime(reservacion.getFecha());
                        cal.set(Calendar.HOUR_OF_DAY, reservation_hourOfDay);
                        cal.set(Calendar.MINUTE,reservation_minute);
                        reservacion.setFecha(cal.getTime());

                        reservacionTime.setText(formatTime(i, i1));
                    }
                },reservation_hourOfDay,reservation_minute,true);
                dialog.show();
            }
        });
        reservacionDate = (TextView)findViewById(R.id.reservacion_date_picker);
        reservacionDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dialog =
                        new DatePickerDialog(activity, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                                reservation_year = i;
                                reservation_month = i1;
                                reservation_day = i2;

                                Calendar cal = Calendar.getInstance();
                                cal.setTime(reservacion.getFecha());
                                cal.set(Calendar.YEAR, reservation_year);
                                cal.set(Calendar.MONTH,reservation_month);
                                cal.set(Calendar.DATE,reservation_day);
                                reservacion.setFecha(cal.getTime());

                                reservacionDate.setText(formatDate(i, i1, i2));
                            }
                        }, reservation_year, reservation_month, reservation_day);
                dialog.show();
            }
        });

        findViewById(R.id.reserva_actualizat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reservacion.setId_estado("5");
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                ActualizarReservaInterface service = retrofit.create(ActualizarReservaInterface.class);
                ActualizarReserva  body = new ActualizarReserva(reservacion);
                Gson gson = new Gson();
                String bodyText = gson.toJson(body);
                Call<JsonElement> call = service.actualizar(token, body);
                call.enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(Response<JsonElement> response, Retrofit retrofit) {
                        JsonElement errors = response.body().getAsJsonObject().get("error");
                        if (errors != null) {
                            Utils.validateRestErrors(activity, errors, true, "", TOAST_MESSAGE_ACTUALIZAR);
                        } else {
                            JsonElement respuesta = response.body().getAsJsonObject().get("exito");
                            if (respuesta != null) {
                                Intent intent = new Intent(activity, ReservacionesListActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                showActualizarError();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.e("ERROR", t.getMessage());
                        showActualizarError();
                    }
                });
            }
        });

        reservaContainer = (LinearLayout)findViewById(R.id.reserva_container);
        enterCodeContainer = (LinearLayout)findViewById(R.id.reserva_enter_code_container);
        if (mode.equalsIgnoreCase("new")){
            enterCodeContainer.setVisibility(View.VISIBLE);
            reservaContainer.setVisibility(View.GONE);
        }else if (mode.equalsIgnoreCase("update")) {
            enterCodeContainer.setVisibility(View.GONE);
            reservaContainer.setVisibility(View.VISIBLE);

            Gson gson = new Gson();
            reservacion =  gson.fromJson(b.getString("reserva"), Reservacion.class);
            LoadReservacion(reservacion);
        }
    }

    private void LoadReservacion(Reservacion reservacion){
        TextView cliente = (TextView)findViewById(R.id.reserva_nombre_cliente);
        cliente.setText(reservacion.getCliente());
        TextView descripcion = (TextView)findViewById(R.id.reserva_descripcion);
        descripcion.setText(reservacion.getPaquete());

        Calendar cal = Calendar.getInstance();
        cal.setTime(reservacion.getFecha());
        reservation_year = cal.get(Calendar.YEAR);
        reservation_month = cal.get(Calendar.MONTH);
        reservation_day = cal.get(Calendar.DATE);
        reservation_hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
        reservation_minute = cal.get(Calendar.MINUTE);
        reservacionDate.setText(formatDate(reservation_year,reservation_month,reservation_day));
        reservacionTime.setText(formatTime(reservation_hourOfDay, reservation_minute));
    }

    private void showValidateError(){
        Toast toast = Toast.makeText(activity, TOAST_MESSAGE, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 10);
        toast.show();
    }

    private void showActualizarError(){
        Toast toast = Toast.makeText(activity, TOAST_MESSAGE_ACTUALIZAR, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 10);
        toast.show();
    }

    public interface ValidateInterface {
        @POST("/APP/abrirReserva.php")
        Call<JsonElement> validate(@Header("Code") String token, @Body ValidarReserva validarReserva);
    }

    public interface ActualizarReservaInterface {
        @POST("/APP/actualizarReserva.php")
        Call<JsonElement> actualizar(@Header("Code") String token, @Body ActualizarReserva actualizarReserva);
    }

    class ValidarReserva {
        private String cod_reserva;

        public ValidarReserva(String cod_reserva) {
            this.cod_reserva = cod_reserva;
        }

        public String getCod_reserva() {
            return cod_reserva;
        }

        public void setCod_reserva(String codigo) {
            this.cod_reserva = cod_reserva;
        }
    }

    class ActualizarReserva {
        private String fecha;
        private String hora;
        private String minuto;
        private String id_estado;
        private String id_paquete;
        private String cod_reserva;

        public ActualizarReserva(Reservacion reservacion) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(reservacion.getFecha());

            String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
            if (cal.get(Calendar.MONTH) + 1 < 10){
                month = "0" + String.valueOf(cal.get(Calendar.MONTH) + 1);
            }

            this.fecha = cal.get(Calendar.YEAR) + "-" + month + "-" + cal.get(Calendar.DATE);
            this.hora = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
            this.minuto = String.valueOf(cal.get(Calendar.MINUTE));
            this.id_estado = reservacion.getId_estado();
            this.id_paquete = reservacion.getId_paquete();
            this.cod_reserva = reservacion.getCod_reserva();
        }

        public String getFecha() {
            return fecha;
        }

        public void setFecha(String fecha) {
            this.fecha = fecha;
        }

        public String getHora() {
            return hora;
        }

        public void setHora(String hora) {
            this.hora = hora;
        }

        public String getMinuto() {
            return minuto;
        }

        public void setMinuto(String minuto) {
            this.minuto = minuto;
        }

        public String getId_estado() {
            return id_estado;
        }

        public void setId_estado(String id_estado) {
            this.id_estado = id_estado;
        }

        public String getId_paquete() {
            return id_paquete;
        }

        public void setId_paquete(String id_paquete) {
            this.id_paquete = id_paquete;
        }

        public String getCod_reserva() {
            return cod_reserva;
        }

        public void setCod_reserva(String cod_reserva) {
            this.cod_reserva = cod_reserva;
        }
    }


    public static String formatDate(int year, int month, int day){
        String result = "";
        if (day < 10)
            result += "0" + day + "/";
        else
            result += day + "/";
        month += 1;
        if (month < 10)
            result += "0" + month + "/";
        else
            result += month + "/";
        result += year;
        return result;
    }

    public static String formatTime(int hourOfDay, int minute){
        String result = "";
        if (hourOfDay < 13)
            result += hourOfDay + ":";
        else
            result += (hourOfDay - 12) + ":";
        if (minute < 10)
            result += "0" + minute;
        else
            result += minute;
        if (hourOfDay < 13)
            result += "am";
        else
            result += "pm";
        return result;
    }
}
