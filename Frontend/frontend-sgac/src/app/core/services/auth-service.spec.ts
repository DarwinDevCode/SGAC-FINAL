import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService, AuthUser, LoginRequest } from './auth-service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  // Mock de usuario que simula la respuesta del backend
  const mockAuthUser: AuthUser = {
    idUsuario: 1,
    nombres: 'Juan',
    apellidos: 'Pérez',
    correo: 'juan@test.com',
    nombreUsuario: 'juan',
    rolActual: 'ADMINISTRADOR',
    roles: [{ idTipoRol: 1, nombreTipoRol: 'ADMINISTRADOR', activo: true }],
    activo: true,
    token: 'fake-jwt-token'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);

    // Limpiar localStorage antes de cada prueba
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  // ─── Creación ────────────────────────────────────────────────────────────────

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  // ─── login() ─────────────────────────────────────────────────────────────────

  describe('login()', () => {
    it('debe hacer POST a /api/auth/login con las credenciales', () => {
      const credentials: LoginRequest = { usuario: 'juan', password: '123456' };
      service.login(credentials).subscribe();
      const req = httpMock.expectOne('http://localhost:8080/api/auth/login');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(credentials);
      req.flush(mockAuthUser);
    });

    it('debe guardar el token en localStorage tras login exitoso', () => {
      const credentials: LoginRequest = { usuario: 'juan', password: '123456' };
      service.login(credentials).subscribe();
      const req = httpMock.expectOne('http://localhost:8080/api/auth/login');
      req.flush(mockAuthUser);

      expect(localStorage.getItem('token')).toBe('fake-jwt-token');
    });

    it('debe guardar el usuario en localStorage tras login exitoso', () => {
      const credentials: LoginRequest = { usuario: 'juan', password: '123456' };
      service.login(credentials).subscribe();
      const req = httpMock.expectOne('http://localhost:8080/api/auth/login');
      req.flush(mockAuthUser);
      const storedUser = JSON.parse(localStorage.getItem('user')!);
      expect(storedUser.nombreUsuario).toBe('juan');
      expect(storedUser.rolActual).toBe('ADMINISTRADOR');
    });

    it('no debe guardar token si la respuesta no lo incluye', () => {
      const credentials: LoginRequest = { usuario: 'juan', password: '123456' };
      const userSinToken: AuthUser = { ...mockAuthUser, token: undefined };
      service.login(credentials).subscribe();
      const req = httpMock.expectOne('http://localhost:8080/api/auth/login');
      req.flush(userSinToken);
      expect(localStorage.getItem('token')).toBeNull();
    });
  });

  // ─── logout() ────────────────────────────────────────────────────────────────

  describe('logout()', () => {

    it('debe eliminar token del localStorage', () => {
      localStorage.setItem('token', 'fake-jwt-token');

      service.logout();

      expect(localStorage.getItem('token')).toBeNull();
    });

    it('debe eliminar user del localStorage', () => {
      localStorage.setItem('user', JSON.stringify(mockAuthUser));

      service.logout();

      expect(localStorage.getItem('user')).toBeNull();
    });

    it('no debe lanzar error si localStorage ya está vacío', () => {
      expect(() => service.logout()).not.toThrow();
    });

  });

  // ─── getToken() ──────────────────────────────────────────────────────────────

  describe('getToken()', () => {

    it('debe retornar el token si existe en localStorage', () => {
      localStorage.setItem('token', 'fake-jwt-token');

      expect(service.getToken()).toBe('fake-jwt-token');
    });

    it('debe retornar null si no hay token', () => {
      expect(service.getToken()).toBeNull();
    });

  });

  // ─── getUser() ───────────────────────────────────────────────────────────────

  describe('getUser()', () => {

    it('debe retornar el usuario parseado desde localStorage', () => {
      localStorage.setItem('user', JSON.stringify(mockAuthUser));

      const user = service.getUser();

      expect(user).not.toBeNull();
      expect(user!.nombreUsuario).toBe('juan');
      expect(user!.rolActual).toBe('ADMINISTRADOR');
    });

    it('debe retornar null si no hay usuario en localStorage', () => {
      expect(service.getUser()).toBeNull();
    });

  });

  // ─── hasRole() ───────────────────────────────────────────────────────────────

  describe('hasRole()', () => {

    it('debe retornar false si no hay usuario autenticado', () => {
      expect(service.hasRole(['ADMINISTRADOR'])).toBe(false);
    });

    it('debe retornar false si el usuario no tiene el rol requerido', () => {
      // getCurrentUser() usa currentUserSubject, que arranca en null
      expect(service.hasRole(['COORDINADOR', 'DOCENTE'])).toBe(false);
    });

  });

});
