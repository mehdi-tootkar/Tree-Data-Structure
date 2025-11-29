# Student Management System using a Trie

## Project Overview

This repository contains a **Student Management System** implemented in **Java**. It leverages a **Trie (prefix tree)** data structure to efficiently store and search student IDs. Compared to the original version, this upgraded system features an object‑oriented design, **persistent storage on disk** and **DFS‑based autocomplete**, making it suitable for advanced coursework or portfolio projects.

### Features

* **Add, search, update and remove students** via a simple console menu.
* **Accurate autocomplete**: when you enter a prefix of a student ID, the system uses depth‑first search (DFS) on the Trie to suggest full IDs. This avoids scanning the entire map and improves performance.
* **Persist data** to a `students.csv` file. Whenever a record is added, updated or removed, changes are saved. Previously saved data are loaded when the application starts.
* **Object‑oriented design**: all operations are instance methods (no `static` methods) to facilitate extensibility, testing and reuse.
* **Clean code organization**: separate classes for the student entity (`Student`), the Trie (`Trie`) and the management logic (`StudentManager`).
* **List all students** sorted by ID.

## Project Structure

```
student‑management/
├── README.md            # This file
├── Student.java         # Data model for a student
├── Trie.java            # Prefix tree implementation
├── StudentManager.java  # Main application logic and CLI
└── students.csv         # Data storage file (created after the first run)
```

## How to Use

### Prerequisites

* Java JDK 11 or higher.

### Compile

To compile all classes, navigate to the project root and run:

```bash
javac -d . student-management/*.java
```

The compiled `.class` files will be placed under the `studentmanagement/` package.

### Run

To run the program:

```bash
java studentmanagement.StudentManager
```

After launching, a console menu will appear and you can select the following options:

1. **Add new student**: enter an ID, name, field of study and GPA. Duplicate IDs are not permitted.
2. **Search student**: enter a full ID or a prefix. If a full match exists, the student’s details are shown; otherwise, the prefix is used to suggest possible IDs.
3. **Update existing student**: change the name, field of study or GPA of a found student (the ID cannot be changed).
4. **Remove student**: delete a student by full ID or select from autocomplete suggestions.
5. **List all students**: show all students sorted by ID.
0. **Quit**: exit the application and save the current state.

## Technical Highlights

* The **Trie** in `Trie.java` is implemented entirely with instance methods (no `static` methods), supporting insertion, search, prefix search, DFS‑based autocomplete and deletion.
* The **autocomplete** functionality uses `getWordsWithPrefix`, which performs a DFS from the node representing the given prefix. This returns a list of complete strings without scanning unrelated entries.
* Data are stored in a CSV file (`students.csv`). If the file does not exist, it is created on the first run. Each line holds: `studentNumber;name;fieldOfStudy;gpa` (semicolon‑separated).
* System messages and source code are in English to align with industry norms.

## Suggested Enhancements

To make this project even more impressive for a thesis or portfolio, consider adding:

* **Graphical user interface**: build a GUI with JavaFX or Swing instead of a console.
* **Database support**: store data in SQLite or PostgreSQL and use an ORM such as Hibernate.
* **Unit tests**: implement JUnit tests for the key operations (insertion, search, deletion, etc.).
* **Smart autocomplete**: limit the number of results or rank suggestions based on custom criteria.
* **Import/export JSON or XML** to integrate with other systems.

## Conclusion

This project showcases proficiency in the Trie data structure, object‑oriented programming and basic data persistence in Java. With the suggested enhancements, it can serve as a strong portfolio piece or a foundation for more sophisticated student management systems.
