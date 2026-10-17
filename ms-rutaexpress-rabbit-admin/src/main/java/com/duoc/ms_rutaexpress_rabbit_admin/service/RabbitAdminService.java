
package com.duoc.ms_rutaexpress_rabbit_admin.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RabbitAdminService {

    private final ColaAdminService colaAdminService;
    private final ExchangeAdminService exchangeAdminService;
    private final BindingAdminService bindingAdminService;

    public RabbitAdminService(
            ColaAdminService colaAdminService,
            ExchangeAdminService exchangeAdminService,
            BindingAdminService bindingAdminService) {

        this.colaAdminService = colaAdminService;
        this.exchangeAdminService = exchangeAdminService;
        this.bindingAdminService = bindingAdminService;
    }

    // ADMINISTRACION DE COLAS

    public Map<String, Object> crearCola(String nombre) {
        return colaAdminService.crear(nombre);
    }

    public Map<String, Object> consultarCola(String nombre) {
        return colaAdminService.consultar(nombre);
    }

    public void eliminarCola(String nombre) {
        colaAdminService.eliminar(nombre);
    }

    // ADMINISTRACION DE EXCHANGES

    public Map<String, Object> crearExchange(
            String nombre,
            String tipo) {

        return exchangeAdminService.crear(nombre, tipo);
    }

    public void eliminarExchange(String nombre) {
        exchangeAdminService.eliminar(nombre);
    }

    // ADMINISTRACION DE BINDINGS

    public Map<String, String> crearBinding(
            String cola,
            String exchange,
            String routingKey) {

        return bindingAdminService.crear(
                cola,
                exchange,
                routingKey
        );
    }

    public void eliminarBinding(
            String cola,
            String exchange,
            String routingKey) {

        bindingAdminService.eliminar(
                cola,
                exchange,
                routingKey
        );
    }
}
