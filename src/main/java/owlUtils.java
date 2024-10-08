import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentClassAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import java.util.Set;

public class owlUtils
{

    public static OWLOntology ontology;
    public static OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
    public static DefaultListModel<String> ontologyListModel; // The DefaultListModel instance for the ontology list

    /***
     * Function which creates the OWLOntologyManager instance
     * @return ontologyManager
     */
    public static OWLOntologyManager createOntologyManager() {return ontologyManager;}

    /***
     * Function which loads any ontology
     * @return ontology
     */
    public static OWLOntology loadOntology(String path) throws OWLOntologyCreationException
    {

        File file = new File(path); // Loading file by using the path

        OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager(); // Create ontology manager

        OWLOntology ontology = ontologyManager.loadOntologyFromOntologyDocument(file); // Loading ontology

        return ontology;

    }

    /***
     * Function which removes an ontology from the manager and checks the list
     */
    public static void removeOntology(String ontologyFileName)
    {

        if (ontology != null) {
            ontologyManager.removeOntology(ontology); // Remove the ontology from the manager
        }
        else
        {
            System.out.println("Ontology removed!");
        }


        if (ontologyListModel != null) // Check if it has been initialized
        {
            // Remove the ontology from the list
            for (int i = 0; i < ontologyListModel.getSize(); i++)
            {
                if (ontologyListModel.getElementAt(i).equals(ontologyFileName))
                {
                    ontologyListModel.remove(i);
                    break;
                }
            }
        }

    }

    /***
     * Function which saves a new ontology
     */
    public static void saveOntology(OWLOntology ontology, String filePath, OWLOntologyManager manager)
    {
        try
        {
            manager.saveOntology(ontology, IRI.create(new File(filePath).toURI())); // Saving the ontology
        }
        catch (OWLOntologyStorageException e)
        {
            e.printStackTrace(); // Printing error
        }
    }

    /***
     * Function which creates the OWLDataFactory instance
     * @return ontologyManager.getOWLDataFactory()
     */
    public static OWLDataFactory createDataFactory() {return ontologyManager.getOWLDataFactory();}

    /***
     * Function to get the ontology's IRI
     * @return IRI
     */
    public static IRI getOntologyIRI(OWLOntology ontology) {return ontology.getOntologyID().getOntologyIRI().get();}

    /***
     * Function to get a set of axioms of the ontology
     * @return Axioms
     */
    public static Set<OWLAxiom> getSetOfOntoAxioms(OWLOntology ontology) {return ontology.getAxioms();}

    /**
     * Function to create the Pellet reasoner.
     * @return OWLReasoner instance using Pellet.
     */
    public static OWLReasoner createReasoner(OWLOntology ontology)
    {
        // Create the reasoner
        OWLReasonerFactory reasonerFactory = new ReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);

        return reasoner;
    }

    /**
     * Function to synchronize the reasoner, inferring additional knowledge and updating the ontology.
     */
    public static void syncReasoner(OWLOntologyManager manager, OWLOntology ontology, String originalOntologyFilePath) throws OWLOntologyStorageException
    {

        // Create Reasoner
        OWLReasoner reasoner = createReasoner(ontology);

        // Precompute inferences
        reasoner.precomputeInferences(InferenceType.values());

        // Create the InferredOntologyGenerator and add the results
        List<InferredAxiomGenerator<? extends OWLAxiom>> gens = new ArrayList<>();
        gens.add(new InferredSubClassAxiomGenerator());
        gens.add(new InferredClassAssertionAxiomGenerator());
        gens.add(new InferredEquivalentClassAxiomGenerator());
        gens.add(new InferredPropertyAssertionGenerator());
        gens.add(new InferredInverseObjectPropertiesAxiomGenerator());
        gens.add(new InferredObjectPropertyCharacteristicAxiomGenerator());
        gens.add(new InferredSubObjectPropertyAxiomGenerator());
        gens.add(new InferredSubDataPropertyAxiomGenerator());

        InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner, gens);
        iog.fillOntology(manager.getOWLDataFactory(), ontology);

        // Name the ontology with its previous name + "_inferred"
        String inferredOntologyFilePath = originalOntologyFilePath.replace(".owl", "_inferred.owl");

        // Saving with the new name
        manager.saveOntology(ontology, new RDFXMLDocumentFormat(), IRI.create(new File(inferredOntologyFilePath).toURI()));

    }

}