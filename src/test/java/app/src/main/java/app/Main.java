package app.src.main.java.app;

public class Main {
    public String getStatus() {
        return "Backend is running";
    }

    public static void main(String[] args) {
        System.out.println(new Main().getStatus());
    }
}
