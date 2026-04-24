package backend.observer;

public class PreviewObserver implements Observer {

    @Override
    public void update(String message) {
        System.out.println("Preview Updated: " + message);
    }
}