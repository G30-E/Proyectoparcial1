package ejercicios;

public class ejercicio1 {

    public static int calcularPuntaje(int[] numeros) {
        int puntajeTotal = 0;
        
        for (int numero : numeros) {
            if (numero == 5) {
                puntajeTotal += 5;
            } else if (numero % 2 == 0) {
                puntajeTotal += 1;
            } else {
                puntajeTotal += 3;
            }
        }

        return puntajeTotal;
    }

    public static void main(String[] args) {
        int[] arreglo1 = {1, 2, 3, 4, 5};
        int[] arreglo2 = {17, 19, 21};
        int[] arreglo3 = {5, 5, 5};

        System.out.println("Resultado 1: " + calcularPuntaje(arreglo1));
        System.out.println("Resultado 2: " + calcularPuntaje(arreglo2));
        System.out.println("Resultado 3: " + calcularPuntaje(arreglo3));

        System.out.println("Complejidad temporal: O(n)");
        System.out.println("Complejidad espacial: O(1)");
    }
}


//Justificación breve:
//Es O(n) en tiempo porque solo se hace una pasada sobre el arreglo.
//Es O(1) en espacio porque solo se usan variables simples adicionales.