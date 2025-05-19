package com.egg.electricity_store.controllers;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ErrorsController implements ErrorController {
    // Todo recurso que tenga "/error" (GET/POST), ingresa a este método.
    @RequestMapping(value = "/error", method = { RequestMethod.GET, RequestMethod.POST })
    /*
     * El método recibe una petición HTTP como parámetro y retorna la vista con los
     * datos
     * del error de manera personalizada.
     */
    public ModelAndView renderErrorPage(HttpServletRequest httpRequest) {
        /*
         * ModelAndView funciona similar al ModelMap para enviar datos a la vista.
         * Además, retorna la vista. Entonces, se crea un objeto ModelAndView llamado
         * "errorPage", con la página/vista de error "error.html" para retornarla con
         * el código y mensaje de error.
         */
        ModelAndView errorPage = new ModelAndView("error");

        // Para personalizar el mensaje de error acorde con el código de error.
        String errorMsg = "";

        // Recupera el código de error que viene del servidor.
        int httpErrorCode = getErrorCode(httpRequest);

        if (httpErrorCode == -1) {
            httpErrorCode = 500; // Asigna un código de error por defecto
        }

        switch (httpErrorCode) {
            case 400: {
                errorMsg = "Bad Request. The server cannot process the request due to an apparent client error.";
                break;
            }
            case 401: {
                errorMsg = "Unauthorized. Authentication is required and has failed or has not yet been provided.";
                break;
            }
            case 403: {
                errorMsg = "Forbidden. User not having the necessary permissions for a resource or attempting a prohibited action";
                break;
            }
            case 404: {
                errorMsg = "Not Found. The requested resource could not be found but may be available in the future.";
                break;
            }
            case 500: {
                errorMsg = "Internal Server Error. An unexpected condition was encountered";
                break;
            }
            default: {
                errorMsg = "Unexpected HTTP Error.";
                break;
            }
        }

        errorPage.addObject("errorCode", httpErrorCode);
        errorPage.addObject("httpErrorMessage", errorMsg);
        // Retorna y renderiza la vista con los vaores de codigo y mensaje
        return errorPage;
    }

    // Recibe la petición y obtiene su atributo ERROR_STATUS_CODE, como Integer
    private int getErrorCode(HttpServletRequest httpRequest) {
        // Obtiene el atributo tipo Integer ("jakarta.servlet.error.status_code")
        Object statusCode = httpRequest.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        // Si el atributo es null o no es una instancia de Integer,
        // retorna un valor por defecto, en este caso -1
        return (statusCode instanceof Integer) ? (Integer) statusCode : -1;
    }
}
