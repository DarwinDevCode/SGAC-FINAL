//import java.util.function;


import static java.lang.Integer.parseInt;//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
void main() {

     Operaciones suma = new Operaciones(){
        @Override
        public float Operacion(int a, int b) {
            return a + b;
        }
    };

     Operaciones multiplicacion = new Operaciones(){
         @Override
         public float Operacion(int a, int b) {
             return a * b;
         }
     };

    Operaciones division = new Operaciones(){
        @Override
        public float Operacion(int a, int b) {
            return (float)(a / b);
        }
    };

    System.out.println(suma.Operacion(10, 20));
    System.out.println(multiplicacion.Operacion(10, 20));
    System.out.println( (float)division.Operacion(20,10));

    Operaciones resta = (a, b) ->  a - b;
    System.out.println(resta.Operacion(10, 20));

    Operaciones sum = this::suma;
    System.out.println(sum.Operacion(10, 20));








//
//
//    Operaciones multiplicacion = new Operaciones() {
//        @Override
//        public double Operacion(double a, double b) {
//            return a * b;
//        }
//    };
//
//
//    System.out.println(suma.Operacion(10, 30.9));
//    System.out.println(suma.Operacion(11, 30.9));
//
//    System.out.println(multiplicacion.Operacion(10, 30.9));
//
//    int suma1 = Arrays.stream(new int[]{1, 2, 3, 4, 5}).max().getAsInt();
//    System.out.println(suma1);
//
//
//    MiFuncion f = () -> System.out.println("Mi Función");
//    f.ejecutar();
//
//    Runnable accion = () -> System.out.println("Inicio del programa");
//    accion.run();








//    OperacionesBasicas suma = new  OperacionesBasicas() {
//        @Override
//        public double Operacion(double a, double b) {
//            return a + b;
//        }
//    };
//
//    OperacionesBasicas resta = new  OperacionesBasicas() {
//        @Override
//        public double Operacion(double a, double b) {
//            return a - b;
//        }
//    };
//
//    OperacionesBasicas division = new  OperacionesBasicas() {
//        @Override
//        public double Operacion(double a, double b) {
//            return a / b;
//        }
//    };
//
//    OperacionesBasicas multiplicacion = new  OperacionesBasicas() {
//        @Override
//        public double Operacion(double a, double b) {
//            return a * b;
//        }
//    };

//    Acciones miAccion = () -> System.out.println("Estoy bailando");
//    miAccion.accion();
//
//    OperacionesBasicas suma = (a,b) -> {
//        a += 2*a;
//        b += a + 2*b;
//        return a + b;};
//    OperacionesBasicas resta = (a,b) -> a + b;
//
//    System.out.println(suma.Operacion(10, 30));
//    System.out.println(resta.Operacion(10, 30));



//    Consumer<String> saludo = (texto) -> System.out.println(texto);
//    saludo.accept("Hola, ¿cómo estás, Dr. Gleiston?");
//
//    Consumer<String> saludoSimplificado = System.out::println;
//    saludo.accept("Hola, ¿cómo estás, Dr. Gleiston?");
//
//    Consumer<Integer> suma = (10) ->  System.out.println(10 + 2);
//
//    System.out.println(suma(10, 20));

    //El supplier no recibe parámetros, solo devuelve
    //El consumer recibe parámetros y los consume

    Supplier<String> nombre = () -> "Darwin";
    System.out.println(nombre.get());

    Consumer<String> saludo = nombrePersona -> System.out.println("Hola, " + nombrePersona);
    saludo.accept(nombre.get());

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

    Supplier<List<String>> generarNombres = () -> List.of("Ana", "Luis", "Pedro");
    System.out.println(generarNombres.get());

    Consumer<List<String>> imprimirNombres = nombres -> System.out.println("Hola a todos " + nombres);
    imprimirNombres.accept(generarNombres.get());

    // /////////////////////////////////////////////////////////////////////////////////////////////

    Consumer<Usuario> validarEdad = usuarioPersona -> {
        if (usuarioPersona.getEdad() >= 18) System.out.println(usuarioPersona.getNombre() + " es mayor de edad, tiene " + usuarioPersona.getEdad() + " años");
        else System.out.println(usuarioPersona.getNombre() + " es menor de edad, tiene " + usuarioPersona.getEdad() + " años");
    };
    validarEdad.accept(usuario.get());

    Usuario user = new Usuario("Darwin", 20);

    CredencialesUsuario credencialesUsuario = new CredencialesUsuario("dsanchezv", "1234");


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

int suma (int a, int b) {
    return a + b;
}