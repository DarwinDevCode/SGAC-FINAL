// ...existing code...

// Polyfill para librerías que asumen entorno Node (p.ej. sockjs-client)
// Sin afectar el bundle más de lo necesario.
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const _global: any = (typeof globalThis !== 'undefined' ? globalThis : window);
// eslint-disable-next-line @typescript-eslint/no-explicit-any
(_global as any).global = _global;

// ...existing code...

