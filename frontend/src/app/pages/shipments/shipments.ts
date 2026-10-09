import {
  ChangeDetectorRef,
  Component,
  OnInit
} from '@angular/core';

import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { MsalService } from '@azure/msal-angular';

import { ShipmentService } from '../../services/shipment';
import { CatalogService } from '../../services/catalog';

import { environment } from '../../../environment';

@Component({
  selector: 'app-shipments',
  standalone: true,
  imports: [
    RouterLink,
    FormsModule
  ],
  templateUrl: './shipments.html',
  styleUrl: './shipments.css'
})
export class ShipmentsComponent implements OnInit {

  // =====================================================
  // DATOS GENERALES
  // =====================================================

  shipments: any[] = [];
  services: any[] = [];
  userRoles: string[] = [];
  correoUsuario = '';

  loading = true;
  errorMessage = '';
  successMessage = '';

  creandoEnvio = false;
  actualizandoEstadoId: number | null = null;


  // =====================================================
  // CONSTANTES DE COTIZACIÓN
  // Deben coincidir con el backend Shipments
  // =====================================================

  private readonly FACTOR_VOLUMETRICO = 4000;
  private readonly PESO_INCLUIDO = 1;
  private readonly PRECIO_KILO_EXTRA = 800;
  private readonly PORCENTAJE_FRAGIL = 0.10;


  // =====================================================
  // NUEVO ENVÍO
  // =====================================================

  nuevoEnvio: any = {
    correoRemitente: '',
    nombreDestinatario: '',
    correoDestinatario: '',
    direccionOrigen: '',
    direccionDestino: '',
    servicio: '',

    tipoPaquete: '',
    alto: null,
    ancho: null,
    largo: null,
    peso: null,
    valorDeclarado: null,
    fragil: false,
    observaciones: ''
  };


  constructor(
    private shipmentService: ShipmentService,
    private catalogService: CatalogService,
    private authService: MsalService,
    private cdr: ChangeDetectorRef
  ) {}


  ngOnInit(): void {
    this.cargarRoles();
    this.cargarEnvios();
    this.cargarServicios();
  }


  // =====================================================
  // AUTENTICACIÓN / ROLES
  // =====================================================

  cargarRoles(): void {

    const instance =
      this.authService.instance;

    let account =
      instance.getActiveAccount();


    if (
      !account &&
      instance.getAllAccounts().length > 0
    ) {

      account =
        instance.getAllAccounts()[0];

      instance.setActiveAccount(
        account
      );
    }


    if (!account) {

      console.warn(
        'No existe una cuenta activa.'
      );

      return;
    }


    this.correoUsuario =
      account.username || '';


    this.authService
      .acquireTokenSilent({

        account,

        scopes: [
          environment.rutaExpressScope
        ],

        redirectUri:
          environment.azure.silentRedirectUri

      })
      .subscribe({

        next: (result) => {

          const claims =
            this.decodeJwtPayload(
              result.accessToken
            );

          this.userRoles =
            claims?.roles ?? [];

          console.log(
            'Roles en Envíos:',
            this.userRoles
          );

          console.log(
            'Correo usuario:',
            this.correoUsuario
          );

          if (this.esCliente()) {

            this.nuevoEnvio.correoRemitente =
              this.correoUsuario;
          }

          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'Error obteniendo roles:',
            error
          );

          this.userRoles = [];

          this.cdr.detectChanges();
        }

      });
  }


  puedeCambiarEstado(): boolean {

    return (
      this.userRoles.includes('Admin') ||
      this.userRoles.includes('Despachador')
    );
  }


  puedeCrearEnvio(): boolean {

    return (
      this.userRoles.includes('Admin') ||
      this.userRoles.includes('Despachador') ||
      this.userRoles.includes('Cliente')
    );
  }


  esCliente(): boolean {

    return this.userRoles.includes(
      'Cliente'
    );
  }


  private decodeJwtPayload(
    token: string
  ): any {

    try {

      const payload =
        token.split('.')[1];

      if (!payload) {
        return {};
      }

      const base64 =
        payload
          .replace(/-/g, '+')
          .replace(/_/g, '/');

      const padded =
        base64.padEnd(
          base64.length +
          (4 - base64.length % 4) % 4,
          '='
        );

      return JSON.parse(
        atob(padded)
      );

    } catch (error) {

      console.error(
        'No se pudo decodificar el access token:',
        error
      );

      return {};
    }
  }


  // =====================================================
  // CARGAR ENVÍOS
  // =====================================================

  cargarEnvios(): void {

    this.loading = true;

    this.shipmentService
      .getShipments()
      .subscribe({

        next: (data) => {

          console.log(
            'Envíos recibidos:',
            data
          );

          this.shipments = data;
          this.loading = false;
          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'Error cargando los envíos:',
            error
          );

          this.errorMessage =
            'No se pudieron cargar los envíos.';

          this.loading = false;
          this.cdr.detectChanges();
        }

      });
  }


  // =====================================================
  // CARGAR CATÁLOGO
  // =====================================================

  cargarServicios(): void {

    this.catalogService
      .getServices()
      .subscribe({

        next: (data) => {

          console.log(
            'Servicios recibidos:',
            data
          );

          this.services = data;
          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'Error cargando servicios:',
            error
          );

          this.cdr.detectChanges();
        }

      });
  }


  // =====================================================
  // SERVICIO SELECCIONADO
  // =====================================================

  obtenerServicioSeleccionado(): any | null {

    if (!this.nuevoEnvio.servicio) {
      return null;
    }

    return this.services.find(
      service =>
        service.nombre ===
        this.nuevoEnvio.servicio
    ) ?? null;
  }


  obtenerTarifaBase(): number {

    const servicio =
      this.obtenerServicioSeleccionado();

    if (!servicio) {
      return 0;
    }

    const tarifa =
      Number(
        servicio.tarifaBase
      );

    if (
      Number.isNaN(tarifa) ||
      tarifa < 0
    ) {
      return 0;
    }

    return tarifa;
  }


  // =====================================================
  // TIPO DE PAQUETE
  // =====================================================

  esDocumento(): boolean {

    return (
      this.nuevoEnvio.tipoPaquete ===
      'DOCUMENTO'
    );
  }


  alCambiarTipoPaquete(): void {

    /*
     * Un documento no necesita medidas,
     * peso ni tratamiento de fragilidad.
     */
    if (this.esDocumento()) {

      this.nuevoEnvio.alto = null;
      this.nuevoEnvio.ancho = null;
      this.nuevoEnvio.largo = null;
      this.nuevoEnvio.peso = null;
      this.nuevoEnvio.fragil = false;
    }
  }


  // =====================================================
  // COTIZACIÓN
  // =====================================================

  calcularPesoVolumetrico(): number {

    if (this.esDocumento()) {
      return 0;
    }

    const alto =
      Number(
        this.nuevoEnvio.alto
      );

    const ancho =
      Number(
        this.nuevoEnvio.ancho
      );

    const largo =
      Number(
        this.nuevoEnvio.largo
      );

    if (
      !alto ||
      !ancho ||
      !largo ||
      alto <= 0 ||
      ancho <= 0 ||
      largo <= 0
    ) {
      return 0;
    }

    return this.redondear(
      (
        alto *
        ancho *
        largo
      ) /
      this.FACTOR_VOLUMETRICO
    );
  }


  calcularPesoCobrable(): number {

    if (this.esDocumento()) {
      return 0;
    }

    const pesoReal =
      Number(
        this.nuevoEnvio.peso
      ) || 0;

    const pesoVolumetrico =
      this.calcularPesoVolumetrico();

    return this.redondear(
      Math.max(
        pesoReal,
        pesoVolumetrico
      )
    );
  }


  calcularRecargoPeso(): number {

    if (this.esDocumento()) {
      return 0;
    }

    const pesoCobrable =
      this.calcularPesoCobrable();

    if (
      pesoCobrable <=
      this.PESO_INCLUIDO
    ) {
      return 0;
    }

    return this.redondear(
      (
        pesoCobrable -
        this.PESO_INCLUIDO
      ) *
      this.PRECIO_KILO_EXTRA
    );
  }


  calcularRecargoFragilidad(): number {

    if (
      this.esDocumento() ||
      !this.nuevoEnvio.fragil
    ) {
      return 0;
    }

    return this.redondear(
      this.obtenerTarifaBase() *
      this.PORCENTAJE_FRAGIL
    );
  }


  calcularPrecioEstimado(): number {

    const tarifaBase =
      this.obtenerTarifaBase();

    const recargoPeso =
      this.calcularRecargoPeso();

    const recargoFragilidad =
      this.calcularRecargoFragilidad();

    return this.redondear(
      tarifaBase +
      recargoPeso +
      recargoFragilidad
    );
  }


  cotizacionDisponible(): boolean {

    if (
      !this.nuevoEnvio.servicio ||
      !this.nuevoEnvio.tipoPaquete
    ) {
      return false;
    }

    /*
     * Para documentos basta con elegir
     * servicio + tipo de envío.
     */
    if (this.esDocumento()) {
      return true;
    }

    return (
      Number(
        this.nuevoEnvio.alto
      ) > 0 &&

      Number(
        this.nuevoEnvio.ancho
      ) > 0 &&

      Number(
        this.nuevoEnvio.largo
      ) > 0 &&

      Number(
        this.nuevoEnvio.peso
      ) > 0
    );
  }


  private redondear(
    valor: number
  ): number {

    return Math.round(
      (
        valor +
        Number.EPSILON
      ) *
      100
    ) / 100;
  }


  formatearPrecio(
    valor: number
  ): string {

    return new Intl.NumberFormat(
      'es-CL',
      {
        style: 'currency',
        currency: 'CLP',
        maximumFractionDigits: 0
      }
    ).format(
      valor || 0
    );
  }


  formatearPeso(
    valor: number
  ): string {

    return new Intl.NumberFormat(
      'es-CL',
      {
        minimumFractionDigits: 0,
        maximumFractionDigits: 2
      }
    ).format(
      valor || 0
    );
  }


  // =====================================================
  // CREAR ENVÍO
  // =====================================================

  crearEnvio(): void {

    this.errorMessage = '';
    this.successMessage = '';

    if (!this.puedeCrearEnvio()) {

      this.errorMessage =
        'No tienes permisos para crear envíos.';

      return;
    }

    if (this.esCliente()) {

      this.nuevoEnvio.correoRemitente =
        this.correoUsuario;
    }


    // ===================================================
    // VALIDACIÓN DATOS GENERALES
    // ===================================================

    if (
      !this.nuevoEnvio.correoRemitente ||
      !this.nuevoEnvio.nombreDestinatario ||
      !this.nuevoEnvio.correoDestinatario ||
      !this.nuevoEnvio.direccionOrigen ||
      !this.nuevoEnvio.direccionDestino ||
      !this.nuevoEnvio.servicio
    ) {

      this.errorMessage =
        'Debes completar los datos generales del envío.';

      this.cdr.detectChanges();
      return;
    }


    // ===================================================
    // VALIDACIÓN PAQUETE
    // ===================================================

    if (!this.nuevoEnvio.tipoPaquete) {

      this.errorMessage =
        'Debes seleccionar un tipo de paquete.';

      this.cdr.detectChanges();
      return;
    }


    /*
     * Peso y dimensiones solamente
     * son obligatorios para paquetes físicos.
     */
    if (!this.esDocumento()) {

      if (
        Number(
          this.nuevoEnvio.alto
        ) <= 0 ||

        Number(
          this.nuevoEnvio.ancho
        ) <= 0 ||

        Number(
          this.nuevoEnvio.largo
        ) <= 0
      ) {

        this.errorMessage =
          'Las dimensiones del paquete deben ser mayores que 0.';

        this.cdr.detectChanges();
        return;
      }


      if (
        Number(
          this.nuevoEnvio.peso
        ) <= 0
      ) {

        this.errorMessage =
          'El peso del paquete debe ser mayor que 0.';

        this.cdr.detectChanges();
        return;
      }
    }


    /*
     * Valor declarado opcional.
     * Solamente rechazamos números negativos.
     */
    if (
      this.nuevoEnvio.valorDeclarado !== null &&
      this.nuevoEnvio.valorDeclarado !== '' &&
      Number(
        this.nuevoEnvio.valorDeclarado
      ) < 0
    ) {

      this.errorMessage =
        'El valor declarado no puede ser negativo.';

      this.cdr.detectChanges();
      return;
    }


    if (
      this.nuevoEnvio.observaciones &&
      this.nuevoEnvio.observaciones.length > 1000
    ) {

      this.errorMessage =
        'Las observaciones no pueden superar los 1000 caracteres.';

      this.cdr.detectChanges();
      return;
    }


    if (this.creandoEnvio) {
      return;
    }

    this.creandoEnvio = true;


    /*
     * No enviamos la cotización calculada en Angular.
     * El backend vuelve a calcularla de forma segura.
     */
    const request = {

      correoRemitente:
        this.nuevoEnvio.correoRemitente,

      nombreDestinatario:
        this.nuevoEnvio.nombreDestinatario,

      correoDestinatario:
        this.nuevoEnvio.correoDestinatario,

      direccionOrigen:
        this.nuevoEnvio.direccionOrigen,

      direccionDestino:
        this.nuevoEnvio.direccionDestino,

      servicio:
        this.nuevoEnvio.servicio,

      tipoPaquete:
        this.nuevoEnvio.tipoPaquete,

      alto:
        this.esDocumento()
          ? null
          : Number(
              this.nuevoEnvio.alto
            ),

      ancho:
        this.esDocumento()
          ? null
          : Number(
              this.nuevoEnvio.ancho
            ),

      largo:
        this.esDocumento()
          ? null
          : Number(
              this.nuevoEnvio.largo
            ),

      peso:
        this.esDocumento()
          ? null
          : Number(
              this.nuevoEnvio.peso
            ),

      valorDeclarado:
        this.nuevoEnvio.valorDeclarado === null ||
        this.nuevoEnvio.valorDeclarado === ''
          ? null
          : Number(
              this.nuevoEnvio.valorDeclarado
            ),

      fragil:
        this.esDocumento()
          ? false
          : !!this.nuevoEnvio.fragil,

      observaciones:
        this.nuevoEnvio.observaciones
          ?.trim() || ''
    };


    console.log(
      'Creando envío:',
      request
    );


    console.log(
      'Cotización visual:',
      {
        tarifaBase:
          this.obtenerTarifaBase(),

        pesoVolumetrico:
          this.calcularPesoVolumetrico(),

        pesoCobrable:
          this.calcularPesoCobrable(),

        recargoPeso:
          this.calcularRecargoPeso(),

        recargoFragilidad:
          this.calcularRecargoFragilidad(),

        precioEstimado:
          this.calcularPrecioEstimado()
      }
    );


    this.shipmentService
      .createShipment(
        request
      )
      .subscribe({

        next: (response) => {

          console.log(
            'Envío creado:',
            response
          );

          this.successMessage =
            'Envío creado correctamente. Código: ' +
            (
              response?.codigoSeguimiento ||
              response?.id ||
              ''
            );

          this.limpiarFormulario();
          this.creandoEnvio = false;
          this.cargarEnvios();
          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'Error creando envío:',
            error
          );

          this.errorMessage =
            error?.error?.message ||
            error?.error?.error ||
            'No se pudo crear el envío.';

          this.creandoEnvio = false;
          this.cdr.detectChanges();
        }

      });
  }


  // =====================================================
  // LIMPIAR FORMULARIO
  // =====================================================

  limpiarFormulario(): void {

    this.nuevoEnvio = {

      correoRemitente:
        this.esCliente()
          ? this.correoUsuario
          : '',

      nombreDestinatario: '',
      correoDestinatario: '',
      direccionOrigen: '',
      direccionDestino: '',
      servicio: '',

      tipoPaquete: '',
      alto: null,
      ancho: null,
      largo: null,
      peso: null,
      valorDeclarado: null,
      fragil: false,
      observaciones: ''
    };
  }


  // =====================================================
  // CAMBIAR ESTADO
  // =====================================================

  cambiarEstado(
    id: number,
    estado: string
  ): void {

    if (!this.puedeCambiarEstado()) {

      this.errorMessage =
        'No tienes permisos para cambiar el estado de un envío.';

      this.cdr.detectChanges();
      return;
    }


    if (
      this.actualizandoEstadoId !== null
    ) {
      return;
    }


    this.actualizandoEstadoId =
      id;

    this.errorMessage = '';
    this.successMessage = '';


    this.shipmentService
      .changeStatus(
        id,
        estado
      )
      .subscribe({

        next: (response) => {

          console.log(
            'Estado actualizado:',
            response
          );

          this.successMessage =
            `Estado actualizado a ${estado}.`;

          this.actualizandoEstadoId =
            null;

          this.cargarEnvios();
          this.cargarServicios();
          this.cdr.detectChanges();
        },

        error: (error) => {

          console.error(
            'Error cambiando estado:',
            error
          );

          this.errorMessage =
            'No se pudo cambiar el estado del envío.';

          this.actualizandoEstadoId =
            null;

          this.cdr.detectChanges();
        }

      });
  }
}