import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.graphstream.ui.view.Viewer;
import org.semanticweb.owlapi.model.*;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;
import org.javatuples.Pair;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;
import org.graphstream.ui.layout.HierarchicalLayout;
import org.semanticweb.owlapi.util.BidirectionalShortFormProvider;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;



public class owlFunctions
{

    /***
     * This record stores the data related to an ontology, including its IRI, number of axioms, and the set of axioms
     */
    public record OntologyData(IRI ontologyIRI, int numberOfAxioms, Set<OWLAxiom> axioms)
    {

        public IRI getOntologyIRI() {return ontologyIRI;} // Getter method to get the ontology IRI

        public int getNumberOfAxioms() {return numberOfAxioms;} // Getter method to get the number of axioms

        public Set<OWLAxiom> getAxioms() {return axioms;} // Getter method to get the set of axioms

    }

    /***
     * Method to handle user's input and attempt to add declarations to the ontology
     */
    public static boolean handleUserInput(String input, OWLOntology ontology, OWLOntologyManager manager, OWLDataFactory dataFactory, IRI ontologyIRI)
    {

        String[] arrayOfString = input.split(" ");

        List<String> classWords = new LinkedList<>();
        List<String> propertyWords = new LinkedList<>();
        Set<OWLAxiom> addedAxioms = new HashSet<>();

        // Trying to identify class and property words with loops
        for (int x = 0; x < arrayOfString.length; x++)
        {

            String word = arrayOfString[x];

            // Remove '(' and ')' characters
            word = word.replace("(", "").replace(")", "");

            if (!word.equalsIgnoreCase("and") && !word.equalsIgnoreCase("some") && !word.equalsIgnoreCase("SubClassOf"))
            {
                if ((x + 1 < arrayOfString.length) && arrayOfString[x + 1].equalsIgnoreCase("some"))
                {
                    if (!propertyWords.contains(word))
                    {
                        propertyWords.add(word);
                    }
                }
                else
                {
                    if (!classWords.contains(word))
                    {
                        classWords.add(word);
                    }
                }
            }

        }

        // Trying to add declarations to the ontology
        try
        {

            // Declaring classes
            for (String className : classWords)
            {
                OWLClass cls = dataFactory.getOWLClass(IRI.create(ontologyIRI + "#" + className));
                OWLDeclarationAxiom classDeclaration = dataFactory.getOWLDeclarationAxiom(cls);
                manager.addAxiom(ontology, classDeclaration);
                addedAxioms.add(classDeclaration);
            }

            // Declaring properties
            for (String propertyName : propertyWords)
            {
                OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRI + "#" + propertyName));
                OWLDeclarationAxiom propertyDeclaration = dataFactory.getOWLDeclarationAxiom(prop);
                manager.addAxiom(ontology, propertyDeclaration);
                addedAxioms.add(propertyDeclaration);
            }

            // Parse the axiom
            OWLAxiom axiom = parseClassExpression(ontology, manager, dataFactory, input);
            manager.addAxiom(ontology, axiom);

            return true;

        }
        catch (Exception e)
        {

            // If there's an error we must remove added declarations that are not used in other axioms
            for (OWLAxiom addedAxiom : addedAxioms)
            {
                // Check if the declared entity is used in other axioms
                if (!isUsedInOtherAxioms(addedAxiom, ontology))
                {
                    manager.removeAxiom(ontology, addedAxiom);
                }
            }

            JOptionPane.showMessageDialog(null, "Error parsing axiom from input: " + input + "\nError: " + e.getMessage(), "Parsing Error", JOptionPane.ERROR_MESSAGE);
            return false;

        }
    }

    /***
     * Method to check if a given entity is used in other axioms
     */
    public static boolean isUsedInOtherAxioms(OWLAxiom addedAxiom, OWLOntology ontology)
    {

        if (addedAxiom instanceof OWLDeclarationAxiom)
        {

            // Get the entity
            OWLEntity entity = ((OWLDeclarationAxiom) addedAxiom).getEntity();

            for (OWLAxiom axiom : ontology.getAxioms())
            {
                // Checks if the entity is contained in the axiom
                if (axiom.getSignature().contains(entity) && !axiom.equals(addedAxiom))
                {
                    return true;
                }

            }
        }

        return false;

    }

    /***
     * Method to parse a class expression from a string
     */
    public static OWLAxiom parseClassExpression(OWLOntology ontology, OWLOntologyManager manager, OWLDataFactory dataFactory, String classExpressionString) {

        // Create a Manchester OWL Syntax Editor Parser with the provided data factory and class expression string
        ManchesterOWLSyntaxEditorParser parser = new ManchesterOWLSyntaxEditorParser(dataFactory, classExpressionString);

        // Set the default ontology for the parser
        parser.setDefaultOntology(ontology);

        // Get the closure of imports (all ontologies imported by the main ontology) to handle all relevant entities
        Set<OWLOntology> importsClosure = ontology.getImportsClosure();

        // Short form provider used to generate simple names for entities (used for display purposes)
        ShortFormProvider shortFormProvider = new SimpleShortFormProvider();

        // Create a bidirectional short form provider adapter to map between entities and their short forms
        BidirectionalShortFormProvider bidiShortFormProvider = new BidirectionalShortFormProviderAdapter(manager, importsClosure, shortFormProvider);

        // Create an entity checker to resolve entity references using the bidirectional short form provider
        OWLEntityChecker entityChecker = new ShortFormEntityChecker(bidiShortFormProvider);

        // Set the entity checker for the parser so that it can resolve entity names in the class expression
        parser.setOWLEntityChecker(entityChecker);

        // Parse the class expression string into an OWL axiom and return it
        return parser.parseAxiom();
    }


    /***
     * Method to remove declarations and axioms when we remove the axiom from the list
     */
    public static void removeAxiomButtonFunction(String input, OWLOntology ontology, OWLOntologyManager manager, OWLDataFactory dataFactory, IRI ontologyIRI)
    {

        String[] arrayOfString = input.split(" ");

        List<String> classWords = new LinkedList<>();
        List<String> propertyWords = new LinkedList<>();
        Set<OWLAxiom> addedAxioms = new HashSet<>();

        // Trying to identify class and property words with loops
        for (int x = 0; x < arrayOfString.length; x++)
        {
            String word = arrayOfString[x];

            // Remove '(' and ')' characters
            word = word.replace("(", "").replace(")", "");

            if (!word.equalsIgnoreCase("and") && !word.equalsIgnoreCase("some") && !word.equalsIgnoreCase("SubClassOf"))
            {
                if ((x + 1 < arrayOfString.length) && arrayOfString[x + 1].equalsIgnoreCase("some"))
                {
                    if (!propertyWords.contains(word))
                    {
                        propertyWords.add(word);
                    }
                }
                else
                {
                    if (!classWords.contains(word))
                    {
                        classWords.add(word);
                    }
                }
            }
        }

        // Trying to add declarations to the ontology
        try
        {
            // Declaring classes
            for (String className : classWords)
            {
                OWLClass cls = dataFactory.getOWLClass(IRI.create(ontologyIRI + "#" + className));
                OWLDeclarationAxiom classDeclaration = dataFactory.getOWLDeclarationAxiom(cls);
                manager.addAxiom(ontology, classDeclaration);
                addedAxioms.add(classDeclaration);
            }

            // Declaring properties
            for (String propertyName : propertyWords)
            {
                OWLObjectProperty prop = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRI + "#" + propertyName));
                OWLDeclarationAxiom propertyDeclaration = dataFactory.getOWLDeclarationAxiom(prop);
                manager.addAxiom(ontology, propertyDeclaration);
                addedAxioms.add(propertyDeclaration);
            }

            // Parse the axiom
            OWLAxiom axiom = parseClassExpression(ontology, manager, dataFactory, input);
            manager.removeAxiom(ontology, axiom);

            for (OWLAxiom addedAxiom : addedAxioms)
            {
                // Check if the declared entity is used in other axioms
                if (!isUsedInOtherAxioms(addedAxiom, ontology))
                {
                    // Removing axiom
                    manager.removeAxiom(ontology, addedAxiom);
                }
            }
        }
        catch (Exception e)
        {

            // If there's an error we must  remove added declarations that are not used in other axioms
            for (OWLAxiom addedAxiom : addedAxioms)
            {
                // Check if the declared entity is used in other axioms
                if (!isUsedInOtherAxioms(addedAxiom, ontology))
                {
                    // Removing axiom
                    manager.removeAxiom(ontology, addedAxiom);
                }
            }

        }

    }

    /***
     * Method to select the destination folder and it's path
     */
    public static String selectDestinationFolder()
    {

        JFileChooser fileChooser = new JFileChooser();

        // Only chooses folders and not files
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);

        // Allow users to create a new folder from within the file chooser
        fileChooser.setDialogTitle("Select or create a new directory");
        fileChooser.setApproveButtonText("Select");

        int returnValue = fileChooser.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION)
        {
            File selectedFolder = fileChooser.getSelectedFile();

            // Check if the directory exists and offer to create it if it does not
            if (!selectedFolder.exists())
            {

                int createOption = JOptionPane.showConfirmDialog(null, "The directory does not exist. Would you like to create it?", "Create Directory", JOptionPane.YES_NO_OPTION);

                if (createOption == JOptionPane.YES_OPTION)
                {

                    if (selectedFolder.mkdirs())
                    {
                        return selectedFolder.getAbsolutePath();
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(null, "Failed to create the directory. Please check permissions.", "Error", JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                }

            }
            else
            {
                return selectedFolder.getAbsolutePath();
            }
        }

        return null;

    }

    /***
     *  Class which changes the renderer color
     */
    public static class OntologyListRenderer extends DefaultListCellRenderer
    {
        //Changing the selected item renderer to orange
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
        {
            Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (isSelected)
            {
                component.setBackground(Color.ORANGE);
            }
            else
            {
                component.setBackground(Color.WHITE);
            }

            component.setForeground(Color.BLACK);

            return component;
        }
    }
    
    /***
     * This method tries to find the path
     */
    public static List<List<Integer>> pathfinder(int[][] A)
    {

        Queue<Integer> indexes = new PriorityQueue<Integer>();
        List<List<Integer>> paths = new LinkedList<List<Integer>>();

        for (int j = 0 ; j < A.length ; j++)
        {
            if (A[0][j] == 1)
            {
                indexes.add(j);

                List<Integer> tempPath = new LinkedList<Integer>();
                tempPath.add(0);
                tempPath.add(j);

                paths.add(tempPath);
            }
        }

        while (!indexes.isEmpty())
        {
            int idx = indexes.poll();

            if (idx >= A.length) { continue; }

            for (int k = 0 ; k < A[idx].length ; k++)
            {
                if (A[idx][k] == 1)
                {

                    int i = 0;

                    List<List<Integer>> tempPaths = new LinkedList<List<Integer>>(paths);

                    while (i < tempPaths.size())
                    {
                        if (idx == tempPaths.get(i).get(tempPaths.get(i).size() - 1))
                        {

                            List<Integer> temp = new LinkedList<Integer>(paths.get(i));
                            temp.add(k);
                            paths.add(temp);
                        }
                        i += 1;
                    }
                    indexes.add(k);
                }
            }
        }

        return paths;

    }

    /***
     * This method takes an adjacency matrix A and returns a list of all complete paths.
     */
    public static List<List<Integer>> setOfAllCompletePaths(int[][] A)
    {

        List<List<Integer>> paths = pathfinder(A);

        List<List<Integer>> completePaths = new LinkedList<List<Integer>>();

        for (List<Integer> path : paths)
        {
            int vertex = path.get(path.size() - 1);

            int sum = IntStream.of(A[vertex]).sum();

            // Check if the current vertex has no outgoing edges, indicating a complete path
            if (sum == 0) { completePaths.add(path); }
        }

        return completePaths;

    }

    /***
     * This method constructs an adjacency matrix A from a map of nodes and a list of edges.
     */
    public static int[][] adjacencyMatrix(Map<Integer, List<Pair<Integer, Integer>>> nodesMap, List<Pair<Integer, Integer>> edgeList)
    {

        // Define the adjacency matrix A:
        int[][] A = new int[nodesMap.keySet().size()][nodesMap.keySet().size()];

        // Initialize the adjacency matrix A (all values to 0):
        for (int i = 0; i < A.length; i++) { for (int j = 0; j < A.length; j++) { A[i][j] = 0; } }

        // Populate the adjacency matrix based on the edge list
        for (Pair<Integer, Integer> e : edgeList) { A[e.getValue0()][e.getValue1()] = 1; }

        return A;

    }

    /***
     * This method converts a class expression (given as a string C) into a tree structure.
     */
    public static DescriptionTree expr2tree(String C, char inputted_vertex_notation)
    {

        DescriptionTree T = new DescriptionTree(inputted_vertex_notation);

        Queue<Pair<String, Integer>> queue = new PriorityQueue<>();

        int index_v = 0;
        int index_u = 1;

        queue.add(new Pair<>(C, index_v));

        // While there are nodes in the queue, parse the expression and build the tree
        while (!queue.isEmpty())
        {
            Pair<String, Integer> pair = queue.poll();

            String current_C = pair.getValue0();

            int idx_v = pair.getValue1();

            // Get the conjuncts and break them down into expression parts
            List<String> result = getConjuncts(current_C);

            List<List<String>> expressionParts = getExpressionParts(result);

            System.out.println("current_C = " + current_C);
            System.out.println("result = " + result);
            System.out.println("expressionParts = " + expressionParts);

            // Process each part of the expression and add edges and vertices to the tree
            for (List<String> part : expressionParts)
            {
                System.out.println(part);

                String edge_label = "";
                String concepts_labels = "";

                // Parse the edge and concept labels from the expression
                if (part.size() == 1)
                {

                    String[] arr = part.get(0).split(" some ");

                    edge_label = arr[0];
                    concepts_labels = arr[1];
                }
                else
                {
                    for (int x = 0 ; x < part.get(0).length() ; x++) { if (part.get(0).charAt(x) == ' ') { break; } edge_label += part.get(0).charAt(x); }

                    for (int x = 0 ; x < part.get(1).length() ; x++)
                    {
                        if (part.get(1).charAt(x) == '(') { break; }

                        concepts_labels += part.get(1).charAt(x);
                    }
                }

                String[] arr = concepts_labels.split(" and ");

                LinkedList<String> label_v = getTopLevelConjuncts(current_C);
                LinkedList<String> label_u = new LinkedList<String>();

                for (int x = 0 ; x < arr.length ; x++)
                {
                    label_u.add(arr[x]);
                }

                char edge_label_char = Character.MIN_VALUE;

                if (edge_label.length() == 1)
                {
                    edge_label_char = edge_label.charAt(0);
                }

                // Add an edge to the tree
                System.out.println(edge_label_char + " / " + label_v  + " / " + label_u  + " / " + idx_v  + " / " + index_u);
                T.addEdge(idx_v, index_u, edge_label_char, label_v, label_u);

                // Continue processing if there are more parts of the expression
                if (part.size() != 1)
                {
                    if (!part.get(1).isEmpty())
                    {
                        queue.add(new Pair<>(part.get(1), index_u));
                    }
                }

                index_u++;

                for (Pair<String, Integer> el : queue)
                {
                    System.out.println("el = " + el);
                }
                System.out.println("===");

            }
        }

        return T;

    }

    /***
     * This method returns the top-level conjuncts (concepts) from a class expression string C.
     */
    public static LinkedList<String> getTopLevelConjuncts(String C)
    {

        // Handle the case where the expression starts with parentheses, signifying a conjunction
        if (C.charAt(0) == '(')
        {
            LinkedList<String> label_v = new LinkedList<String>();
            label_v.add("T");
            return label_v;
        }

        LinkedList<String> label_v = new LinkedList<String>();

        String current_concept_label = "";

        // Parse the top-level concept label before any parentheses
        for (int x = 0 ; x < C.length() ; x++)
        {
            if (C.charAt(x) == '(') { break; }
            current_concept_label += C.charAt(x);
        }

        // Split the concept by "and" to get individual conjuncts
        String[] arr_currentConcept = current_concept_label.split(" and ");

        for (int x = 0 ; x < arr_currentConcept.length ; x++)
        {
            if (!arr_currentConcept[x].equals(" ") || !arr_currentConcept[x].equals("") || !arr_currentConcept[x].equals(null))
            {
                label_v.add(arr_currentConcept[x].replaceAll(" ", ""));
            }
        }

        return label_v;

    }

    /***
     * This method returns a list of conjuncts (sub-expressions) from a class expression string C.
     */
    public static List<String> getConjuncts(String C)
    {

        int x = 0;
        int p = 0;
        int start_idx = 0;
        int end_idx = 0;
        List<String> result = new LinkedList<String>();

        // Traverse the string and extract expressions enclosed by parentheses
        while (x < C.length())
        {

            if (C.charAt(x) == '(' && p == 0) { start_idx = x; }

            if (C.charAt(x) == '(') { p++; }

            if (C.charAt(x) == ')') { p--; }

            // Once we've closed a set of parentheses, add the sub-expression to the result
            if (C.charAt(x) == ')' && p == 0)
            {
                end_idx = x;
                result.add(C.substring(start_idx + 1, end_idx));
            }

            x++;

        }

        return result;

    }

    /***
     * This method takes a list of class expression strings (result) and breaks each expression into its respective components (edge labels and concepts).
     */
    public static List<List<String>> getExpressionParts(List<String> result)
    {

        List<List<String>> expressionConj = new LinkedList<List<String>>();

        // Loop through each expression in the result list
        for (String expr : result)
        {

            List<String> temp = new LinkedList<String>();

            // If there are no parentheses, treat the entire expression as a single concept
            if (!expr.contains("("))
            {
                temp.add(expr);
                expressionConj.add(temp);
                continue;
            }

            // Otherwise, split the expression at the parentheses to separate the edge label and concepts
            for (int x = 0 ; x < expr.length() ; x++)
            {

                char character = expr.charAt(x);

                if (character == '(')
                {
                    temp.add(expr.substring(0, x - 1));
                    temp.add(expr.substring(x + 1, expr.length() - 1));
                    break;
                }
            }
            expressionConj.add(temp);
        }

        // Return the list of expression components (edge labels and concepts)
        return expressionConj;

    }

    /***
     * This method takes two DescriptionTrees, T1 and T2, and displays them as graphs.
     */
    public static void displayTrees(DescriptionTree T1, DescriptionTree T2)
    {

        // Initialize the graph for T1 with custom styling
        Graph graph_T1 = new SingleGraph("T1");

        graph_T1.setAttribute("ui.stylesheet", "node{\n" +
                "    size: 10px, 10px;\n" +
                "    fill-color: #e6b400;\n" +
                "    text-mode: normal; \n" +
                "}" + "edge{\n" +
                "    text-offset: -10,-10;\n" +
                "    text-alignment:above;\n" +
                "    fill-color: #4169e1;\n" +
                "    text-mode: normal; \n" +
                "}" + "node#0 {\r\n" +
                "	fill-color: #08a045;\r\n" + "}" + "node:clicked {\r\n" + "	fill-color: red;\r\n" + "}"
                + "graph {\r\n"
                + "	fill-color: lightgray;\r\n"
                + "}");

        // Add nodes to the graph for T1
        for (Map.Entry<Integer, DTNode> entry : T1.V.entrySet())
        {
            DTNode node = entry.getValue();
            graph_T1.addNode(entry.getKey().toString()).setAttribute("ui.label", node.name);
        }

        // Add edges to the graph for T1
        int edge_counter = 0;

        for (Map.Entry<Pair<Integer, Integer>, Character> entry : T1.E.entrySet())
        {
            Pair<Integer, Integer> edge = entry.getKey();

            String node_v = edge.getValue0().toString();
            String node_u = edge.getValue1().toString();
            String edge_label = entry.getValue().toString();

            graph_T1.addEdge(edge_label + "(" + edge_counter + ")", node_v, node_u, true).setAttribute("ui.label", edge_label);
            edge_counter++;
        }

        // Initialize the graph for T2 with custom styling
        Graph graph_T2 = new SingleGraph("T2");

        graph_T2.setAttribute("ui.stylesheet", "node{\n" +
                "    size: 10px, 10px;\n" +
                "    fill-color: #e6b400;\n" +
                "    text-mode: normal; \n" +
                "}" + "edge{\n" +
                "    text-offset: -10,-10;\n" +
                "    text-alignment:above;\n" +
                "    fill-color: #4169e1;\n" +
                "    text-mode: normal; \n" +
                "}" + "node#0 {\r\n" +
                "	fill-color: #08a045;\r\n" + "}" + "node:clicked {\r\n" + "	fill-color: red;\r\n" + "}"
                + "graph {\r\n"
                + "	fill-color: lightgray;\r\n"
                + "}");

        // Add nodes to the graph for T2
        for (Map.Entry<Integer, DTNode> entry : T2.V.entrySet())
        {
            DTNode node = entry.getValue();
            graph_T2.addNode(entry.getKey().toString()).setAttribute("ui.label", node.name);
        }

        // Add edges to the graph for T2
        int edge_counter1 = 0;

        for (Map.Entry<Pair<Integer, Integer>, Character> entry : T2.E.entrySet())
        {
            Pair<Integer, Integer> edge = entry.getKey();

            String node_v = edge.getValue0().toString();
            String node_u = edge.getValue1().toString();
            String edge_label = entry.getValue().toString();

            graph_T2.addEdge(edge_label + "(" + edge_counter1 + ")", node_v, node_u, true).setAttribute("ui.label", edge_label);
            edge_counter1++;
        }

        // Display T1 using a hierarchical layout and allow closing the graph window
        Viewer viewer1 = graph_T1.display();
        viewer1.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
        viewer1.enableAutoLayout(new HierarchicalLayout());

        // Display T2 using a hierarchical layout and allow closing the graph window
        Viewer viewer2 = graph_T2.display();
        viewer2.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
        viewer2.enableAutoLayout(new HierarchicalLayout());

    }

    /**
     * Adds styled text to the given JTextPane, making words inside double quotes bold.
     */
    public static void addStyledText(JTextPane textPane, String text)
    {

        StyledDocument doc = textPane.getStyledDocument();

        // Create a regular style
        Style regular = textPane.addStyle("Regular", null);
        StyleConstants.setFontFamily(regular, "SansSerif");

        // Create a bold and orange style
        Style boldOrange = textPane.addStyle("BoldOrange", regular);
        StyleConstants.setBold(boldOrange, true);
        StyleConstants.setForeground(boldOrange, Color.ORANGE);

        // Split the text and apply styles
        String[] parts = text.split("\"");

        boolean isBold = false;

        for (String part : parts)
        {
            try
            {
                if (isBold)
                {
                    doc.insertString(doc.getLength(), "\"" + part + "\"", boldOrange);
                }
                else
                {
                    doc.insertString(doc.getLength(), part, regular);
                }
                isBold = !isBold;  // Toggle the bold flag
            }
            catch (BadLocationException e)
            {
                e.printStackTrace();
            }
        }

    }
}



