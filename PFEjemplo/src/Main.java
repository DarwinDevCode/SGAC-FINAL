void main() {

     Operaciones suma = new Operaciones(){
        @Override
        public int Operacion(int a, int b) {
            return a + b;
        }
    };

     Operaciones multiplicacion = new Operaciones(){
         @Override
         public int Operacion(int a, int b) {
             return a * b;
         }
     };


    Operaciones division = new Operaciones(){
        @Override
        public int Operacion(int a, int b) {
            return a / b;
        }
    };

    System.out.println(suma.Operacion(10, 20));
    System.out.println(multiplicacion.Operacion(10, 20));
    System.out.println(division.Operacion(20,10));

    // /////////////////////////////////////////////////////////////////////////////////////////////

    Operaciones resta = (a, b) ->  a - b;
    System.out.println(resta.Operacion(10, 20));

    // /////////////////////////////////////////////////////////////////////////////////////////////

    Consumer<String> saludo = texto -> System.out.println(texto);
    saludo.accept("Hola, ¿cómo estás, Dr. Gleiston?");

    Consumer<Integer> incrementoC = valor ->  System.out.println(++valor);
    incrementoC.accept(999);

    // /////////////////////////////////////////////////////////////////////////////////////////////

    Supplier<String> nombre = () -> "Darwin";
    System.out.println(nombre.get());

    Consumer<String> saludoS = nombrePersona -> System.out.println("Hola, " + nombrePersona);
    saludoS.accept(nombre.get());

    // /////////////////////////////////////////////////////////////////////////////////////////////

    Supplier<Integer> numero = () -> new Random().nextInt(100000);

    Consumer<Integer> duplicaNumero = numeroADuplicar -> System.out.println(2 * numeroADuplicar);
    duplicaNumero.accept(numero.get());

    // /////////////////////////////////////////////////////////////////////////////////////////////

    Supplier<Usuario> usuario = () -> new Usuario("Darwin", 20);
    System.out.println(usuario.get().getNombre() +" "+  usuario.get().getEdad());

    Consumer<Usuario> imprimirUsuario = usuarioPersona
            -> System.out.println("Tu nombre es " + usuarioPersona.getNombre() + " y tu edad es " + usuarioPersona.getEdad() + " años");
    imprimirUsuario.accept(usuario.get());

    // /////////////////////////////////////////////////////////////////////////////////////////////

    Supplier<List<String>> listaNombres = () -> List.of("Ana", "Luis", "Pedro");
    System.out.println(listaNombres.get());

    Consumer<List<String>> imprimirNombres = nombres -> System.out.println("Hola a todos " + nombres);
    imprimirNombres.accept(listaNombres.get());

    // /////////////////////////////////////////////////////////////////////////////////////////////

    Consumer<Usuario> validarEdad = usuarioPersona -> {
        if (usuarioPersona.getEdad() >= 18) System.out.println(usuarioPersona.getNombre() + " es mayor de edad, tiene " + usuarioPersona.getEdad() + " años");
        else System.out.println(usuarioPersona.getNombre() + " es menor de edad, tiene " + usuarioPersona.getEdad() + " años");
    };
    validarEdad.accept(usuario.get());



    Usuario user = new Usuario("Darwin", 20);

    CredencialesUsuario credencialesUsuario = new CredencialesUsuario("dsanchezv", "1234567890");

    Supplier<CredencialesUsuario> credencialAcceso = () -> credencialesUsuario;
    System.out.println(credencialAcceso.get().getNombreUsuario() + "  " + credencialAcceso.get().getContraseniaUsuario());


    Consumer<CredencialesUsuario> validarCredencial = credencialUsuario -> {
        if(credencialUsuario.getNombreUsuario() == "dsanchezv" && credencialUsuario.getContraseniaUsuario() == "1234")
            System.out.println("Credenciales correctas");
        else
            System.out.println("Credenciales incorrecas");
    };

    validarCredencial.accept(credencialAcceso.get());


}
