package com.dreambox.dreamboxstores.Models;

import java.util.Date;

/**
 * Created by dcoellar on 10/16/15.
 */
public class Reservacion {
    private String cod_reserva;
    private String cliente;
    private String id_estado;
    private String estado;
    private String id_paquete;
    private String paquete;
    private Date fecha;
    private String nom_proveedor;
    private String id_proveedor;

    public Reservacion(){}

    public Reservacion(String cod_reserva, String cliente, String id_estado, String estado, String id_paquete, String paquete, Date fecha, String nom_proveedor, String id_proveedor) {
        this.cod_reserva = cod_reserva;
        this.cliente = cliente;
        this.id_estado = id_estado;
        this.estado = estado;
        this.id_paquete = id_paquete;
        this.paquete = paquete;
        this.fecha = fecha;
        this.nom_proveedor = nom_proveedor;
        this.id_proveedor = id_proveedor;
    }
    public String getCod_reserva() {
        return cod_reserva;
    }

    public void setCod_reserva(String cod_reserva) {
        this.cod_reserva = cod_reserva;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getId_estado() {
        return id_estado;
    }

    public void setId_estado(String id_estado) {
        this.id_estado = id_estado;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getId_paquete() {
        return id_paquete;
    }

    public void setId_paquete(String id_paquete) {
        this.id_paquete = id_paquete;
    }

    public String getPaquete() {
        return paquete;
    }

    public void setPaquete(String paquete) {
        this.paquete = paquete;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getNom_proveedor() {
        return nom_proveedor;
    }

    public void setNom_proveedor(String nom_proveedor) {
        this.nom_proveedor = nom_proveedor;
    }

    public String getId_proveedor() {
        return id_proveedor;
    }

    public void setId_proveedor(String id_proveedor) {
        this.id_proveedor = id_proveedor;
    }

}
