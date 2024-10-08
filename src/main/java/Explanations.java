//package com.xai.methods;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import openllet.owlapi.OpenlletReasoner;
import openllet.owlapi.OpenlletReasonerFactory;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import openllet.owlapi.explanation.PelletExplanation;
import javax.swing.*;

public class Explanations
{

    public OWLOntology ontology;
    public OWLAxiom axiom;

    /**
     * Constructor for class Explanations
     */
    public Explanations(OWLOntology ontology, OWLAxiom axiom)
    {
        this.ontology = ontology;
        this.axiom = axiom;
    }

    /**
     * Function that checks whether the axiom is entailed or not entailed.
     * @return true / false
     */
    public boolean isEntailed()
    {

        OpenlletReasoner reasoner = OpenlletReasonerFactory.getInstance().createReasoner(this.ontology);

        reasoner.precomputeInferences(InferenceType.values());

        return reasoner.isEntailed(this.axiom);

    }

    /**
     * Function that returns the explanations for an entailment.
     * @return Set of explanations for an axiom
     */
    public Set<Set<OWLAxiom>> getExplanationsEntailment(int numberOfExplanations) throws OWLException, IOException {

        OpenlletReasoner reasoner = OpenlletReasonerFactory.getInstance().createReasoner(this.ontology);

        reasoner.precomputeInferences(InferenceType.values());

        PelletExplanation explanationGenerator = new PelletExplanation(reasoner);

        Set<Set<OWLAxiom>> explanations = explanationGenerator.getEntailmentExplanations(this.axiom);

        return explanations;
    }

    /**
     * Function that returns the explanations for a non-entailment.
     */
    public static Set<Set<String>> getExplanationsNonEntailment(OWLOntology selectedOntology, int numberOfExplanations, String input) throws OWLOntologyCreationException
    {

        String splitted_input[] = input.split(" SubClassOf ");

        for (String str : splitted_input)
        {
            System.out.println(str);
        }

        String string1 = splitted_input[0];
        String string2 = splitted_input[1];

        DescriptionTree T1 = owlFunctions.expr2tree(string1,'v');
        DescriptionTree T2 = owlFunctions.expr2tree(string2,'w');


        System.out.println(string1);
        System.out.println(string2);
        System.out.println(T1.tree);
        System.out.println(T2.tree);
        owlFunctions.displayTrees(T1,T2);

        // Generate Hypotheses:
        XeNON xenon = new XeNON(T1, T2);

        Map i = xenon.subtreeIsomorphisms();

        Set<Set<String>> Hypotheses = xenon.constructHypotheses(i, "SubClassOf");

        // Create an ontology manager
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        // Create a data factory
        OWLDataFactory dataFactory = manager.getOWLDataFactory();

        JOptionPane.showMessageDialog(null, "CREATE or CHOOSE a folder to save all the hypotheses from the non-entailment process");

        // Create the destination folder
        String destinationFolder = owlFunctions.selectDestinationFolder();

        int index = 1;

        for (Set<String> hypothesis : Hypotheses)
        {

            UUID uuid = UUID.randomUUID();
            IRI ontologyIRI = IRI.create("http://example.org/ontology/" + uuid);

            try
            {

                OWLOntology ontology = manager.createOntology(ontologyIRI);

                // Handle each axiom in the hypothesis
                for (String axiom : hypothesis)
                {
                    owlFunctions.handleUserInput(axiom, ontology, manager, dataFactory, ontologyIRI);
                }

                // Save the ontology to a file
                String fileName = "Explanations_Hypothesis_" + index + ".owl";
                String filePath = destinationFolder + File.separator + fileName;
                owlUtils.saveOntology(ontology, filePath, manager);

                index++;
            }
            catch (OWLOntologyCreationException e)
            {
                e.printStackTrace();
            }
        }

        // Store the axioms of the selected ontology in a Set
        Set<OWLAxiom> selectedOntologyAxioms = selectedOntology.getAxioms();

        JOptionPane.showMessageDialog(null, "CREATE or CHOOSE a folder to save new ontologies which will be made by the union of every hypothesis and the selected ontology");

        // Create the destination folder
        String destinationUnionFolder = owlFunctions.selectDestinationFolder();

        int number = 1;

        for (Set<String> hypothesis : Hypotheses)
        {

            UUID uuid = UUID.randomUUID();
            IRI ontologyIRI = IRI.create("http://example.org/ontology/" + uuid);

            OWLOntology ontology = manager.createOntology(ontologyIRI);

            // Add axioms from the selected ontology to the new hypothesis ontology
            for (OWLAxiom axiom : selectedOntologyAxioms)
            {
                manager.addAxiom(ontology, axiom);
                System.out.println(axiom);
            }

            // Handle each axiom in the hypothesis
            for (String axiom : hypothesis)
            {
                owlFunctions.handleUserInput(axiom, ontology, manager, dataFactory, ontologyIRI);
            }

            // Save the ontology to a file
            String fileName = "Union_Hypothesis_" + number + ".owl";
            String filePath = destinationUnionFolder + File.separator + fileName;
            owlUtils.saveOntology(ontology, filePath, manager);

            number++;
        }

        return Hypotheses;

    }

}