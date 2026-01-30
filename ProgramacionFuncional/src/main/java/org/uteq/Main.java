package org.uteq;


public class Main {
    public static void main(String[] args) {
        Operaciones suma = new Operaciones(){
            @Override
            public int sumar(int a, int b) {
                return a + b;
            }
        };

        Operaciones resta = new Operaciones(){
            @Override
            public int restar(int a, int b) {
                return a - b;
            }
        };

        Operaciones multiplicar = new Operaciones(){
            @Override
            public int multiplicar(int a, int b) {
                return a * b;
            }
        };
    }
}
