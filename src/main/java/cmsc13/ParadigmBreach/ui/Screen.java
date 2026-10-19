package cmsc13.ParadigmBreach.ui;

public enum Screen {
    DESKTOP ("/fxml_files/desktop.fxml",      "ParadigmBreach", 1000, 600, true),
    TERMINAL("/fxml_files/command-line.fxml", "Terminal",        950, 400, false),
    READMETUTORIAL("/fxml_files/readme-md-tutorial.fxml", "README Tutorial", 600, 750, true),
    TUTORIALREADME("/fxml_files/tutorial-readme-md.fxml", "Tutorial README", 600, 400, true),
    SPLASHART("/fxml_files/splash-art.fxml", "Splash Art", 800, 400, true);

    private final String fxmlPath, title;
    private final double width, height;
    private final boolean singleInstance;

    Screen(String fxmlPath, String title, double width, double height, boolean singleInstance) {
        this.fxmlPath = fxmlPath;
        this.title = title;
        this.width = width;
        this.height = height;
        this.singleInstance = singleInstance;
        // testign
    }

    public String getFxmlPath() { return fxmlPath; }
    public String getTitle() { return title; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public boolean isSingleInstance() { return singleInstance; }
}