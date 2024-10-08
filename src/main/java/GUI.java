import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.util.*;


public class GUI extends JFrame
{

    // ------------------ Components ------------------
    private final JList<String> ontologyList; // List to display loaded ontologies
    private final DefaultListModel<String> ontologyListModel;

    // Text areas for the details Panel
    private JTextArea axiomsTextArea = null;
    private JTextArea ontologyIRITextArea = null;
    private JTextArea numberOfAxiomsTextArea = null;

    // Maps to store ontology data and paths
    private final Map<String, owlFunctions.OntologyData> ontologyDataMap = new HashMap<>();
    private final Map<String, String> ontologyPaths = new HashMap<>();

    public GUI()
    {

        // ------------------ Frame setup ------------------
        setTitle("Ontology Tool");
        setSize(1100, 600); // Size of the window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Stop the program when I close the window

        // ------------------ Top button panel setup ------------------
        JPanel buttonPanel = new JPanel(new GridLayout(1, 6, 20, 10));

        // Creating buttons
        JButton loadButton = new JButton("Load Ontology");
        JButton removeButton = new JButton("Remove Ontology");
        JButton helpButton = new JButton("Help");
        JButton createHypothesisButton = new JButton("Create Hypothesis");
        JButton generateExplanationsButton = new JButton("Generate Explanations");
        JButton startReasonerButton = new JButton("Start Reasoner");

        // Set the background color of the buttons
        loadButton.setBackground(Color.ORANGE);
        removeButton.setBackground(Color.ORANGE);
        helpButton.setBackground(Color.BLACK);
        createHypothesisButton.setBackground(Color.ORANGE);
        generateExplanationsButton.setBackground(Color.ORANGE);
        startReasonerButton.setBackground(Color.ORANGE);

        // Set the text color of the "Help" button to orange
        helpButton.setForeground(Color.ORANGE);

        // Adding buttons to the button panel
        buttonPanel.add(loadButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(createHypothesisButton);
        buttonPanel.add(generateExplanationsButton);
        buttonPanel.add(startReasonerButton);
        buttonPanel.add(helpButton);

        // Create a wrapper panel for margins and add the button panel to it
        JPanel buttonPanelWrapper = new JPanel(new BorderLayout());
        buttonPanelWrapper.setBorder(new EmptyBorder(10, 20, 10, 20)); // top, left, bottom, right
        buttonPanelWrapper.add(buttonPanel, BorderLayout.CENTER);

        // Add the wrapper panel to the top of the window
        add(buttonPanelWrapper, BorderLayout.NORTH);


        // ------------------ West list panel setup ------------------
        // Initialize the model for the ontology list and then create the ontology list using the model
        ontologyListModel = new DefaultListModel<>();
        ontologyList = new JList<>(ontologyListModel);

        // Create a scroll pane for the ontology list
        JScrollPane scrollPane = new JScrollPane(ontologyList);
        scrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.ORANGE, 2), " Loaded Ontologies "));
        JPanel listPanel = new JPanel(new BorderLayout());

        // Add the scroll pane to the center of the panel
        listPanel.add(scrollPane, BorderLayout.CENTER);
        listPanel.setPreferredSize(new Dimension(370, 300));

        // Create a wrapper panel for margins and add the list panel to it
        JPanel listPanelWrapper = new JPanel(new BorderLayout());
        listPanelWrapper.setBorder(new EmptyBorder(10, 20, 10, 5)); // top, left, bottom, right
        listPanelWrapper.add(listPanel, BorderLayout.CENTER);

        // Add the wrapper panel to the west side of the window
        add(listPanelWrapper, BorderLayout.WEST);

        // ------------------ Details panel setup ------------------
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));

        // Ontology IRI TextArea setup
        ontologyIRITextArea = new JTextArea(1, 30);
        ontologyIRITextArea.setEditable(false);
        JScrollPane IriScrollPane = new JScrollPane(ontologyIRITextArea);
        IriScrollPane.setPreferredSize(new Dimension(1200, 50));
        IriScrollPane.setMaximumSize(IriScrollPane.getPreferredSize());
        IriScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.ORANGE, 2), " Ontology IRI "));
        detailsPanel.add(IriScrollPane);

        // Number of Axioms TextArea setup
        numberOfAxiomsTextArea = new JTextArea(1, 30);
        numberOfAxiomsTextArea.setEditable(false);
        JScrollPane numberOfAxiomsScrollPane = new JScrollPane(numberOfAxiomsTextArea);
        numberOfAxiomsScrollPane.setPreferredSize(new Dimension(1200, 50));
        numberOfAxiomsScrollPane.setMaximumSize(numberOfAxiomsScrollPane.getPreferredSize());
        numberOfAxiomsScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.ORANGE, 2), " Number Of Axioms "));
        detailsPanel.add(numberOfAxiomsScrollPane);

        // Axioms TextArea setup
        axiomsTextArea = new JTextArea(10, 30);
        axiomsTextArea.setEditable(false);
        JScrollPane axiomsScrollPane = new JScrollPane(axiomsTextArea);
        axiomsScrollPane.setPreferredSize(new Dimension(1200, 1000));
        axiomsScrollPane.setMaximumSize(axiomsScrollPane.getPreferredSize());
        axiomsScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.ORANGE, 2), " Axioms "));
        detailsPanel.add(axiomsScrollPane);

        // Ensure components inside detailsPanel are in center
        for (Component comp : detailsPanel.getComponents()) {((JComponent) comp).setAlignmentX(Component.CENTER_ALIGNMENT);}

        // Create a wrapper panel for margins and add the details panel to it
        JPanel detailsPanelWrapper = new JPanel(new BorderLayout());
        detailsPanelWrapper.setBorder(new EmptyBorder(10, 5, 10, 20)); // top, left, bottom, right
        detailsPanelWrapper.add(detailsPanel, BorderLayout.CENTER);

        // Add the wrapper panel to the center of the window
        add(detailsPanelWrapper, BorderLayout.CENTER);

        setVisible(true);

        // Setting colors for every OptionPane of the code
        UIManager.put("OptionPane.messageForeground", Color.BLACK);
        UIManager.put("Button.background", Color.ORANGE);
        UIManager.put("Button.foreground", Color.BLACK);


        /**
         * --------------------------- Load button -------------------------------
         * When the loadButton is clicked it opens the computer files and you can choose to load an ontology file
         */
        loadButton.addActionListener(load ->
        {

            // Create a file chooser
            JFileChooser fileChooser = new JFileChooser();

            // Show the open dialog and capture the user's choice
            int returnValue = fileChooser.showOpenDialog(null);

            // If the user selects a file and clicks "Open"
            if (returnValue == JFileChooser.APPROVE_OPTION)
            {

                // Get the selected file
                File selectedFile = fileChooser.getSelectedFile();

                try
                {

                    // Get the file name and the absolute file path
                    String fileName = selectedFile.getName();
                    String filePath = selectedFile.getAbsolutePath();

                    // Check if the selected file has a valid ontology file extension and it's not already loaded
                    if ((fileName.endsWith(".owl") || fileName.endsWith(".owx") || fileName.endsWith(".ttl"))
                            && !ontologyListModel.contains(fileName))
                    {

                        // Add the file name to the ontology list model
                        ontologyListModel.addElement(fileName);

                        // Store the path
                        ontologyPaths.put(fileName, filePath);

                        // Load the ontology using owlUtils
                        OWLOntology ontology = owlUtils.loadOntology(filePath);

                        // Retrieve the ontology IRI
                        IRI ontologyIRI = owlUtils.getOntologyIRI(ontology);

                        // Retrieve the set of axioms in the ontology
                        Set<OWLAxiom> axioms = owlUtils.getSetOfOntoAxioms(ontology);

                        // Get the number of axioms
                        int numberOfAxioms = axioms.size();

                        // Store ontology data in the ontologyDataMap with the file name as the key
                        ontologyDataMap.put(fileName, new owlFunctions.OntologyData(ontologyIRI, numberOfAxioms, axioms));

                        // Show a success message
                        JOptionPane.showMessageDialog(null, "Ontology loaded successfully!");

                    }
                    else
                    {
                        // Show an error message if the selected file is not an ontology file or already loaded
                        JOptionPane.showMessageDialog(null, "Failed to load. Not an ontology file or already loaded.", "Load Failed", JOptionPane.ERROR_MESSAGE);
                    }

                }
                catch (OWLOntologyCreationException e)
                {

                    // Show an error message if loading the ontology fails
                    JOptionPane.showMessageDialog(null, "Failed to load ontology: " + e.getMessage(), "Load Failed", JOptionPane.ERROR_MESSAGE);

                }

            }

        });



        /**
         * --------------------------- Remove button -------------------------------
         * When the removeButton is clicked it removes the ontology from the list and the manager
         */
        removeButton.addActionListener(remove ->
        {
            // Get the index of the selected ontology in the list
            int selectedIndex = ontologyList.getSelectedIndex();

            // If an ontology is selected
            if (selectedIndex != -1)
            {
                // Get the name of the ontology file
                String ontologyFileName = ontologyListModel.getElementAt(selectedIndex);

                //Remove the path of the ontology from the map
                ontologyPaths.remove(ontologyFileName);

                // Remove the ontology from the list model
                ontologyListModel.remove(selectedIndex);

                // Clear all the text areas
                ontologyIRITextArea.setText(null);
                numberOfAxiomsTextArea.setText(null);
                axiomsTextArea.setText(null);

                try
                {
                    // Find and remove the ontology from the ontology manager using owlUtils remove function
                    owlUtils.removeOntology(ontologyFileName);

                    // Display a success message
                    JOptionPane.showMessageDialog(null, "Ontology removed successfully!");
                }
                catch (Exception e)
                {
                    // Show an error message if ontology removal fails
                    JOptionPane.showMessageDialog(null, "Failed to remove ontology: " + e.getMessage(), "Remove Failed", JOptionPane.ERROR_MESSAGE);
                    System.out.println(e);
                }
            }
            else
            {
                // Show a message if no ontology is selected
                JOptionPane.showMessageDialog(null, "No selected ontology!");
            }
        });


        /**
         * --------------------------- Ontology List -------------------------------
         * This listener is triggered when an item in the ontologyList is selected
         */
        ontologyList.setCellRenderer(new owlFunctions.OntologyListRenderer());

        ontologyList.addListSelectionListener(e ->
        {

            // Check the selection
            if (!e.getValueIsAdjusting())
            {

                int selectedIndex = ontologyList.getSelectedIndex();

                // Check if an item is selected
                if (selectedIndex != -1)
                {

                    // Get the path
                    String ontologyFilePath = ontologyListModel.getElementAt(selectedIndex);

                    // Get ontology's data from the map
                    owlFunctions.OntologyData data = ontologyDataMap.get(ontologyFilePath);

                    if (data != null)
                    {

                        // Display details if the ontology has no axioms
                        if (data.getNumberOfAxioms() == 0)
                        {

                            ontologyIRITextArea.setText(data.getOntologyIRI().toString());
                            numberOfAxiomsTextArea.setText("0");
                            axiomsTextArea.setText("There aren't axioms in this ontology!");

                        }
                        else
                        {

                            // Display details for ontologies with axioms
                            ontologyIRITextArea.setText(data.getOntologyIRI().toString());
                            numberOfAxiomsTextArea.setText(String.valueOf(data.getNumberOfAxioms()));

                            StringBuilder axiomsText = new StringBuilder();

                            for (OWLAxiom axiom : data.getAxioms())
                            {

                                // Remove all the IRIs
                                String cleanedAxiomString = axiom.toString().replaceAll("http.*?#", "");
                                axiomsText.append(cleanedAxiomString).append("\n");

                            }

                            axiomsTextArea.setText(axiomsText.toString());

                        }

                    }
                    else
                    {

                        // If no data is available, load ontology data and display it
                        try
                        {

                            // Load Ontology
                            OWLOntology ontology = owlUtils.loadOntology(ontologyFilePath);

                            // Get the IRI
                            IRI ontologyIRI = owlUtils.getOntologyIRI(ontology);

                            // Get the axioms
                            Set<OWLAxiom> axioms = owlUtils.getSetOfOntoAxioms(ontology);

                            // Get the number of axioms
                            int numberOfAxioms = axioms.size();

                            // Put the details in their text areas
                            ontologyIRITextArea.setText(ontologyIRI.toString());
                            numberOfAxiomsTextArea.setText(String.valueOf(numberOfAxioms));

                            // Create a builder to Display the axioms
                            StringBuilder axiomsText = new StringBuilder();

                            for (OWLAxiom axiom : axioms)
                            {
                                // Remove all the IRIs
                                String cleanedAxiomString = axiom.toString().replaceAll("http.*?#", "");
                                axiomsText.append(cleanedAxiomString).append("\n");
                            }

                            axiomsTextArea.setText(axiomsText.toString());

                            // Store the loaded ontology data in the ontologyDataMap
                            ontologyDataMap.put(ontologyFilePath, new owlFunctions.OntologyData(ontologyIRI, numberOfAxioms, axioms));

                        }
                        catch (OWLOntologyCreationException ex)
                        {

                            // Error message
                            JOptionPane.showMessageDialog(null, "Failed to load ontology: " + ex.getMessage(), "Load Failed", JOptionPane.ERROR_MESSAGE);

                        }

                    }

                }

            }

        });


        /**
         * --------------------------- Hypothesis Button -------------------------------
         * When the createHypothesisButton is clicked, it opens a new window where we can add axioms in a new ontology
         */
        createHypothesisButton.addActionListener(e -> {

            JFrame hypothesisFrame = new JFrame("Create Hypothesis");
            hypothesisFrame.setSize(600, 400);
            JPanel hypothesisPanel = new JPanel(new BorderLayout(10, 10));
            hypothesisPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


            // Create manager for ontology:
            OWLOntologyManager manager = owlUtils.createOntologyManager();

            // Get the data factory:
            OWLDataFactory dataFactory = owlUtils.createDataFactory();

            // Create a unique IRI for the new ontology:
            UUID uuid = UUID.randomUUID();
            IRI ontologyIRI = IRI.create("http://example.org/ontology/" + uuid);

            // Create an empty ontology with the unique IRI:
            OWLOntology ontology;

            try
            {

                ontology = manager.createOntology(ontologyIRI);

            }
            catch (OWLOntologyCreationException ex)
            {

                JOptionPane.showMessageDialog(hypothesisFrame, "Failed to create new ontology: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;

            }


            // Axiom panel
            JPanel axiomPanel = new JPanel(new BorderLayout(5, 5));
            JLabel axiomLabel = new JLabel("Axiom:");
            JTextArea axiomTextArea = new JTextArea(1, 20);
            axiomTextArea.setLineWrap(true);
            JScrollPane axiomScrollPane = new JScrollPane(axiomTextArea);
            axiomScrollPane.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 2));
            axiomPanel.add(axiomLabel, BorderLayout.WEST);
            axiomPanel.add(axiomScrollPane, BorderLayout.CENTER);

            // Buttons panel with FlowLayout to control spacing
            JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            Dimension buttonSize = new Dimension(175, 25);

            JButton addButton = new JButton("Add");
            addButton.setPreferredSize(buttonSize);
            addButton.setBackground(Color.ORANGE);
            buttonsPanel.add(addButton);

            JButton removeAxiomButton = new JButton("Remove");
            removeAxiomButton.setPreferredSize(buttonSize);
            removeAxiomButton.setBackground(Color.ORANGE);
            buttonsPanel.add(removeAxiomButton);

            JButton undoButton = new JButton("Undo");
            undoButton.setBackground(Color.ORANGE);
            undoButton.setPreferredSize(buttonSize);
            buttonsPanel.add(undoButton);

            // Top panel containing axiom panel and buttons panel
            JPanel topPanel = new JPanel();
            topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
            topPanel.add(axiomPanel);
            topPanel.add(buttonsPanel);

            // Axioms display area
            DefaultListModel<String> axiomListModel = new DefaultListModel<>();
            JList<String> axiomList = new JList<>(axiomListModel);
            JScrollPane axiomListScrollPane = new JScrollPane(axiomList);
            axiomListScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.ORANGE, 2), " Added Axioms "));

            // Additional buttons panel for Save & Exit and Legend buttons
            JPanel additionalButtonsPanel = new JPanel(new BorderLayout(10, 10));
            Dimension smallButtonSize = new Dimension(175, 25);

            JButton saveExitButton = new JButton("Save & Exit");
            saveExitButton.setBackground(Color.ORANGE);
            saveExitButton.setPreferredSize(smallButtonSize);
            additionalButtonsPanel.add(saveExitButton, BorderLayout.EAST);

            JButton legendButton = new JButton("Legend");
            legendButton.setBackground(Color.BLACK);
            legendButton.setForeground(Color.ORANGE);
            legendButton.setPreferredSize(smallButtonSize);
            additionalButtonsPanel.add(legendButton, BorderLayout.WEST);

            // Add components to the guider panel
            hypothesisPanel.add(topPanel, BorderLayout.NORTH);
            hypothesisPanel.add(axiomListScrollPane, BorderLayout.CENTER);
            hypothesisPanel.add(additionalButtonsPanel, BorderLayout.SOUTH);

            hypothesisFrame.add(hypothesisPanel);
            hypothesisFrame.setVisible(true);

            // -------------- ADD AXIOM BUTTON ---------------
            addButton.addActionListener(e15 ->
            {

                // Trims the input
                String axiomInput = axiomTextArea.getText().trim();

                if (!axiomInput.isEmpty())
                {

                    try
                    {

                        if (owlFunctions.handleUserInput(axiomInput, ontology, manager, dataFactory, ontologyIRI))
                        {

                            // Change renderer color
                            axiomList.setCellRenderer(new owlFunctions.OntologyListRenderer());

                            axiomListModel.addElement(axiomInput);

                        }

                        axiomTextArea.setText(""); // Clear the text area after adding

                    }
                    catch (Exception ex)
                    {

                        JOptionPane.showMessageDialog(hypothesisFrame, "Error adding axiom: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);

                    }
                }
                else
                {

                    JOptionPane.showMessageDialog(hypothesisFrame, "Axiom input cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);

                }

            });

            // Stack for storing removed axioms for undo functionality
            Stack<String> removedAxioms = new Stack<>();

            // -------------- REMOVE AXIOM BUTTON ---------------
            removeAxiomButton.addActionListener(e14 ->
            {

                int selectedIdx = axiomList.getSelectedIndex();

                if (selectedIdx != -1)
                {

                    // Remove axiom from the list
                    String removedAxiom = axiomListModel.remove(selectedIdx);

                    // Remove axiom from the ontology
                    owlFunctions.removeAxiomButtonFunction(removedAxiom, ontology, manager, dataFactory, ontologyIRI);

                    // Store the removed axiom for undo
                    removedAxioms.push(removedAxiom);

                }
            });

            // ------------------- UNDO REMOVED AXIOM BUTTON---------------------
            undoButton.addActionListener(e13 ->
            {

                if (!removedAxioms.isEmpty())
                {

                    // Retrieve the last removed axiom
                    String lastRemovedAxiom = removedAxioms.pop();

                    // Add it back to the list
                    axiomListModel.addElement(lastRemovedAxiom);

                    // Add it back to the ontology
                    owlFunctions.handleUserInput(lastRemovedAxiom, ontology, manager, dataFactory, ontologyIRI);

                }

            });

            // ------------------- SAVE & EXIT BUTTON -------------------
            saveExitButton.addActionListener(e12 ->
            {

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Specify a file to save the ontology");

                // Suggest a default file name
                fileChooser.setSelectedFile(new File("MyHypothesis.owl"));

                // Filter only for .owl files
                FileNameExtensionFilter filter = new FileNameExtensionFilter("OWL Ontologies (*.owl)", "owl");
                fileChooser.addChoosableFileFilter(filter);
                fileChooser.setFileFilter(filter);

                int userSelection = fileChooser.showSaveDialog(hypothesisFrame);

                if (userSelection == JFileChooser.APPROVE_OPTION)
                {

                    File fileToSave = fileChooser.getSelectedFile();

                    // Ensure it has a .owl extension
                    if (!fileToSave.getPath().toLowerCase().endsWith(".owl"))
                    {

                        fileToSave = new File(fileToSave.getPath() + ".owl");

                    }

                    try
                    {

                        // Specify the format
                        OWLDocumentFormat format = new RDFXMLDocumentFormat();

                        // Save the ontology to the specified file
                        manager.saveOntology(ontology, format, IRI.create(fileToSave.toURI()));

                        JOptionPane.showMessageDialog(hypothesisFrame, "Ontology saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

                        hypothesisFrame.dispose(); // Close the window after saving

                    }
                    catch (OWLOntologyStorageException ex)
                    {

                        // Error message
                        JOptionPane.showMessageDialog(hypothesisFrame, "Failed to save the ontology: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);

                    }

                }
            });


            // ------------------- LEGEND BUTTON ------------------------
            legendButton.addActionListener(e16 ->
            {

                // Create a new JFrame for the legend window
                JFrame legendFrame = new JFrame("Axiom Writing Legend");
                legendFrame.setSize(400, 300);

                JPanel legendPanel = new JPanel(new BorderLayout(10, 10));
                legendPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                // Create the text pane and set the border
                JTextPane legendTextPane = new JTextPane();
                legendTextPane.setBorder(new EmptyBorder(10, 10, 10, 10));
                legendTextPane.setEditable(false);

                // Text to be displayed
                String legendText = """
                        1. C subClassOf D
                        2. r some C subClassOf D
                        3. C subClassOf r some D
                        4. r some C subClassOf s some D
                        """;

                // Define a regular style
                StyledDocument doc = legendTextPane.getStyledDocument();
                Style regular = doc.addStyle("Regular", null);
                StyleConstants.setForeground(regular, Color.BLACK);
                StyleConstants.setFontFamily(regular, "SansSerif");
                StyleConstants.setFontSize(regular, 14);

                // Define a yellow style for keywords
                Style keyword = doc.addStyle("Keyword", null);
                StyleConstants.setForeground(keyword, Color.ORANGE);
                StyleConstants.setBold(keyword, true);

                // Split the text and apply styles
                String[] words = legendText.split(" ");
                for (String word : words)
                {
                    try
                    {
                        if (word.equals("some") || word.equals("subClassOf"))
                        {
                            doc.insertString(doc.getLength(), word + " ", keyword);
                        }
                        else
                        {
                            doc.insertString(doc.getLength(), word + " ", regular);
                        }
                    }
                    catch (BadLocationException ex)
                    {
                        ex.printStackTrace();
                    }
                }

                // Create a label for the title
                JLabel legendTitleLabel = new JLabel("How to write Axioms");
                legendTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
                legendTitleLabel.setForeground(Color.ORANGE);
                legendTitleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));  // Add some space below the title

                // Add the title and text pane to the panel
                legendPanel.add(legendTitleLabel, BorderLayout.NORTH);
                JScrollPane legendScrollTextPanel = new JScrollPane(legendTextPane);
                legendScrollTextPanel.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 2));
                legendPanel.add(legendScrollTextPanel, BorderLayout.CENTER);

                legendFrame.add(legendPanel);
                legendFrame.setVisible(true);

            });


        });


        /**
         * --------------------------- Start Reasoner Button -------------------------------
         * When this button is clicked, it creates a new ontology which is the selected ontology but synchronized
         */
        startReasonerButton.addActionListener(e ->
        {

            try
            {

                // Check if an ontology has been selected from the list
                int selectedIndex = ontologyList.getSelectedIndex();

                // Store the file path of the selected ontology from the list model
                String ontologyFileName = ontologyListModel.getElementAt(selectedIndex);
                String ontologyFilePath = ontologyPaths.get(ontologyFileName);

                // Check if the ontology file exists and is readable
                File ontologyFile = new File(ontologyFilePath);

                if (!ontologyFile.exists() || !ontologyFile.canRead())
                {

                    JOptionPane.showMessageDialog(null, "Failed to find or read the ontology file: " + ontologyFilePath);

                }

                // Load the selected ontology
                OWLOntology selectedOntology = owlUtils.loadOntology(ontologyFilePath);

                // Get the ontology manager
                OWLOntologyManager manager = selectedOntology.getOWLOntologyManager();

                // Synchronize the ontology using the reasoner
                owlUtils.syncReasoner(manager, selectedOntology, ontologyFilePath);

                // Message that the ontology has been synchronized and saved successfully
                JOptionPane.showMessageDialog(null, "Ontology synchronized and saved successfully!");
            }
            catch (Exception ex)
            {

                // Catch any exceptions
                JOptionPane.showMessageDialog(null, "No ontology selected for synchronization OR an error occurred during synchronization ");

            }

        });


        /**
         * --------------------------- Generate Explanations Button -------------------------------
         * When this button is clicked, it creates a new ontology which is the selected ontology but synchronized
         */
        generateExplanationsButton.addActionListener(e ->
        {

            // Check if an ontology is selected
            int selectedIndex = ontologyList.getSelectedIndex();

            if (selectedIndex == -1)
            {
                JOptionPane.showMessageDialog(null, "No ontology selected!");
            }
            else
            {
                // Frame set up
                JFrame explanationFrame = new JFrame("Generate Explanations");
                explanationFrame.setSize(600, 400);
                explanationFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Close the window properly

                JPanel explanationPanel = new JPanel(new GridBagLayout());
                explanationPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(5, 5, 5, 5);

                // Add the new label at the top
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.gridwidth = 3;
                gbc.anchor = GridBagConstraints.CENTER;
                JLabel titleLabel = new JLabel("Is the axiom entailed in the ontology or not?");
                titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
                titleLabel.setForeground(Color.ORANGE);
                explanationPanel.add(titleLabel, gbc);

                // Axiom panel
                gbc.gridy = 1;
                gbc.gridwidth = 1;
                gbc.anchor = GridBagConstraints.WEST;
                JLabel axiomLabel = new JLabel("Axiom:");
                explanationPanel.add(axiomLabel, gbc);

                gbc.gridx = 1;
                gbc.weightx = 1.0;
                gbc.fill = GridBagConstraints.HORIZONTAL;

                JTextArea axiomTextArea = new JTextArea()
                {
                    @Override
                    public Dimension getPreferredSize()
                    {
                        return new Dimension(super.getPreferredSize().width, 20);
                    }
                };

                axiomTextArea.setLineWrap(true);
                axiomTextArea.setWrapStyleWord(true);
                JScrollPane axiomScrollPane = new JScrollPane(axiomTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                axiomScrollPane.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 2));
                explanationPanel.add(axiomScrollPane, gbc);

                // Check button panel
                gbc.gridx = 2;
                gbc.gridy = 1;
                gbc.weightx = 0;
                gbc.fill = GridBagConstraints.NONE;
                JButton checkButton = new JButton("Check");
                checkButton.setBackground(Color.ORANGE);
                checkButton.setPreferredSize(new Dimension(80, 20)); // Set preferred size for button
                explanationPanel.add(checkButton, gbc);

                // Entailment result label panel
                gbc.gridx = 0;
                gbc.gridy = 2;
                gbc.gridwidth = 3;
                gbc.anchor = GridBagConstraints.CENTER;
                JLabel entailmentResultLabel = new JLabel();
                explanationPanel.add(entailmentResultLabel, gbc);

                // Text area for displaying justifications
                gbc.gridy = 3;
                gbc.gridwidth = 3;
                gbc.weightx = 1.0;
                gbc.weighty = 1.0;
                gbc.fill = GridBagConstraints.BOTH;
                JTextArea justificationsTextArea = new JTextArea();
                justificationsTextArea.setLineWrap(true);
                justificationsTextArea.setEditable(false);
                JScrollPane justificationsScrollPane = new JScrollPane(justificationsTextArea);
                justificationsScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.ORANGE, 2), " Explanations "));
                explanationPanel.add(justificationsScrollPane, gbc);

                explanationFrame.add(explanationPanel);
                explanationFrame.setVisible(true);

                // Add a ComponentListener to handle resizing behavior
                explanationFrame.addComponentListener(new ComponentAdapter()
                {
                    @Override
                    public void componentResized(ComponentEvent e)
                    {

                        Dimension size = explanationFrame.getSize();
                        // Check if the window is maximized

                        if (size.width >= explanationFrame.getToolkit().getScreenSize().width &&
                                size.height >= explanationFrame.getToolkit().getScreenSize().height)
                        {
                            // The window is maximized
                            gbc.fill = GridBagConstraints.HORIZONTAL;
                            explanationPanel.revalidate();
                        }

                    }

                });

                // ------------------- CHECK BUTTON ------------------------
                checkButton.addActionListener(e1 ->
                {
                    // Trims the input
                    String axiomInput = axiomTextArea.getText().trim();

                    if (!axiomInput.isEmpty())
                    {
                        // Store the file path of the selected ontology from the list model
                        String ontologyFileName = ontologyListModel.getElementAt(selectedIndex);
                        String ontologyFilePath = ontologyPaths.get(ontologyFileName);

                        // Check if the ontology file exists and is readable
                        File ontologyFile = new File(ontologyFilePath);
                        if (ontologyFile.exists() && ontologyFile.canRead())
                        {
                            try
                            {
                                // Get the ontology
                                OWLOntology selectedOntology = owlUtils.loadOntology(ontologyFilePath);

                                // Get the axiom
                                OWLAxiom axiom = owlFunctions.parseClassExpression(selectedOntology, selectedOntology.getOWLOntologyManager(), selectedOntology.getOWLOntologyManager().getOWLDataFactory(), axiomInput);

                                // Call explanations method
                                Explanations explanations = new Explanations(selectedOntology, axiom);
                                boolean isAxiomEntailed = explanations.isEntailed();

                                if (isAxiomEntailed)
                                {
                                    // Display the image and the text
                                    ImageIcon tickIcon = new ImageIcon("C:\\Users\\30693\\IdeaProjects\\OntologyTool\\pictures\\tick.jpg");
                                    Image tickImage = tickIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                                    entailmentResultLabel.setIcon(new ImageIcon(tickImage));
                                    entailmentResultLabel.setText("Axiom is entailed");

                                    try
                                    {
                                        Set<Set<OWLAxiom>> explanationsSet = explanations.getExplanationsEntailment(3);

                                        // Build the text for explanations
                                        StringBuilder explanationsText = new StringBuilder();
                                        for (Set<OWLAxiom> explanation : explanationsSet)
                                        {
                                            explanationsText.append("Explanation:\n");
                                            for (OWLAxiom ax : explanation)
                                            {
                                                String cleanedAxiomString = ax.toString().replaceAll("http.*?#", "");
                                                explanationsText.append(cleanedAxiomString).append("\n");
                                            }
                                            explanationsText.append("\n"); // Add newline for better separation
                                        }

                                        // Append the complete set of explanations to the text area
                                        justificationsTextArea.setText(explanationsText.toString());
                                    }
                                    catch (Exception exception)
                                    {
                                        // Generate and display explanations
                                        Set<Set<OWLAxiom>> explanationsSet = explanations.getExplanationsEntailment(3);

                                        // Build the text for explanations
                                        StringBuilder explanationsText = new StringBuilder();
                                        for (Set<OWLAxiom> explanation : explanationsSet)
                                        {
                                            explanationsText.append("Explanation:\n");
                                            for (OWLAxiom ax : explanation)
                                            {
                                                String cleanedAxiomString = ax.toString().replaceAll("http.*?#", "");
                                                explanationsText.append(cleanedAxiomString).append("\n");
                                            }
                                            explanationsText.append("\n"); // Add newline for better separation
                                        }

                                        // Append the complete set of explanations to the text area
                                        justificationsTextArea.setText(explanationsText.toString());
                                    }
                                }
                                else
                                {
                                    // Display the image and the text
                                    ImageIcon xIcon = new ImageIcon("C:\\Users\\30693\\IdeaProjects\\OntologyTool\\pictures\\x.jpg");
                                    Image xImage = xIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                                    entailmentResultLabel.setIcon(new ImageIcon(xImage));
                                    entailmentResultLabel.setText("Axiom is not entailed");

                                    // Call the method for entailed explanations
                                    Set<Set<String>> hypotheses = Explanations.getExplanationsNonEntailment(selectedOntology, 3, axiomInput);

                                    // Build the text for explanations
                                    StringBuilder sb = new StringBuilder();

                                    // Counter for the hypotheses
                                    int hypothesisCount = 1;

                                    for (Set<String> hypothesis : hypotheses)
                                    {
                                        sb.append("Hypothesis ").append(hypothesisCount).append("\n");
                                        sb.append("-------------------------------------------------------------------------------------\n");

                                        for (String ax : hypothesis)
                                        {
                                            sb.append(ax.toString()).append("\n");
                                        }

                                        sb.append("\n");
                                        hypothesisCount++;
                                    }

                                    justificationsTextArea.setText(sb.toString());
                                }
                            }
                            catch (Exception ex)
                            {
                                JOptionPane.showMessageDialog(null, ex);
                            }
                        }
                        else
                        {
                            JOptionPane.showMessageDialog(null, "Failed to find or read the ontology file.");
                        }
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(null, "Axiom input cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }

        });


        /**
         * --------------------------- Help Button -------------------------------
         * When this button is clicked, it appears a window with instructions about the tool
         */
        helpButton.addActionListener(e ->
        {

            // Create a new JFrame for the guider window
            JFrame guiderFrame = new JFrame("Help Window");
            guiderFrame.setSize(600, 400);

            JPanel guiderPanel = new JPanel(new BorderLayout(10, 10));
            guiderPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Create the text pane and set the border
            JTextPane textPane = new JTextPane();
            textPane.setBorder(new EmptyBorder(10, 10, 10, 10));
            textPane.setEditable(false);

            // Text to be displayed
            String text = "The \"Load Ontology\" button located on the top left of the main window of the application is used to load ontologies that users have stored on their computer. When they click the button a window opens in their computer files. This allows them to browse and select any ontology they want. Attention, the file must have the extension (.owl)! Of course there is a successful message if the process is right and an error message if something goes wrong.\n\n"
                    + "The \"Loaded Ontologies\" area is a list in which all the ontologies that have been loaded by the user are displayed. The user can select an ontology from the list by clicking on its name. When the user clicks on an ontology in the list then in the areas \"Ontology IRI\", \"Number Of Axioms\" and \"Axioms\" the corresponding elements of the ontology are displayed in each area.\n\n"
                    + "The \"Remove Button\" removes an ontology from the \"Loaded Ontologies\" list. Attention, the user must have selected an ontology in the list to be removed as mentioned before, i.e. by clicking on its name. If something goes wrong an appropriate message is displayed.\n\n"
                    + "The areas \"Ontology IRI\", \"Number Of Axioms\" and \"Axioms\" are displaying the details of the clicked ontology from the \"Loaded Ontologies\" list. If nothing is clicked then these areas are empty.\n\n"
                    + "The \"Create Hypothesis\" button is used to build ontologies. The user can create his own ontology by adding axioms which are syntactically correctly written. He can consult the \"Legend Button\" which is located inside the ontology construction window for the correct syntactic way to write the axioms. Of course the user is able to save his ontology to his computer files.\n\n"
                    + "The \"Generate Explanations\" button is used to check if an axiom is entailed in the selected ontology. If the axiom is entailed then the tick mark and some explanations are displayed, if of course they exist. If it is not then the x mark is displayed and then the user has to select or create two folders in order. The first folder will contain the hypotheses and the second folder will contain the union ontologies. All the files that will be stored inside the folders are done automatically so all the user has to do is load them to see what they contain inside. Finally two graphs will be displayed in two new windows which the user can watch. Attention, the user have to select an ontology from the list otherwise a appropriate message will be displayed.\n\n"
                    + "The \"Start Reasoner\" button is used to synchronize an ontology. The user must have selected an ontology from the list or else an appropriate message will be displayed. If he has selected an ontology from the list then it will sync it and create a new sync ontology which will be automatically named inferred + the name of the ontology and will be stored in the same place as the original ontology.";

            // Add styled text to the text pane
            owlFunctions.addStyledText(textPane, text);

            // Create a label for the title
            JLabel titleLabel = new JLabel("Instructions");
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
            titleLabel.setForeground(Color.ORANGE);
            titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));  // Add some space below the title

            // Add the title and text pane to the panel
            guiderPanel.add(titleLabel, BorderLayout.NORTH);
            JScrollPane scrollTextPanel = new JScrollPane(textPane);
            scrollTextPanel.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 2));
            guiderPanel.add(scrollTextPanel, BorderLayout.CENTER);

            guiderFrame.add(guiderPanel);
            guiderFrame.setVisible(true);

        });

    }


    public static void main (String[]args)
    {

        // Execute the GUI creation
        SwingUtilities.invokeLater(() ->
        {

            // Create and display the OwlUtilsGUI
            new GUI().setVisible(true);

        });

    }

}