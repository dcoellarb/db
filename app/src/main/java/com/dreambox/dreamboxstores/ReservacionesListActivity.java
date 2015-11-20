package com.dreambox.dreamboxstores;

import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.dreambox.dreamboxstores.Models.Reservacion;
import com.dreambox.dreamboxstores.Utils.Transform;
import com.dreambox.dreamboxstores.Utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Date;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * Created by dcoellar on 10/16/15.
 */
public class ReservacionesListActivity extends AppCompatActivity {
    public static final String BASE_URL = "http://dreambox.com.ec";

    private String searchText = "";
    private String estadoId = "5";
    private ArrayList<Reservacion> list = new ArrayList<>();
    private LayoutInflater inflater;
    private Menu menu;
    private ListView listView;
    private Activity activity;
    private android.support.v4.widget.SwipeRefreshLayout swipeRefresh;
    private ViewGroup selectedEstado;
    private SearchView searchViewAction;
    private MenuItem filter;
    private MenuItem searchMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservaciones_list);
        activity = this;

        inflater = getLayoutInflater();

        listView = (ListView)findViewById(R.id.reservaciones_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(activity, ReservacionActivity.class);
                intent.putExtra("mode","update");

                Gson gson = new Gson();
                intent.putExtra("reserva", gson.toJson(list.get(i)));

                startActivity(intent);
            }
        });

        swipeRefresh = (android.support.v4.widget.SwipeRefreshLayout)findViewById(R.id.reservaciones_swipe_refresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                searchText = "";
                LoadReservas();
            }
        });

        findViewById(R.id.reservaciones_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, ReservacionActivity.class);
                intent.putExtra("mode","new");
                startActivity(intent);
            }
        });

        selectedEstado = (ViewGroup)findViewById(R.id.reservaciones_pendientes);
        findViewById(R.id.reservaciones_pendientes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (estadoId != "5"){
                    ((ViewGroup)view).getChildAt(0).setVisibility(View.VISIBLE);
                    selectedEstado.getChildAt(0).setVisibility(View.INVISIBLE);
                    estadoId = "5";
                    LoadReservas();
                    selectedEstado = (ViewGroup)view;
                }
            }
        });

        findViewById(R.id.reservaciones_confirmadas).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (estadoId != "8"){
                    ((ViewGroup)view).getChildAt(0).setVisibility(View.VISIBLE);
                    selectedEstado.getChildAt(0).setVisibility(View.INVISIBLE);
                    estadoId = "8";
                    LoadReservas();
                    selectedEstado = (ViewGroup)view;
                }
            }
        });

        findViewById(R.id.reservaciones_canceladas).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (estadoId != "6"){
                    ((ViewGroup)view).getChildAt(0).setVisibility(View.VISIBLE);
                    selectedEstado.getChildAt(0).setVisibility(View.INVISIBLE);
                    estadoId = "6";
                    LoadReservas();
                    selectedEstado = (ViewGroup)view;
                }
            }
        });

        LoadReservas();

        handleIntent(getIntent());
    }

    private void LoadReservas(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        LoginInterface service = retrofit.create(LoginInterface.class);

        SharedPreferences prefs = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        String token = prefs.getString(getString(R.string.token_key),"");

        Call<JsonElement> call = service.reservas(token, estadoId, searchText);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Response<JsonElement> response, Retrofit retrofit) {
                JsonElement errors = response.body().getAsJsonObject().get("error");
                if (errors != null) {
                    JsonArray errorsArray = errors.getAsJsonArray();
                    if (errorsArray != null && errorsArray.size() == 1){
                        JsonObject firstError = errorsArray.get(0).getAsJsonObject();
                        if (firstError != null){
                            if (firstError.get("codigo") != null){
                                if (firstError.get("codigo").getAsString().equalsIgnoreCase("300")){
                                    list = new ArrayList<>();
                                    listView.setAdapter(new ReservacionesListAdapter());
                                }else{
                                    Utils.validateRestErrors(activity, errors, true, "", "");
                                }
                            }else{
                                Utils.validateRestErrors(activity, errors, true, "", "");
                            }
                        }else{
                            Utils.validateRestErrors(activity, errors, true, "", "");
                        }
                    }else{
                        Utils.validateRestErrors(activity, errors, true, "", "");
                    }
                } else {
                    JsonElement data = response.body().getAsJsonObject().get("datos");
                    if (data != null) {
                        list = Transform.transformRespose(data);
                        listView.setAdapter(new ReservacionesListAdapter());
                    }
                }
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("ERROR", t.getMessage());
                swipeRefresh.setRefreshing(false);
            }
        });
    }

    public interface LoginInterface {
        @POST("/APP/listadoReservas.php")
        Call<JsonElement> reservas(@Header("Code") String token, @Query("id_estado") String estado, @Query("cliente") String searchText);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_reservaciones_list, menu);

        //search_action = menu.findItem(R.id.action_search);
        filter = menu.findItem(R.id.action_filter);

        // Associate searchable configuration with the SearchView
        searchMenuItem = menu.findItem(R.id.search);
        searchViewAction = (SearchView) MenuItemCompat.getActionView(searchMenuItem);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchableInfo searchableInfo = searchManager
                .getSearchableInfo(getComponentName());
        searchViewAction.setSearchableInfo(searchableInfo);
        searchViewAction.setIconifiedByDefault(true);
        searchViewAction.findViewById(R.id.search_close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchViewAction.setQuery("", false);
                searchText = "";
                searchViewAction.setIconified(true);

                LoadReservas();
            }
        });
        searchViewAction.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    searchText = "";
                    LoadReservas();
                }
                return false;
            }
        });
        searchViewAction.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(view, 0);
                    }
                }
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_filter) {
            final View view = findViewById(R.id.reservaciones_filter);
            if (view.getVisibility() == View.GONE){
                view.setVisibility(View.VISIBLE);
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_up_in);
                view.setAnimation(animation);
                animation.start();

            }else{
                view.setVisibility(View.GONE);
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_up_out);
                view.setAnimation(animation);
                animation.start();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        String action = intent.getAction();
        if (Intent.ACTION_SEARCH.equals(action)) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            searchText = query;
            LoadReservas();
        }
    }

    class ReservacionesListAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return list.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            Reservacion reservacion = list.get(i);

            view = inflater.inflate(R.layout.item_reservaciones,viewGroup,false);

            TextView reserva_nombre_cliente = (TextView)view.findViewById(R.id.reserva_nombre_cliente);
            reserva_nombre_cliente.setText(reservacion.getCliente());
            TextView reserva_fecha = (TextView)view.findViewById(R.id.reserva_fecha);
            reserva_fecha.setText(formatDate(reservacion.getFecha()));
            TextView reserva_descripcion = (TextView)view.findViewById(R.id.reserva_descripcion);
            reserva_descripcion.setText(reservacion.getPaquete());

            return view;
        }

    }

    public static String formatDate(Date date){
        String result = "";
        switch (date.getMonth()){
            case 0:
                result = "Ene ";
                break;
            case 1:
                result = "Feb ";
                break;
            case 2:
                result = "Mar ";
                break;
            case 3:
                result = "Abr ";
                break;
            case 4:
                result = "May ";
                break;
            case 5:
                result = "Jun ";
                break;
            case 6:
                result = "Jul ";
                break;
            case 7:
                result = "Ago ";
                break;
            case 8:
                result = "Sep ";
                break;
            case 9:
                result = "Oct ";
                break;
            case 10:
                result = "Nov ";
                break;
            case 11:
                result = "Dic ";
                break;
        }
        result += date.getDate();
        return  result;
    }
}
