import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { UsuarioService } from './usuario-service';
import { UsuarioDTO } from '../dto/usuario';

describe('UsuarioService', () => {
  let service: UsuarioService;
  let httpMock: HttpTestingController;

  const BASE = 'http://localhost:8080/api/auth';

  const mockUsuario: UsuarioDTO = {
    idUsuario: 1,
    nombres: 'Juan',
    apellidos: 'Pérez',
    cedula: '1234567890',
    correo: 'juan@test.com',
    nombreUsuario: 'juan',
    activo: true,
    roles: [{ idTipoRol: 1, nombreTipoRol: 'ESTUDIANTE', activo: true }]
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [UsuarioService]
    });

    service = TestBed.inject(UsuarioService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  // ─── Creación ──────────────────────────────────────────────────────────────

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  // ─── listarUsuarios() ──────────────────────────────────────────────────────

  describe('listarUsuarios()', () => {

    it('debe hacer GET a /api/auth', () => {
      service.listarUsuarios().subscribe();

      const req = httpMock.expectOne(BASE);
      expect(req.request.method).toBe('GET');
      req.flush([mockUsuario]);
    });

    it('debe retornar lista de usuarios', () => {
      service.listarUsuarios().subscribe(data => {
        expect(data.length).toBe(1);
        expect(data[0].nombreUsuario).toBe('juan');
      });

      httpMock.expectOne(BASE).flush([mockUsuario]);
    });

    it('debe retornar lista vacía si no hay usuarios', () => {
      service.listarUsuarios().subscribe(data => {
        expect(data.length).toBe(0);
      });

      httpMock.expectOne(BASE).flush([]);
    });

  });

  // ─── crear() por rol ───────────────────────────────────────────────────────

  describe('crear() - ESTUDIANTE', () => {

    const nuevoEstudiante: UsuarioDTO = {
      nombres: 'Ana', apellidos: 'López', cedula: '0987654321',
      correo: 'ana@test.com', nombreUsuario: 'ana', password: 'pass123',
      rolRegistro: 'ESTUDIANTE', idCarrera: 2, matricula: '2024-001', semestre: 3
    };

    it('debe hacer POST a /api/auth/registro-estudiante', () => {
      service.crear(nuevoEstudiante).subscribe();

      const req = httpMock.expectOne(`${BASE}/registro-estudiante`);
      expect(req.request.method).toBe('POST');
      req.flush(nuevoEstudiante);
    });

    it('debe enviar los campos correctos en el body', () => {
      service.crear(nuevoEstudiante).subscribe();

      const req = httpMock.expectOne(`${BASE}/registro-estudiante`);
      expect(req.request.body.idCarrera).toBe(2);
      expect(req.request.body.matricula).toBe('2024-001');
      expect(req.request.body.semestre).toBe(3);
      req.flush(nuevoEstudiante);
    });

  });

  describe('crear() - DOCENTE', () => {

    const nuevoDocente: UsuarioDTO = {
      nombres: 'Carlos', apellidos: 'Ruiz', cedula: '1111111111',
      correo: 'carlos@test.com', nombreUsuario: 'carlos', password: 'pass123',
      rolRegistro: 'DOCENTE'
    };

    it('debe hacer POST a /api/auth/registro-docente', () => {
      service.crear(nuevoDocente).subscribe();

      const req = httpMock.expectOne(`${BASE}/registro-docente`);
      expect(req.request.method).toBe('POST');
      req.flush(nuevoDocente);
    });

  });

  describe('crear() - COORDINADOR', () => {

    const nuevoCoordinador: UsuarioDTO = {
      nombres: 'María', apellidos: 'Torres', cedula: '2222222222',
      correo: 'maria@test.com', nombreUsuario: 'maria', password: 'pass123',
      rolRegistro: 'COORDINADOR', idCarrera: 1
    };

    it('debe hacer POST a /api/auth/registro-coordinador', () => {
      service.crear(nuevoCoordinador).subscribe();

      const req = httpMock.expectOne(`${BASE}/registro-coordinador`);
      expect(req.request.method).toBe('POST');
      req.flush(nuevoCoordinador);
    });

    it('debe enviar idCarrera en el body', () => {
      service.crear(nuevoCoordinador).subscribe();

      const req = httpMock.expectOne(`${BASE}/registro-coordinador`);
      expect(req.request.body.idCarrera).toBe(1);
      req.flush(nuevoCoordinador);
    });

  });

  describe('crear() - DECANO', () => {

    const nuevoDecano: UsuarioDTO = {
      nombres: 'Pedro', apellidos: 'Gómez', cedula: '3333333333',
      correo: 'pedro@test.com', nombreUsuario: 'pedro', password: 'pass123',
      rolRegistro: 'DECANO', idFacultad: 1
    };

    it('debe hacer POST a /api/auth/registro-decano', () => {
      service.crear(nuevoDecano).subscribe();

      const req = httpMock.expectOne(`${BASE}/registro-decano`);
      expect(req.request.method).toBe('POST');
      req.flush(nuevoDecano);
    });

    it('debe enviar idFacultad en el body', () => {
      service.crear(nuevoDecano).subscribe();

      const req = httpMock.expectOne(`${BASE}/registro-decano`);
      expect(req.request.body.idFacultad).toBe(1);
      req.flush(nuevoDecano);
    });

  });

  describe('crear() - ADMINISTRADOR', () => {

    const nuevoAdmin: UsuarioDTO = {
      nombres: 'Root', apellidos: 'Admin', cedula: '4444444444',
      correo: 'root@test.com', nombreUsuario: 'root', password: 'pass123',
      rolRegistro: 'ADMINISTRADOR'
    };

    it('debe hacer POST a /api/auth/registro-admin', () => {
      service.crear(nuevoAdmin).subscribe();

      const req = httpMock.expectOne(`${BASE}/registro-admin`);
      expect(req.request.method).toBe('POST');
      req.flush(nuevoAdmin);
    });

  });

  describe('crear() - AYUDANTE_CATEDRA', () => {

    const nuevoAyudante: UsuarioDTO = {
      nombres: 'Luis', apellidos: 'Mora', cedula: '5555555555',
      correo: 'luis@test.com', nombreUsuario: 'luis', password: 'pass123',
      rolRegistro: 'AYUDANTE_CATEDRA', horasAyudante: 20
    };

    it('debe hacer POST a /api/auth/registro-ayudante-directo', () => {
      service.crear(nuevoAyudante).subscribe();

      const req = httpMock.expectOne(`${BASE}/registro-ayudante-directo`);
      expect(req.request.method).toBe('POST');
      req.flush(nuevoAyudante);
    });

    it('debe enviar horasAyudante en el body', () => {
      service.crear(nuevoAyudante).subscribe();

      const req = httpMock.expectOne(`${BASE}/registro-ayudante-directo`);
      expect(req.request.body.horasAyudante).toBe(20);
      req.flush(nuevoAyudante);
    });

  });

  describe('crear() - rol inválido', () => {

    it('debe lanzar error si el rol no es soportado', () => {
      const usuarioRolInvalido: UsuarioDTO = {
        nombres: 'X', apellidos: 'Y', cedula: '0000000000',
        correo: 'x@test.com', nombreUsuario: 'xy',
        rolRegistro: 'ROL_INEXISTENTE'
      };

      expect(() => service.crear(usuarioRolInvalido).subscribe())
        .toThrowError('Rol no soportado: ROL_INEXISTENTE');
    });

  });

  // ─── cambiarEstado() ───────────────────────────────────────────────────────

  describe('cambiarEstado()', () => {

    it('debe hacer PATCH a /api/auth/:id/estado', () => {
      service.cambiarEstado(1).subscribe();

      const req = httpMock.expectOne(`${BASE}/1/estado`);
      expect(req.request.method).toBe('PATCH');
      req.flush(null);
    });

    it('debe usar el id correcto en la URL', () => {
      service.cambiarEstado(42).subscribe();

      const req = httpMock.expectOne(`${BASE}/42/estado`);
      expect(req.request.url).toContain('/42/estado');
      req.flush(null);
    });

  });

  // ─── cambiarEstadoRol() ────────────────────────────────────────────────────

  describe('cambiarEstadoRol()', () => {

    it('debe hacer PATCH a /api/auth/:idUsuario/roles/:idTipoRol/estado', () => {
      service.cambiarEstadoRol(1, 2).subscribe();

      const req = httpMock.expectOne(`${BASE}/1/roles/2/estado`);
      expect(req.request.method).toBe('PATCH');
      req.flush(null);
    });

    it('debe construir la URL con los ids correctos', () => {
      service.cambiarEstadoRol(10, 5).subscribe();

      const req = httpMock.expectOne(`${BASE}/10/roles/5/estado`);
      expect(req.request.url).toContain('/10/roles/5/estado');
      req.flush(null);
    });

  });

});
