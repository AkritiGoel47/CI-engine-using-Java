package org.openjfx;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.DirectoryChooser;

public class FXMLController {

    @FXML
    private ComboBox<String> languageComboBox;
    @FXML
    private TextArea outputTextArea;
    @FXML
    private Label outputLabel;
    @FXML
    private CheckBox testCheckBox;  // CheckBox to toggle test phase

    private File selectedDirectory;

    public void initialize() {
        languageComboBox.getItems().addAll(
            "Java (Maven)",
            "Java (Gradle)",
            "Python",
            "JavaScript",
            "Docker"
        );
        languageComboBox.setVisible(false); // Hide initially
    }

    // Handle project directory selection
    @FXML
    private void handleSelectProject() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Project Directory");
        File directory = directoryChooser.showDialog(outputTextArea.getScene().getWindow());

        if (directory != null) {
            selectedDirectory = directory;
            String detectedLanguage = detectLanguage(selectedDirectory);
            outputLabel.setText("Detected Language: " + detectedLanguage);
            if (detectedLanguage.equals("Unknown")) {
                languageComboBox.setVisible(true);
            } else {
                languageComboBox.setVisible(false);
            }
        } else {
            outputTextArea.appendText("No directory selected.\n");
        }
    }

    // Handle language selection if detected language is not found
    @FXML
    private void handleLanguageSelection() {
        String selectedLanguage = languageComboBox.getValue();
        if (selectedLanguage != null) {
            runBuildTool(selectedLanguage);
            if (testCheckBox.isSelected()) {
                runTests(selectedLanguage);
            }
        } else {
            outputTextArea.appendText("Please select a language from the dropdown.\n");
        }
    }

    // Detect the programming language from files in the project
    private String detectLanguage(File projectDir) {
        if (containsFile(projectDir, "pom.xml")) {
            return "Java (Maven)";
        } else if (containsFile(projectDir, "build.gradle")) {
            return "Java (Gradle)";
        } else if (containsFile(projectDir, "package.json")) {
            return "JavaScript (Node.js)";
        } else if (containsFile(projectDir, "requirements.txt")) {
            return "Python";
        } else if (containsFile(projectDir, "Dockerfile")) {
            return "Docker";
        }
        return "Unknown";
    }

    // Check if a specific file exists in the project directory
    private boolean containsFile(File projectDir, String fileName) {
        File[] files = projectDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().equals(fileName)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Run the corresponding build tool based on selected programming language
    private void runBuildTool(String selectedLanguage) {
        String command = null;
        switch (selectedLanguage) {
            case "Java (Maven)":
                command = "mvn clean install"; // Maven command to build the project
                break;
            case "Java (Gradle)":
                command = "./gradlew build"; // Gradle command to build the project
                break;
            case "Python":
                command = "python3 setup.py install"; // Python setup script
                break;
            case "JavaScript (Node.js)":
                command = "npm install"; // Node.js package install
                break;
            case "Docker":
                command = "docker build -t myimage ."; // Docker build command
                break;
            default:
                outputTextArea.appendText("Unsupported build tool or language.\n");
                return;
        }

        executeCommand(command);
    }

    // Run the test phase if applicable
    private void runTests(String selectedLanguage) {
        String testCommand = null;

        if (selectedLanguage.equals("Java (Maven)")) {
            testCommand = "mvn test"; // Maven command to run tests
        } else if (selectedLanguage.equals("Java (Gradle)")) {
            testCommand = "./gradlew test"; // Gradle command to run tests
        } else if (selectedLanguage.equals("Python")) {
            testCommand = "pytest"; // Python pytest command to run tests
        } else if (selectedLanguage.equals("JavaScript (Node.js)")) {
            testCommand = "npm test"; // Node.js test command
        } else {
            outputTextArea.appendText("No tests available for this language.\n");
            return;
        }

        // Detect if tests are present in the project directory
        if (!containsTestFiles(selectedDirectory)) {
            outputTextArea.appendText("No tests found. Please add tests to your project.\n");
            return;
        }

        executeCommand(testCommand);
    }

    // Check if test files exist in the project directory
    private boolean containsTestFiles(File projectDir) {
        // Check common directories for tests
        File testDir = new File(projectDir, "src/test");
        return testDir.exists() && testDir.isDirectory();
    }

    // Execute the given command (build or test) and capture output
    private void executeCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command, null, selectedDirectory);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                outputTextArea.appendText(line + "\n");
            }
        } catch (IOException e) {
            outputTextArea.appendText("Error executing command: " + e.getMessage() + "\n");
        }
    }
}
