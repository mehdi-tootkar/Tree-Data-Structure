package studentmanagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A trie (prefix tree) implementation for storing and retrieving strings efficiently.
 * <p>
 * The trie supports insertion, exact search, prefix search and deletion. Additionally it
 * exposes a method to return all complete words sharing a given prefix. This is
 * useful for implementing autocomplete functionality without having to scan an
 * external map or collection.
 */
public class Trie {
    /**
     * Internal node representation. Each node holds a map of character to child
     * nodes and a flag indicating whether the path to this node constitutes a
     * complete word.
     */
    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEndOfWord;
    }

    private final TrieNode root;

    /**
     * Constructs an empty trie.
     */
    public Trie() {
        this.root = new TrieNode();
    }

    /**
     * Inserts a new word into the trie.
     *
     * @param word the string to insert
     */
    public void insert(String word) {
        if (word == null || word.isEmpty()) {
            return;
        }
        TrieNode current = root;
        for (char c : word.toCharArray()) {
            current = current.children.computeIfAbsent(c, k -> new TrieNode());
        }
        current.isEndOfWord = true;
    }

    /**
     * Returns true if the given word exists in the trie.
     *
     * @param word the string to search for
     * @return true if the word exists, false otherwise
     */
    public boolean search(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }
        TrieNode node = traverseToNode(word);
        return node != null && node.isEndOfWord;
    }

    /**
     * Returns true if there exists at least one word in the trie that starts with
     * the given prefix.
     *
     * @param prefix the prefix to search for
     * @return true if any word begins with the prefix, false otherwise
     */
    public boolean startsWith(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return false;
        }
        return traverseToNode(prefix) != null;
    }

    /**
     * Retrieves a list of all full words in the trie that start with the given
     * prefix. If the prefix is not present, an empty list is returned.
     *
     * @param prefix the prefix to search for
     * @return a list of complete words beginning with the prefix
     */
    public List<String> getWordsWithPrefix(String prefix) {
        List<String> results = new ArrayList<>();
        if (prefix == null || prefix.isEmpty()) {
            return results;
        }
        TrieNode node = traverseToNode(prefix);
        if (node == null) {
            return results;
        }
        // Use a StringBuilder to build strings during DFS to avoid costly string
        // concatenation.
        StringBuilder sb = new StringBuilder(prefix);
        dfs(node, sb, results);
        return results;
    }

    /**
     * Deletes a word from the trie if it exists. This method returns true only if
     * the word was found and removed. The removal is performed lazily: nodes are
     * only removed when they are no longer needed by any other word (i.e. have no
     * children and are not marked as end-of-word for another word).
     *
     * @param word the word to delete
     * @return true if the word was deleted, false if it was not found
     */
    public boolean delete(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }
        return delete(root, word, 0);
    }

    // ----------------------- Private helper methods ---------------------------

    /**
     * Traverses the trie along the given string and returns the node representing
     * its last character. If any character is not found, null is returned.
     */
    private TrieNode traverseToNode(String str) {
        TrieNode current = root;
        for (char c : str.toCharArray()) {
            TrieNode next = current.children.get(c);
            if (next == null) {
                return null;
            }
            current = next;
        }
        return current;
    }

    /**
     * Depth-first search from the given node, appending characters to the
     * StringBuilder as the search descends. When an end-of-word node is reached,
     * the built string is added to the results list.
     */
    private void dfs(TrieNode node, StringBuilder sb, List<String> results) {
        if (node.isEndOfWord) {
            results.add(sb.toString());
        }
        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            sb.append(entry.getKey());
            dfs(entry.getValue(), sb, results);
            sb.deleteCharAt(sb.length() - 1);
        }
    }

    /**
     * Recursive helper for deleting a word. Returns true if the parent should
     * delete this child node, false otherwise.
     */
    private boolean delete(TrieNode current, String word, int index) {
        if (index == word.length()) {
            if (!current.isEndOfWord) {
                return false; // Word not found
            }
            current.isEndOfWord = false;
            // If the node has no children, tell the parent to remove this node
            return current.children.isEmpty();
        }
        char ch = word.charAt(index);
        TrieNode node = current.children.get(ch);
        if (node == null) {
            return false; // Word not found
        }
        boolean shouldDeleteCurrentNode = delete(node, word, index + 1);
        if (shouldDeleteCurrentNode) {
            current.children.remove(ch);
            return current.children.isEmpty() && !current.isEndOfWord;
        }
        return false;
    }
}