package studentmanagement;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Main entry point and coordinator for the student management application.
 * <p>
 * This class encapsulates all logic related to interacting with the user via
 * console, persisting student data to disk and interfacing with the Trie for
 * efficient prefix-based search. Upon startup it loads any previously saved
 * students from a simple semicolon-separated file. Every mutation (add,
 * update, remove) triggers a save to disk so that no data is lost on crash.
 */
public class StudentManager {
    // A map from student number to Student object for quick lookup of student details
    private final Map<String, Student> students = new HashMap<>();
    // Trie structure holding all student numbers for prefix queries
    private final Trie trie = new Trie();
    // File used to persist student data between runs. Located in the working directory
    private static final String DATA_FILE = "students.csv";

    /**
     * Constructs a new StudentManager and loads previously saved students from
     * disk.
     */
    public StudentManager() {
        loadFromFile();
    }

    /**
     * Loads students from the data file into memory. Each line in the file
     * represents one student, with fields separated by semicolons in the order
     * studentNumber;name;fieldOfStudy;gpa. Lines beginning with a hash (#) or
     * blank lines are ignored. If the file does not exist, nothing happens.
     */
    private void loadFromFile() {
        Path path = Paths.get(DATA_FILE);
        if (!Files.exists(path)) {
            // No existing data; nothing to load
            return;
        }
        try (Reader reader = Files.newBufferedReader(path);
             BufferedReader br = new BufferedReader(reader)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split(";", -1);
                if (parts.length != 4) {
                    continue; // Malformed line; skip it
                }
                String number = parts[0];
                String name = parts[1];
                String field = parts[2];
                double gpa;
                try {
                    gpa = Double.parseDouble(parts[3]);
                } catch (NumberFormatException e) {
                    continue; // Bad GPA; skip
                }
                Student student = new Student(name, number, field, gpa);
                students.put(number, student);
                trie.insert(number);
            }
        } catch (IOException e) {
            System.err.println("Error reading data file: " + e.getMessage());
        }
    }

    /**
     * Saves all students currently in memory to the data file. Each student is
     * written on its own line separated by semicolons. This method overwrites
     * the existing file each time it is called.
     */
    private void saveToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) {
            // Write header comment for humans
            writer.write("# studentNumber;name;fieldOfStudy;gpa\n");
            for (Student student : students.values()) {
                String line = String.format("%s;%s;%s;%.2f",
                        student.getStudentNumber(),
                        student.getName(),
                        student.getFieldOfStudy(),
                        student.getGpa());
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing data file: " + e.getMessage());
        }
    }

    /**
     * Adds a new student to the system after prompting the user for details. If
     * the student number already exists, the user is notified and nothing is
     * added.
     *
     * @param scanner the scanner used to read user input
     */
    private void addStudent(Scanner scanner) {
        System.out.print("Enter student number: ");
        String number = scanner.nextLine().trim();
        if (number.isEmpty()) {
            System.out.println("Student number cannot be empty.");
            return;
        }
        if (students.containsKey(number)) {
            System.out.println("A student with number " + number + " already exists.");
            return;
        }
        System.out.print("Enter student name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }
        System.out.print("Enter field of study: ");
        String field = scanner.nextLine().trim();
        System.out.print("Enter GPA (e.g. 17.5): ");
        String gpaStr = scanner.nextLine().trim();
        double gpa;
        try {
            gpa = Double.parseDouble(gpaStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid GPA. Please enter a numeric value.");
            return;
        }
        Student student = new Student(name, number, field, gpa);
        students.put(number, student);
        trie.insert(number);
        saveToFile();
        System.out.println("Student added successfully.");
    }

    /**
     * Searches for a student by student number. If an exact match is not found
     * but the prefix exists, the user is presented with autocomplete options.
     *
     * @param scanner the scanner used to read user input
     */
    private void searchStudent(Scanner scanner) {
        System.out.print("Enter student number (full or prefix): ");
        String number = scanner.nextLine().trim();
        if (students.containsKey(number)) {
            Student student = students.get(number);
            printStudent(student);
            return;
        }
        // If prefix exists, show suggestions
        if (trie.startsWith(number)) {
            List<String> suggestions = trie.getWordsWithPrefix(number);
            // Sort suggestions for predictable output
            Collections.sort(suggestions);
            // Remove suggestions that are not actually in the students map (shouldn't happen but safe)
            suggestions.removeIf(s -> !students.containsKey(s));
            if (suggestions.isEmpty()) {
                System.out.println("No students match the given prefix.");
                return;
            }
            System.out.println("Did you mean one of the following?");
            for (int i = 0; i < suggestions.size(); i++) {
                System.out.printf("%d. %s\n", i + 1, suggestions.get(i));
            }
            System.out.print("Enter number of choice (or 0 to cancel): ");
            String choiceStr = scanner.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(choiceStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid selection.");
                return;
            }
            if (choice > 0 && choice <= suggestions.size()) {
                String selectedNumber = suggestions.get(choice - 1);
                Student student = students.get(selectedNumber);
                printStudent(student);
            } else if (choice == 0) {
                System.out.println("Search cancelled.");
            } else {
                System.out.println("Invalid selection.");
            }
        } else {
            System.out.println("Student with number " + number + " does not exist.");
        }
    }

    /**
     * Updates an existing student's information. The user can modify the name,
     * field of study and GPA. The student number cannot be changed.
     *
     * @param scanner the scanner used to read user input
     */
    private void updateStudent(Scanner scanner) {
        System.out.print("Enter student number to update (full or prefix): ");
        String number = scanner.nextLine().trim();
        Student target = null;
        if (students.containsKey(number)) {
            target = students.get(number);
        } else if (trie.startsWith(number)) {
            List<String> suggestions = trie.getWordsWithPrefix(number);
            Collections.sort(suggestions);
            suggestions.removeIf(s -> !students.containsKey(s));
            if (suggestions.isEmpty()) {
                System.out.println("No students match the given prefix.");
                return;
            }
            System.out.println("Did you mean one of the following?");
            for (int i = 0; i < suggestions.size(); i++) {
                System.out.printf("%d. %s\n", i + 1, suggestions.get(i));
            }
            System.out.print("Enter number of choice (or 0 to cancel): ");
            String choiceStr = scanner.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(choiceStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid selection.");
                return;
            }
            if (choice > 0 && choice <= suggestions.size()) {
                String selectedNumber = suggestions.get(choice - 1);
                target = students.get(selectedNumber);
            } else if (choice == 0) {
                System.out.println("Update cancelled.");
                return;
            } else {
                System.out.println("Invalid selection.");
                return;
            }
        }
        if (target == null) {
            System.out.println("Student with number " + number + " does not exist.");
            return;
        }
        System.out.println("Updating student: " + target.getStudentNumber());
        System.out.print("Enter new name (leave blank to keep current: " + target.getName() + "): ");
        String newName = scanner.nextLine().trim();
        if (!newName.isEmpty()) {
            target.setName(newName);
        }
        System.out.print("Enter new field of study (leave blank to keep current: " + target.getFieldOfStudy() + "): ");
        String newField = scanner.nextLine().trim();
        if (!newField.isEmpty()) {
            target.setFieldOfStudy(newField);
        }
        System.out.print("Enter new GPA (leave blank to keep current: " + target.getGpa() + "): ");
        String newGpaStr = scanner.nextLine().trim();
        if (!newGpaStr.isEmpty()) {
            try {
                double newGpa = Double.parseDouble(newGpaStr);
                target.setGpa(newGpa);
            } catch (NumberFormatException e) {
                System.out.println("Invalid GPA. Keeping current value.");
            }
        }
        // Update the map with the modified student (key remains the same)
        students.put(target.getStudentNumber(), target);
        saveToFile();
        System.out.println("Student updated successfully.");
    }

    /**
     * Removes a student from the system. If the given input is a prefix, the
     * user is prompted to choose from matching student numbers. Removal is
     * followed by saving to disk.
     *
     * @param scanner the scanner used to read user input
     */
    private void removeStudent(Scanner scanner) {
        System.out.print("Enter student number to remove (full or prefix): ");
        String number = scanner.nextLine().trim();
        String targetNumber = null;
        if (students.containsKey(number)) {
            targetNumber = number;
        } else if (trie.startsWith(number)) {
            List<String> suggestions = trie.getWordsWithPrefix(number);
            Collections.sort(suggestions);
            suggestions.removeIf(s -> !students.containsKey(s));
            if (suggestions.isEmpty()) {
                System.out.println("No students match the given prefix.");
                return;
            }
            System.out.println("Did you mean one of the following?");
            for (int i = 0; i < suggestions.size(); i++) {
                System.out.printf("%d. %s\n", i + 1, suggestions.get(i));
            }
            System.out.print("Enter number of choice (or 0 to cancel): ");
            String choiceStr = scanner.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(choiceStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid selection.");
                return;
            }
            if (choice > 0 && choice <= suggestions.size()) {
                targetNumber = suggestions.get(choice - 1);
            } else if (choice == 0) {
                System.out.println("Removal cancelled.");
                return;
            } else {
                System.out.println("Invalid selection.");
                return;
            }
        }
        if (targetNumber == null) {
            System.out.println("Student with number " + number + " does not exist.");
            return;
        }
        students.remove(targetNumber);
        trie.delete(targetNumber);
        saveToFile();
        System.out.println("Student removed successfully.");
    }

    /**
     * Prints all students in the system ordered by student number.
     */
    private void listStudents() {
        if (students.isEmpty()) {
            System.out.println("No students registered.");
            return;
        }
        List<Student> all = new ArrayList<>(students.values());
        all.sort(Comparator.comparing(Student::getStudentNumber));
        System.out.println("--- Student List (sorted by number) ---");
        for (Student student : all) {
            printStudent(student);
        }
    }

    /**
     * Utility method to print a student's details in a friendly format.
     *
     * @param student the student to print
     */
    private void printStudent(Student student) {
        System.out.println("\nStudent Number: " + student.getStudentNumber());
        System.out.println("Name:           " + student.getName());
        System.out.println("Field of Study: " + student.getFieldOfStudy());
        System.out.println("GPA:            " + student.getGpa());
        System.out.println();
    }

    /**
     * Displays the main menu and processes user selections. The menu is shown
     * repeatedly until the user chooses to quit.
     */
    public void run() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        while (running) {
            System.out.println("\n--- Student Management Menu ---");
            System.out.println("1. Add new student");
            System.out.println("2. Search student");
            System.out.println("3. Update existing student");
            System.out.println("4. Remove student");
            System.out.println("5. List all students");
            System.out.println("0. Quit");
            System.out.print("Enter your choice: ");
            String choiceStr = scanner.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(choiceStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number from 0 to 5.");
                continue;
            }
            switch (choice) {
                case 1:
                    addStudent(scanner);
                    break;
                case 2:
                    searchStudent(scanner);
                    break;
                case 3:
                    updateStudent(scanner);
                    break;
                case 4:
                    removeStudent(scanner);
                    break;
                case 5:
                    listStudents();
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Unknown choice. Please select between 0 and 5.");
            }
        }
        // Save once more upon exit to ensure any unsaved changes are persisted
        saveToFile();
        System.out.println("Goodbye!");
    }

    /**
     * Application entry point. Creates a new StudentManager and invokes its
     * main loop. Catch any exception to avoid crashing silently.
     */
    public static void main(String[] args) {
        try {
            StudentManager manager = new StudentManager();
            manager.run();
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}