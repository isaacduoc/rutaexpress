import {
  ComponentFixture,
  TestBed
} from '@angular/core/testing';

import { of } from 'rxjs';

import { ShipmentsComponent } from './shipments';

import { ShipmentService } from '../../services/shipment';
import { CatalogService } from '../../services/catalog';

import { MsalService } from '@azure/msal-angular';


describe('ShipmentsComponent', () => {

  let component: ShipmentsComponent;

  let fixture:
    ComponentFixture<ShipmentsComponent>;


  const shipmentServiceMock = {

    getShipments: () =>
      of([]),

    createShipment: () =>
      of({}),

    changeStatus: () =>
      of({})

  };


  const catalogServiceMock = {

    getServices: () =>
      of([])

  };


  const msalServiceMock = {

    instance: {

      getActiveAccount: () =>
        null,

      getAllAccounts: () =>
        [],

      setActiveAccount: () => {}

    },

    acquireTokenSilent: () =>
      of({
        accessToken: ''
      })

  };


  beforeEach(async () => {

    await TestBed.configureTestingModule({

      imports: [
        ShipmentsComponent
      ],

      providers: [

        {
          provide: ShipmentService,
          useValue: shipmentServiceMock
        },

        {
          provide: CatalogService,
          useValue: catalogServiceMock
        },

        {
          provide: MsalService,
          useValue: msalServiceMock
        }

      ]

    }).compileComponents();


    fixture =
      TestBed.createComponent(
        ShipmentsComponent
      );


    component =
      fixture.componentInstance;


    fixture.detectChanges();

  });


  it(
    'should create',
    () => {

      expect(
        component
      ).toBeTruthy();

    }
  );

});