package com.dreambox.dreamboxstores.Utils;

import com.dreambox.dreamboxstores.Models.Reservacion;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by dcoellar on 10/17/15.
 */
public class Transform {
    public static ArrayList<Reservacion> transformRespose(JsonElement data){
        ArrayList<Reservacion> list = new ArrayList<>();
        JsonArray dataArray = data.getAsJsonArray();
        if (dataArray != null){
            for(int i=0;i<dataArray.size();i++){
                JsonElement item = dataArray.get(i);
                JsonObject itemObject = item.getAsJsonObject();
                if (itemObject != null){
                    Reservacion reserva = transformResponseObject(itemObject);
                    list.add(reserva);
                }
            }
        }
        return list;
    }

    public static Reservacion transformResponseObject(JsonObject itemObject){
        Reservacion reserva = new Reservacion();
        JsonElement member = itemObject.get("cod_reserva");
        if (member != null){
            reserva.setCod_reserva(member.getAsString());
        }
        member = itemObject.get("cliente");
        if (member != null){
            reserva.setCliente(member.getAsString());
        }
        member = itemObject.get("id_estado");
        if (member != null){
            reserva.setId_estado(member.getAsString());
        }
        member = itemObject.get("estado");
        if (member != null){
            reserva.setEstado(member.getAsString());
        }
        member = itemObject.get("id_paquete");
        if (member != null){
            reserva.setId_paquete(member.getAsString());
        }
        member = itemObject.get("paquete");
        if (member != null){
            reserva.setPaquete(member.getAsString());
        }
        member = itemObject.get("fecha");
        if (member != null){
            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd HH:mm:ss")
                    .create();
            reserva.setFecha(gson.fromJson(member,Date.class));
        }
        member = itemObject.get("nom_proveedor");
        if (member != null) {
            reserva.setNom_proveedor(member.getAsString());
        }
        member = itemObject.get("id_proveedor");
        if (member != null) {
            reserva.setId_proveedor(member.getAsString());
        }
        return reserva;
    }

}
