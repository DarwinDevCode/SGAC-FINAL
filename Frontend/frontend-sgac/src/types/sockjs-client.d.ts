declare module 'sockjs-client' {
  export interface SockJSClass {
    new (url: string, _reserved?: any, options?: any): WebSocket;
  }

  const SockJS: SockJSClass;
  export default SockJS;
}
