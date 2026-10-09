export const environment = {

  production: false,

  // BFF
  apiUrl: 'https://45owu90zfg.execute-api.us-east-1.amazonaws.com',

  azure: {

    // AplicacionCloudNative
    clientId: '67000e08-055c-441d-a5bb-fabf887283e0',

    authority:
      'https://login.microsoftonline.com/d4e45c2c-5598-4953-9517-f1c5f09d4eb1',

    redirectUri: window.location.origin

  },

  // Scope expuesto por RutaExpress API
  rutaExpressScope:
    'api://c515cc67-aed4-4210-89f0-433271797592/access_as_user'

};