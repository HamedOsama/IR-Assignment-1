/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package invertedIndex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.Math.log10;
import static java.lang.Math.sqrt;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.io.PrintWriter;

/**
 *
 * @author ehab
 */
public class Index5 {

    // --------------------------------------------
    int N = 0;
    public Map<Integer, SourceRecord> sources; // store the doc_id and the file name.

    public HashMap<String, DictEntry> index; // THe inverted index
    // --------------------------------------------

    public Index5() {
        sources = new HashMap<Integer, SourceRecord>();
        index = new HashMap<String, DictEntry>();
    }

    public void setN(int n) {
        N = n;
    }

    // ---------------------------------------------
    // ---------------------------------------------
    // This method prints the posting list starting from the given Posting object.
    // It traverses through the posting list and prints each document ID, separated
    // by commas, within square brackets.
    // If there is no next Posting, it prints the document ID without appending a
    // comma.

    public void printPostingList(Posting p) {
        // Iterator<Integer> it2 = hset.iterator(); // Uncomment this line if using an
        // iterator
        System.out.print("["); // Opening bracket for the posting list
        while (p != null) {
            // Prints the document ID followed by a comma unless it's the last element
            if (p.next == null)
                System.out.print("" + p.docId);
            else
                System.out.print("" + p.docId + ",");
            p = p.next; // Move to the next posting in the list
        }
        System.out.println("]"); // Closing bracket for the posting list
    }

    // ---------------------------------------------
    /**
     * This method prints the dictionary containing the terms along with their
     * document frequency
     * and their corresponding posting lists.
     * It iterates through the entries of the index, retrieving each term along with
     * its associated
     * dictionary entry. It then prints the term, its document frequency, and its
     * posting list.
     * Finally, it prints the total number of terms in the dictionary.
     * 
     * Note: The printing of the dictionary is formatted as follows:
     * - Each term is printed within double square brackets, followed by its
     * document frequency.
     * - The corresponding posting list is printed using the printPostingList()
     * method.
     * - The total number of terms in the dictionary is printed at the end.
     */
    public void printDictionary() {
        Iterator it = index.entrySet().iterator(); // Iterator to traverse the index entries
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next(); // Retrieve the next entry
            DictEntry dd = (DictEntry) pair.getValue(); // Extract the dictionary entry
            // Print the term, its document frequency, and the associated posting list
            System.out.print("** [" + pair.getKey() + "," + dd.doc_freq + "]       =--> ");
            printPostingList(dd.pList); // Print the posting list associated with the term
        }
        // Print a separator and the total number of terms in the dictionary
        System.out.println("------------------------------------------------------");
        System.out.println("*** Number of terms = " + index.size());
    }

    // -----------------------------------------------
    /**
     * This method builds an index from text files stored on disk.
     * It takes an array of file names as input and processes each file to index its
     * content.
     * 
     * For each file, the method reads its content line by line and indexes each
     * line.
     * It also updates the source records with information about the indexed files.
     * 
     * The process involves several steps:
     * 1. Reading the content of each file.
     * 2. Tokenizing the text into words, filtering out stop words, and performing
     * stemming.
     * 3. Adding the words to the index and updating document frequencies and term
     * frequencies.
     * 
     * The method iterates through each line of text in a file, calling the
     * 'indexOneLine' method
     * to index the content. It also calculates the total word count for each file.
     * 
     * Additionally, it updates source records with the total word count of each
     * indexed file.
     * 
     * If a file is not found or cannot be read, the method prints an error message
     * and skips it.
     * 
     * Finally, the method increments the document ID counter and returns after
     * processing all files.
     * 
     * @param files An array of file names containing the text to be indexed.
     */
    public void buildIndex(String[] files) {
        int fid = 0; // Initialize document ID counter
        // Iterate through each file name in the input array
        for (String fileName : files) {
            try (BufferedReader file = new BufferedReader(new FileReader(fileName))) {
                // Check if the file name is not already in the sources map; if not, add it
                if (!sources.containsKey(fileName)) {
                    sources.put(fid, new SourceRecord(fid, fileName, fileName, "notext"));
                }
                String ln;
                int flen = 0; // Initialize total word count for the file
                // Read each line of text from the file and index its content
                while ((ln = file.readLine()) != null) {
                    flen += indexOneLine(ln, fid); // Index the line and update total word count
                }
                // Update the total word count for the file in the source records
                sources.get(fid).length = flen;
            } catch (IOException e) {
                // Print error message if the file is not found or cannot be read
                System.out.println("File " + fileName + " not found. Skip it");
            }
            fid++; // Increment document ID counter for the next file
        }
        // Optionally, print the dictionary after indexing all files
        // printDictionary();
    }

    // ----------------------------------------------------------------------------
    /**
     * This method indexes a single line of text from a document.
     * It processes the line to extract individual words, filters out stop words,
     * performs stemming on each word, and adds them to the index.
     * 
     * The method splits the input line into words using non-word characters as
     * delimiters.
     * It increments the total word count for the line by the number of words
     * extracted.
     * 
     * For each word extracted, it checks if the word is a stop word using the
     * 'stopWord' method.
     * If the word is not a stop word, it converts it to lowercase and performs
     * stemming using the 'stemWord' method.
     * 
     * It then checks if the word is already in the index. If not, it adds the word
     * to the index
     * and initializes its posting list if necessary.
     * 
     * It adds the document ID to the posting list associated with the word in the
     * index,
     * updating the document frequency and term frequency accordingly.
     * 
     * Additionally, if the word is "lattice" (case-insensitive), it prints debug
     * information.
     * 
     * Finally, the method returns the total word count for the line.
     * 
     * @param ln  The line of text to be indexed.
     * @param fid The document ID associated with the line.
     * @return The total number of words indexed from the line.
     */
    public int indexOneLine(String ln, int fid) {
        int flen = 0; // Initialize the total word count for the line

        String[] words = ln.split("\\W+"); // Split the line into words using non-word characters as delimiters
        flen += words.length; // Increment the total word count by the number of words extracted

        // Process each word extracted from the line
        for (String word : words) {
            word = word.toLowerCase(); // Convert the word to lowercase
            // Check if the word is a stop word; if yes, skip to the next word
            if (stopWord(word)) {
                continue;
            }
            word = stemWord(word); // Perform stemming on the word

            // Check if the word is not already in the index; if not, add it
            if (!index.containsKey(word)) {
                index.put(word, new DictEntry());
            }

            // Add the document ID to the posting list associated with the word in the index
            if (!index.get(word).postingListContains(fid)) {
                index.get(word).doc_freq += 1; // Increment document frequency
                // Initialize the posting list if necessary
                if (index.get(word).pList == null) {
                    index.get(word).pList = new Posting(fid);
                    index.get(word).last = index.get(word).pList;
                } else {
                    index.get(word).last.next = new Posting(fid);
                    index.get(word).last = index.get(word).last.next;
                }
            } else {
                index.get(word).last.dtf += 1; // Increment term frequency
            }

            // Increment term frequency in the collection
            index.get(word).term_freq += 1;

            // Print debug information if the word is "lattice" (case-insensitive)
            if (word.equalsIgnoreCase("lattice")) {
                System.out.println("  <<" + index.get(word).getPosting(1) + ">> " + ln);
            }
        }
        return flen; // Return the total word count for the line
    }

    // ----------------------------------------------------------------------------
    /**
     * This method checks if a given word is a stop word.
     * Stop words are common words that are often filtered out because they are
     * considered
     * to have little semantic value or are too frequent to be useful in information
     * retrieval.
     * 
     * The method compares the input word to a predefined list of stop words.
     * If the word matches any of the stop words or if its length is less than 2
     * characters,
     * the method returns true, indicating that the word is a stop word.
     * Otherwise, it returns false.
     * 
     * @param word The word to be checked for being a stop word.
     * @return True if the word is a stop word, false otherwise.
     */
    boolean stopWord(String word) {
        // Predefined list of stop words
        if (word.equals("the") || word.equals("to") || word.equals("be") || word.equals("for") || word.equals("from")
                || word.equals("in")
                || word.equals("a") || word.equals("into") || word.equals("by") || word.equals("or")
                || word.equals("and") || word.equals("that")) {
            return true; // Return true if the word matches any stop word
        }
        // Check if the length of the word is less than 2 characters
        if (word.length() < 2) {
            return true; // Return true if the word is shorter than 2 characters
        }
        return false; // Return false if the word is not a stop word
    }

    // ----------------------------------------------------------------------------
    /**
     * This method performs stemming on a given word.
     * Stemming is the process of reducing words to their base or root form.
     * 
     * In this implementation, the method simply returns the input word unchanged,
     * indicating that no stemming is applied.
     * 
     * @param word The word to be stemmed.
     * @return The stemmed word (in this case, the input word is returned
     *         unchanged).
     */
    String stemWord(String word) {
        return word; // Return the input word unchanged (no stemming applied)
    }

    // ----------------------------------------------------------------------------
    /**
     * This method intersects two posting lists to find common documents between
     * them.
     * It takes two posting lists as input and returns a new posting list containing
     * the document IDs that are common between the input posting lists.
     * 
     * The method iterates through both posting lists simultaneously and compares
     * the document IDs of corresponding postings.
     * 
     * If the document IDs match, it adds the document ID to the answer posting
     * list.
     * 
     * If the document ID of the first posting is less than the document ID of the
     * second posting,
     * it moves to the next posting in the first posting list.
     * 
     * If the document ID of the second posting is less than the document ID of the
     * first posting,
     * it moves to the next posting in the second posting list.
     * 
     * The method continues this process until it reaches the end of either posting
     * list.
     * 
     * @param pL1 The first posting list to intersect.
     * @param pL2 The second posting list to intersect.
     * @return A new posting list containing the document IDs common to both input
     *         posting lists.
     */
    Posting intersect(Posting pL1, Posting pL2) {
        // Initialize variables for the intersection process
        Posting answer = null; // Initialize the answer posting list
        Posting last = null; // Pointer to the last posting in the answer list

        // Iterate through both posting lists simultaneously
        while (pL1 != null && pL2 != null) {
            // If document IDs of corresponding postings match, add the document ID to the
            // answer list
            if (pL1.docId == pL2.docId) {
                if (answer == null) {
                    answer = new Posting(pL1.docId); // Create a new posting list if answer is null
                    last = answer;
                } else {
                    last.next = new Posting(pL1.docId); // Add document ID to the answer list
                    last = last.next;
                }
                pL1 = pL1.next; // Move to the next posting in the first posting list
                pL2 = pL2.next; // Move to the next posting in the second posting list
            } else if (pL1.docId < pL2.docId) {
                pL1 = pL1.next; // Move to the next posting in the first posting list
            } else {
                pL2 = pL2.next; // Move to the next posting in the second posting list
            }
        }

        // Return the answer posting list containing common document IDs
        return answer;
    }

    /**
     * This method performs a non-optimized search for a given phrase in the index.
     * It splits the input phrase into individual words and retrieves the posting
     * list
     * for the first word from the index.
     * 
     * Then, it iterates through the remaining words in the phrase, intersecting
     * their
     * posting lists with the posting list of the first word to find common
     * documents.
     * 
     * For each document found in the intersected posting lists, the method
     * retrieves
     * the corresponding source record and constructs a result string containing
     * document ID, title, and length information.
     * 
     * Finally, the method returns the result string.
     * 
     * @param phrase The phrase to search for in the index.
     * @return A string containing information about documents matching the given
     *         phrase.
     */
    public String find_24_01(String phrase) {
        String result = ""; // Initialize the result string
        String[] words = phrase.split("\\W+"); // Split the phrase into individual words
        int len = words.length; // Get the number of words in the phrase

        // Retrieve the posting list for the first word from the index
        Posting posting = index.get(words[0].toLowerCase()).pList;
        int i = 1;
        // Intersect the posting lists of the remaining words with the posting list of
        // the first word
        while (i < len) {
            posting = intersect(posting, index.get(words[i].toLowerCase()).pList);
            i++;
        }
        // Iterate through the posting list of intersected documents
        while (posting != null) {
            // Retrieve source record information for the document and append it to the
            // result string
            result += "\t" + posting.docId + " - " + sources.get(posting.docId).title + " - "
                    + sources.get(posting.docId).length + "\n";
            posting = posting.next; // Move to the next posting in the list
        }
        return result; // Return the result string containing document information
    }

    // ---------------------------------
    String[] sort(String[] words) { // bubble sort
        boolean sorted = false;
        String sTmp;
        // -------------------------------------------------------
        while (!sorted) {
            sorted = true;
            for (int i = 0; i < words.length - 1; i++) {
                int compare = words[i].compareTo(words[i + 1]);
                if (compare > 0) {
                    sTmp = words[i];
                    words[i] = words[i + 1];
                    words[i + 1] = sTmp;
                    sorted = false;
                }
            }
        }
        return words;
    }

    // ---------------------------------

    /**
     * This method stores the index and source records into a storage file.
     * It constructs the path to the storage file based on the current working
     * directory and the provided storage name.
     * 
     * The method iterates through the source records and writes their information
     * (URL, title, length, norm, text)
     * to the storage file, replacing commas in the title and text with '~' to avoid
     * conflicts with CSV formatting.
     * 
     * After writing the source records, it writes a separator "section2" to
     * demarcate the start of the index data.
     * Then, it iterates through the index entries and writes the term frequency,
     * document frequency,
     * and posting list associated with each term to the storage file.
     * 
     * Finally, it adds an "end" marker to indicate the end of the file and closes
     * the FileWriter.
     * 
     * @param storageName The name of the storage file to store the index and source
     *                    records.
     */
    public void store(String storageName) {
        try {
            String currentPath = System.getProperty("user.dir"); // Get the current working directory
            // Construct the path to the storage file
            String pathToStorage = currentPath + "\\invertedIndex\\tmp11\\output\\" + storageName;
            Writer wr = new FileWriter(pathToStorage); // Create a FileWriter to write to the storage file

            // Write source records to the storage file
            for (Map.Entry<Integer, SourceRecord> entry : sources.entrySet()) {
                // Write source record information to the storage file, replacing commas in
                // title and text with '~'
                wr.write(entry.getKey().toString() + ",");
                wr.write(entry.getValue().URL.toString() + ",");
                wr.write(entry.getValue().title.replace(',', '~') + ",");
                wr.write(entry.getValue().length + ",");
                wr.write(String.format("%4.4f", entry.getValue().norm) + ",");
                wr.write(entry.getValue().text.toString().replace(',', '~') + "\n");
            }
            wr.write("section2" + "\n"); // Write separator to demarcate start of index data

            // Write index data to the storage file
            Iterator it = index.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                DictEntry dd = (DictEntry) pair.getValue();
                // Write term frequency, document frequency, and posting list to the storage
                // file
                wr.write(pair.getKey().toString() + "," + dd.doc_freq + "," + dd.term_freq + ";");
                Posting p = dd.pList;
                while (p != null) {
                    wr.write(p.docId + "," + p.dtf + ":"); // Write document ID and term frequency
                    p = p.next; // Move to the next posting in the posting list
                }
                wr.write("\n"); // Write newline character to separate postings for each term
            }

            wr.write("end" + "\n"); // Write marker indicating end of the file
            wr.close(); // Close the FileWriter
            System.out.println("=============EBD STORE=============");

        } catch (Exception e) {
            e.printStackTrace(); // Print stack trace if an exception occurs during file writing
        }
    }

    // =========================================
    /**
     * This method checks whether a storage file with the specified name exists in
     * the directory "/home/ehab/tmp11/rl/".
     * It constructs a File object representing the storage file using the provided
     * name and directory path.
     * 
     * If the file exists and is not a directory, the method returns true,
     * indicating that the storage file exists.
     * Otherwise, it returns false.
     * 
     * @param storageName The name of the storage file to be checked for existence.
     * @return True if the storage file exists and is not a directory, false
     *         otherwise.
     */
    public boolean storageFileExists(String storageName) {
        // Construct a File object representing the storage file
        java.io.File f = new java.io.File("/home/ehab/tmp11/rl/" + storageName);
        // Check if the file exists and is not a directory
        if (f.exists() && !f.isDirectory())
            return true; // Return true if the file exists and is not a directory
        return false; // Return false if the file does not exist or is a directory
    }

    // ----------------------------------------------------
    /**
     * This method creates a new storage file with the specified name in the
     * directory "/home/ehab/tmp11/".
     * It writes the string "end" followed by a newline character to the storage
     * file, indicating the end of the file.
     * 
     * @param storageName The name of the storage file to be created.
     */
    public void createStore(String storageName) {
        try {
            // Construct the full path to the storage file
            String pathToStorage = "/home/ehab/tmp11/" + storageName;
            // Create a new FileWriter to write to the storage file
            Writer wr = new FileWriter(pathToStorage);
            // Write "end" followed by a newline character to the storage file
            wr.write("end" + "\n");
            // Close the FileWriter to release system resources
            wr.close();
        } catch (Exception e) {
            // Print stack trace if an exception occurs during file creation
            e.printStackTrace();
        }
    }

    // ----------------------------------------------------
    // load index from hard disk into memory
    /**
     * This method loads the index and source records from a storage file.
     * It reads the contents of the storage file located at the specified path.
     * 
     * The method initializes new HashMaps for source records and index entries.
     * It then iterates through each line of the file to parse and load the data.
     * 
     * Source records are loaded until the separator "section2" is encountered.
     * Each line representing a source record is split by commas, and the components
     * are used
     * to create a new SourceRecord object, which is then added to the 'sources'
     * HashMap.
     * 
     * After loading source records, the method proceeds to load the index entries.
     * Each line representing an index entry is split by semicolons into term
     * information and postings.
     * The term information includes the term itself, document frequency, and term
     * frequency.
     * Postings for each term are split by colons into document IDs and term
     * frequencies.
     * 
     * The method constructs DictEntry objects for each term and adds them to the
     * 'index' HashMap,
     * associating each term with its corresponding posting list.
     * 
     * Finally, the method returns the loaded index.
     * 
     * @param storageName The name of the storage file from which to load the index
     *                    and source records.
     * @return The loaded index as a HashMap of terms mapped to their corresponding
     *         DictEntry objects.
     */
    public HashMap<String, DictEntry> load(String storageName) {
        try {
            String pathToStorage = "/home/ehab/tmp11/rl/" + storageName; // Path to the storage file
            sources = new HashMap<Integer, SourceRecord>(); // Initialize HashMap for source records
            index = new HashMap<String, DictEntry>(); // Initialize HashMap for index entries

            BufferedReader file = new BufferedReader(new FileReader(pathToStorage)); // Open the storage file for
                                                                                     // reading
            String ln = ""; // String to hold each line read from the file

            // Load source records until the separator "section2" is encountered
            while ((ln = file.readLine()) != null) {
                if (ln.equalsIgnoreCase("section2")) {
                    break; // Stop loading source records when the separator is encountered
                }
                String[] ss = ln.split(","); // Split the line by commas to extract source record components
                int fid = Integer.parseInt(ss[0]); // Extract file ID
                try {
                    // Create a new SourceRecord object using the extracted components and add it to
                    // 'sources'
                    SourceRecord sr = new SourceRecord(fid, ss[1], ss[2].replace('~', ','), Integer.parseInt(ss[3]),
                            Double.parseDouble(ss[4]), ss[5].replace('~', ','));
                    sources.put(fid, sr); // Add the SourceRecord to 'sources'
                } catch (Exception e) {
                    // Print error message if there's an issue creating the SourceRecord
                    System.out.println(fid + "  ERROR  " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // Load index entries
            while ((ln = file.readLine()) != null) {
                if (ln.equalsIgnoreCase("end")) {
                    break; // Stop loading index entries when the end marker is encountered
                }
                String[] ss1 = ln.split(";"); // Split the line by semicolons to separate term information and postings
                String[] ss1a = ss1[0].split(","); // Split term information by commas
                String[] ss1b = ss1[1].split(":"); // Split postings by colons

                // Create a new DictEntry for the term using document frequency and term
                // frequency
                index.put(ss1a[0], new DictEntry(Integer.parseInt(ss1a[1]), Integer.parseInt(ss1a[2])));
                String[] ss1bx; // Array to hold individual postings
                // Process each posting for the term
                for (int i = 0; i < ss1b.length; i++) {
                    ss1bx = ss1b[i].split(","); // Split each posting by commas to extract document ID and term
                                                // frequency
                    // Add the posting to the posting list of the term in the index
                    if (index.get(ss1a[0]).pList == null) {
                        index.get(ss1a[0]).pList = new Posting(Integer.parseInt(ss1bx[0]), Integer.parseInt(ss1bx[1]));
                        index.get(ss1a[0]).last = index.get(ss1a[0]).pList;
                    } else {
                        index.get(ss1a[0]).last.next = new Posting(Integer.parseInt(ss1bx[0]),
                                Integer.parseInt(ss1bx[1]));
                        index.get(ss1a[0]).last = index.get(ss1a[0]).last.next;
                    }
                }
            }
            System.out.println("============= END LOAD ============="); // Print message indicating end of loading
            return index; // Return the loaded index
        } catch (Exception e) {
            e.printStackTrace(); // Print stack trace if an exception occurs during loading
        }
        return index; // Return the loaded index (or null if an exception occurred)
    }
}

// =====================================================================
