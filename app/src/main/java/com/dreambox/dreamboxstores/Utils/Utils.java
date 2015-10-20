package com.dreambox.dreamboxstores.Utils;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.dreambox.dreamboxstores.LoginActivity;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 * Created by dcoellar on 10/17/15.
 */
public class Utils {
    public static void validateRestErrors(Activity activity, JsonElement errors, Boolean redirect, String toastInvalidUserMessage, String toastMessage){
        Boolean isWrongUserPassword = false;
        JsonArray array = errors.getAsJsonArray();
        if (array != null){
            for (int i = 0;i < array.size();i++){
                JsonElement element = array.get(i).getAsJsonObject().get("codigo");
                if (element != null){
                    if (element.getAsString().equalsIgnoreCase("100")){
                        isWrongUserPassword = true;
                    }else{
                        JsonElement descripcion = array.get(i).getAsJsonObject().get("descripcion");
                        if (descripcion != null) {
                            Log.e("ERROR", descripcion.getAsString());
                        }else{
                            Log.e("ERROR", "Error without description");
                        }
                    }
                }
            }
        }
        if (isWrongUserPassword){
            if (redirect){
                Intent intent = new Intent(activity, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                activity.startActivity(intent);
            }else{
                Log.e("ERROR","Invalid user");
                Toast toast = Toast.makeText(activity, toastInvalidUserMessage, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 10);
                toast.show();
            }

        }else{
            Toast toast = Toast.makeText(activity, toastMessage, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 10);
            toast.show();
        }
    }
}
