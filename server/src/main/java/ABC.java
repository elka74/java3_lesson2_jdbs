/* Задача 1 к 4 уроку
Создать три потока, каждый из которых выводит определенную букву (A, B и C) 5 раз (порядок – ABСABСABС).
 Используйте wait/notify/notifyAll.*/

public class ABC {
    static Object monitor = new Object();
    public static volatile int ch = 1;
    static final int rep = 5;

    public static void main(String[] args) {
        new Thread(() -> {
            try {
                for (int i = 0; i < rep; i++) {
                    synchronized (monitor) {
                        while (ch != 1) {
                            monitor.wait();
                        }
                        System.out.print("A");
                        ch = 2;
                        monitor.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }).start();


        new Thread(() -> {
            try {
                for (int i = 0; i < rep; i++) {
                    synchronized (monitor) {
                        while (ch != 2) {
                            monitor.wait();
                        }
                        System.out.print("B");
                        ch = 3;
                        monitor.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }).start();


        new Thread(() -> {
            try {
                for (int i = 0; i < rep; i++) {
                    synchronized (monitor) {
                        while (ch != 3) {
                            monitor.wait();
                        }
                        System.out.print("C ");
                        ch = 1;
                        monitor.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }).start();

    }
}
