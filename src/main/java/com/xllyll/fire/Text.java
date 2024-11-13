package com.xllyll.fire;

import java.util.Random;

public class Text {


    public static double getRandomDouble() {
        Random random = new Random();
        return random.nextDouble()+random.nextDouble()+random.nextDouble();
    }

    public static void main(String[] args) {

        double randomDouble = getRandomDouble();
        System.out.println("Random double: " + randomDouble);
        randomDouble = getRandomDouble();
        System.out.println("Random double: " + randomDouble);
        randomDouble = getRandomDouble();
        System.out.println("Random double: " + randomDouble);
        randomDouble = getRandomDouble();
        System.out.println("Random double: " + randomDouble);
    }

}
