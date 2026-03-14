package ejerciciosparcial1;

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
