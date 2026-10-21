package cmsc13.ParadigmBreach.ui;

public enum Screen {
    DESKTOP ("/fxml_files/desktop.fxml",      "ParadigmBreach", 1000, 600, true),
    TERMINAL("/fxml_files/command-line.fxml", "Terminal",        975, 400, false),
    TUTORIALREADME("/fxml_files/tutorial-readme-md.fxml", "README.md (Tutorial)", 650, 550, true),
    SPLASHART("/fxml_files/splash-art.fxml", "Splash Art", 800, 400, true);

    private final String fxmlPath, title;
    private final double w, h;
    private final boolean singleInstance;

    Screen(String fxmlPath, String title, double w, double h, boolean singleInstance) {
        this.fxmlPath = fxmlPath;
        this.title = title;
        this.w = w;
        this.h = h;
        this.singleInstance = singleInstance;
        // testign
    }

    public String getFxmlPath() { return fxmlPath; }
    public String getTitle() { return title; }
    public double getWidth() { return w; }
    public double getHeight() { return h; }
    public boolean isSingleInstance() { return singleInstance; }
}