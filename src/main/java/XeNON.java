import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import org.javatuples.Pair;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;


public class XeNON<E>
{

    public DescriptionTree T1;
    public DescriptionTree T2;

    /**
     * Constructor for class {@link XeNON}
     * @param T1
     * @param T2
     */
    public XeNON(DescriptionTree T1, DescriptionTree T2)
    {
        this.T1 = T1;
        this.T2 = T2;
    }

    /**
     * R SubClassOf N(v0)/~ x N(w0)/~.
     * @param v
     * @param w
     * @param N_equivClasses_1
     * @param N_equivClasses_2
     * @return R
     */
    public Set<Pair<Set<Integer>, Set<Integer>>> R(DescriptionTree T1, DescriptionTree T2, int v, int w, Set<Set<Integer>> N_equivClasses_1, Set<Set<Integer>> N_equivClasses_2)
    {
        Set<Pair<Set<Integer>, Set<Integer>>> R_vw = new HashSet<Pair<Set<Integer>, Set<Integer>>>();

        // for all [x] in N(v)/~
        for (Set<Integer> x_equivClass : N_equivClasses_1)
        {
            // for all [y] in N(w)/~
            for (Set<Integer> y_equivClass : N_equivClasses_2)
            {
                // x in [x], y in [y]
                int x = -1;
                int y = -1;
                for (Integer a : x_equivClass) { x = a; break; }
                for (Integer b : y_equivClass) { y = b; break; }

                // <v, x> in E1, <w, y> in E2
                Pair<Integer, Integer> e1 = new Pair<Integer, Integer>(v, x);
                Pair<Integer, Integer> e2 = new Pair<Integer, Integer>(w, y);

                // iff xi(<v, x>) == xi(<w, y>) => <[x], [y]> in R
                if (T1.E.get(e1) == T2.E.get(e2))
                {
                    Pair<Set<Integer>, Set<Integer>> pairOfEquivalenceClasses = new Pair<Set<Integer>, Set<Integer>>(x_equivClass, y_equivClass);
                    R_vw.add(pairOfEquivalenceClasses);
                }
            }
        }

        // R
        return R_vw;
    }

    /**
     * Util function for finding all permutations of length k.
     * @param p
     * @param i
     * @param k
     * @param permutations
     */
    public void nPkUtil(List<Integer> p, int i, int k, LinkedList<List<Integer>> permutations)
    {
        if (i == k)
        {
            List<Integer> subListToAdd = new LinkedList<Integer>(p.subList(0, k));

            permutations.add(subListToAdd);

            return ;
        }

        for (int j = i ; j < p.size() ; j++)
        {
            Collections.swap(p, i, j);
            this.nPkUtil(p, i + 1, k, permutations);
            Collections.swap(p, i, j);
        }
    }

    /**
     * Function for finding all permutations of length k.
     * @param p
     * @param i
     * @param k
     * @return permutations
     */
    public LinkedList<List<Integer>> nPk(List<Integer> p, int i, int k)
    {

        LinkedList<List<Integer>> permutations = new LinkedList<List<Integer>>();

        this.nPkUtil(p, i, k, permutations);

        return permutations;

    }

    /**
     * Map [x] -> [y].
     * @param r
     * @return [x] -> [y]
     */
    public List<List<Pair<Integer, Integer>>> mapEquivalenceClasses(Pair<Set<Integer>, Set<Integer>> r)
    {
        List<List<Pair<Integer, Integer>>> mappings = new LinkedList<List<Pair<Integer, Integer>>>();

        List<Integer> x_equivClass = new LinkedList<Integer>(r.getValue0());  // [x]
        List<Integer> y_equivClass = new LinkedList<Integer>(r.getValue1());  // [y]

        if (x_equivClass.size() <= y_equivClass.size())
        {

            List<List<Integer>> result = this.nPk(new LinkedList<Integer>(y_equivClass), 0, x_equivClass.size());

            for (List<Integer> comb : result)
            {

                List<Pair<Integer, Integer>> combPair = new LinkedList<Pair<Integer, Integer>>();

                for (int i = 0; i < comb.size(); i++) { combPair.add(new Pair<Integer, Integer>(x_equivClass.get(i), comb.get(i))); }

                mappings.add(combPair);

            }

        }
        else
        {

            List<List<Integer>> result = this.nPk(new LinkedList<Integer>(x_equivClass), 0, y_equivClass.size());

            for (List<Integer> comb : result)
            {

                List<Pair<Integer, Integer>> combPair = new LinkedList<Pair<Integer, Integer>>();

                for (int i = 0; i < comb.size(); i++) { combPair.add(new Pair<Integer, Integer>(comb.get(i), y_equivClass.get(i))); }

                mappings.add(combPair);
            }

        }

        return mappings;
    }

    /**
     * Function for computing the Cartesian product of n sets.
     * @param sets
     * @return S1 x S2 x ... Sn
     */
    public static List<List<List<Pair<Integer, Integer>>>> getCartesianProduct(List<List<List<Pair<Integer, Integer>>>> sets)
    {
        List<List<List<Pair<Integer, Integer>>>> result = new ArrayList<>();
        getCartesianProductHelper(sets, 0, new ArrayList<>(), result);

        return result;
    }

    /**
     * Helper function for computing the Cartesian product of n sets.
     * @param sets
     * @param index
     * @param current
     * @param result
     */
    private static void getCartesianProductHelper(List<List<List<Pair<Integer, Integer>>>> sets, int index, List<List<Pair<Integer, Integer>>> current,
                                                  List<List<List<Pair<Integer, Integer>>>> result)
    {

        if (index == sets.size())
        {
            result.add(new ArrayList<>(current));
            return;
        }

        List<List<Pair<Integer, Integer>>> currentSet = sets.get(index);

        for (List<Pair<Integer, Integer>> element: currentSet)
        {
            current.add(element);
            getCartesianProductHelper(sets, index+1, current, result);
            current.remove(current.size() - 1);
        }

    }

    /**
     * Method for computing mappings from R(v, w) between nodes v in T1 and w in T2 (Main theory)
     * @param v
     * @param w
     * @return mappings
     */
    public List<List<Pair<Integer, Integer>>> getMappingsOfEquivalenceClasses(int v, int w)
    {
        // Find N(v) and N(w):
        Set<Integer> N1 = this.T1.N(v);
        Set<Integer> N2 = this.T2.N(w);

        // Find N(v)/~ and N(w)/~:
        Set<Set<Integer>> N_equivClasses_1 = this.T1.N_equivClasses(N1, v);
        Set<Set<Integer>> N_equivClasses_2 = this.T2.N_equivClasses(N2, w);

        // Find relation R SubClassOf N(v)/~ x N(w)/~:
        Set<Pair<Set<Integer>, Set<Integer>>> R_vw = R(T1, T2, v, w, N_equivClasses_1, N_equivClasses_2);

        // Define the set of injective mappings M_i for all <[x], [y]> in R
        List<List<List<Pair<Integer, Integer>>>> M_i = new LinkedList<List<List<Pair<Integer, Integer>>>>();

        // Map nodes for <[x], [y]> in R:
        for (Pair<Set<Integer>, Set<Integer>> r : R_vw) { List<List<Pair<Integer, Integer>>> i = mapEquivalenceClasses(r); M_i.add(i); }

        // Construct current mappings:
        List<List<Pair<Integer, Integer>>> mappingsEQC = new LinkedList<List<Pair<Integer, Integer>>>();

        for (List<List<Pair<Integer, Integer>>> m1 : getCartesianProduct(M_i))
        {
            List<Pair<Integer, Integer>> temp = new LinkedList<Pair<Integer,Integer>>();

            for (List<Pair<Integer, Integer>> m2 : m1) { temp.addAll(m2);}

            mappingsEQC.add(temp);

        }

        return mappingsEQC;
    }

    /**
     * Method for constructing the solutions tree for subtree isomorphisms between two description trees T1 and T2
     * @return isomorphisms
     */
    public Map<Integer, List<Pair<DTNode, DTNode>>> subtreeIsomorphisms()
    {

        Map<Integer, List<Pair<DTNode, DTNode>>> isomorphisms = new HashMap<Integer, List<Pair<DTNode, DTNode>>>();

        Map<Integer, List<Pair<Integer, Integer>>> ST_nodes = new HashMap<Integer, List<Pair<Integer, Integer>>>();
        List<Pair<Integer, Integer>> startMappings = new LinkedList<Pair<Integer, Integer>>();
        startMappings.add(new Pair<>(0, 0));
        ST_nodes.put(0, startMappings);
        List<Pair<Integer, Integer>> ST_edges = new LinkedList<Pair<Integer, Integer>>();

        Queue<Integer> nodesToEvaluate = new PriorityQueue<Integer>();
        nodesToEvaluate.add(0);

        int nodeIdx = 1;

        while (!nodesToEvaluate.isEmpty())
        {

            if (ST_nodes.size() > 5000) {break;}

            // Get the current node from the solutions tree:
            int n = nodesToEvaluate.poll();

            // Get from the current node in the solutions tree the mappings to evaluate:
            List<Pair<Integer, Integer>> mappingsToEvaluate = ST_nodes.get(n);

            List<List<List<Pair<Integer, Integer>>>> unique_combination_mappings = new LinkedList<List<List<Pair<Integer, Integer>>>>();

            for (Pair<Integer, Integer> pair : mappingsToEvaluate)
            {

                // Get the vertices for which we need to find R(v, w):
                int v = pair.getValue0();
                int w = pair.getValue1();

                // Find R(v, w) and map equivalence classes derived from nodes v and w:
                List<List<Pair<Integer, Integer>>> currentMappingsEQC = getMappingsOfEquivalenceClasses(v, w);

                unique_combination_mappings.add(currentMappingsEQC);

            } // end for pair : mappingsToEvaluate

            // Construct new solution tree nodes from the currentMappingsEQC: THIS NEEDS TO GO INSIDE THE ABOVE IF-ELSEIF STATEMENT!
            List<List<List<Pair<Integer, Integer>>>> unique_combination_mappings_product = getCartesianProduct(unique_combination_mappings);

            for (List<List<Pair<Integer, Integer>>> x : unique_combination_mappings_product)
            {

                List<Pair<Integer, Integer>> new_ST_node = new LinkedList<Pair<Integer, Integer>>();

                for (List<Pair<Integer, Integer>> y : x)
                {
                    if (y.isEmpty()) { continue; }
                    new_ST_node.addAll(y);
                }

                if (new_ST_node.isEmpty()) { continue; }

                // Add new node with mappings in the solution tree:
                ST_nodes.put(nodeIdx, new_ST_node);

                // Add the new edge between the parent and child nodes:
                ST_edges.add(new Pair<Integer, Integer>(n, nodeIdx));

                // Add the index of the next node to evaluate:
                nodesToEvaluate.add(nodeIdx);

                // Increment the node index:
                nodeIdx += 1;

            } // end for x : unique_combination_mappings_product

        } // end while

        // Return all complete paths from the solutions tree containing subtree isomorphisms:
        List<List<Integer>> ST_paths = owlFunctions.setOfAllCompletePaths(owlFunctions.adjacencyMatrix(ST_nodes, ST_edges));

        // Get subtree isomorphisms:
        int numberOfSolution = 1;

        for (List<Integer> path : ST_paths)
        {

            List<Pair<DTNode, DTNode>> currentIsomorphicMapping = new LinkedList<Pair<DTNode, DTNode>>();

            for (Integer solutionsNode : path)
            {
                List<Pair<Integer, Integer>> nodeMapping = ST_nodes.get(solutionsNode);

                for (Pair<Integer, Integer> pairNodes : nodeMapping)
                {
                    currentIsomorphicMapping.add(new Pair<DTNode, DTNode>(this.T1.V.get(pairNodes.getValue0()), this.T2.V.get(pairNodes.getValue1())));
                }
            }

            isomorphisms.put(numberOfSolution, currentIsomorphicMapping);
            numberOfSolution += 1;

        }

        return isomorphisms;

    }

    /**
     * Method for constructing the hypotheses from the subtree isomorphisms between two description trees T1 and T2.
     * @param isomorphisms
     * @return H
     */
    public Set<Set<String>> constructHypotheses(Map<Integer, List<Pair<DTNode, DTNode>>> isomorphisms, String typeOfAxiom) throws OWLOntologyCreationException
    {

        Set<Set<String>> H = new HashSet<Set<String>>();

        Set<String> V1 = this.T1.inv_V.keySet();
        Set<String> V2 = this.T2.inv_V.keySet();

        for (Entry<Integer, List<Pair<DTNode, DTNode>>> entry : isomorphisms.entrySet())
        {

            List<Pair<DTNode, DTNode>> isomorphicMappings = entry.getValue();

            Set<String> h = new HashSet<String>();

            // Get the nodes to contract from T1 and T2:
            Pair<Set<Integer>, Set<Integer>> pairNodesToContract = findNodesToContract(isomorphicMappings, V1, V2);
            List<Integer> nodesToContractT1 = new LinkedList<Integer>(pairNodesToContract.getValue0());
            List<Integer> nodesToContractT2 = new LinkedList<Integer>(pairNodesToContract.getValue1());

            Collections.sort(nodesToContractT1, Collections.reverseOrder());
            Collections.sort(nodesToContractT2, Collections.reverseOrder());

            Map<Integer, List<String>> L1 = contractNodesAndModifyLabels(nodesToContractT1, T1);
            Map<Integer, List<String>> L2 = contractNodesAndModifyLabels(nodesToContractT2, T2);

            for (Pair<DTNode, DTNode> mapping : isomorphicMappings)
            {

                // For each mapping (v_i, w_i), read the labels of v_i and w_i and construct the OWLAxiom v_i SubClassOf w_i. We can just read the nodes in the isomorphic mappings, since
                // they contain the contracted nodes (class expressions) in them.
                DTNode v = mapping.getValue0();
                DTNode w = mapping.getValue1();

                // Get index of v and index of w:
                int vIdx = this.T1.inv_V.get(v.name);
                int wIdx = this.T2.inv_V.get(w.name);

                // Construct axiom as string:
                String clsInput = String.join(" and ", L1.get(vIdx)) + " " + typeOfAxiom + " " + String.join(" and ", L2.get(wIdx));

                // Add the axiom to the hypothesis:
                h.add(clsInput);

            }

            // Add the hypothesis in the set of hypotheses:
            H.add(h);

        }

        return H;
    }

    /**
     * Function that for a set of nodes to contract and a description tree it returns a modified version of the node labels.
     * @param nodesToContractT1
     * @param T
     * @return L
     */
    public Map<Integer, List<String>> contractNodesAndModifyLabels(List<Integer> nodesToContractT1, DescriptionTree T)
    {
        Map<Integer, List<String>> L = new HashMap<Integer, List<String>>();

        for (Entry<Integer, DTNode> dt_node : T.V.entrySet()) { L.put(dt_node.getKey(), new LinkedList<>(dt_node.getValue().label)); }

        for (Integer child : nodesToContractT1)
        {

            int parent = 0;

            for (Entry<Pair<Integer, Integer>, Character> x : T.E.entrySet()) { if (x.getKey().getValue1() == child) { parent = x.getKey().getValue0(); } }

            String conjunct = "(" + String.join(" and ", L.get(child)) + ")";

            L.get(parent).add("(" + T.E.get(new Pair<>(parent, child)) + " some " + conjunct + ")");

        }

        return L;
    }

    /**
     * Function that for a given isomorphic mapping it finds a set of nodes to contract from T1 and T2.
     * @param isomorphicMappings
     * @param V1
     * @param V2
     * @return <nodesToContractT1, nodesToContractT2>
     */
    public Pair<Set<Integer>, Set<Integer>> findNodesToContract(List<Pair<DTNode, DTNode>> isomorphicMappings, Set<String> V1, Set<String> V2)
    {
        // Find all common nodes in the isomorphic mapping with T1 and T2:
        Set<String> commonNodesT1 = new HashSet<String>();
        Set<String> commonNodesT2 = new HashSet<String>();

        for (Pair<DTNode, DTNode> i : isomorphicMappings) { commonNodesT1.add(i.getValue0().name); commonNodesT2.add(i.getValue1().name); }

        // Find all nodes to contract from T1 and T2:
        Set<String> nodesToContractT1_temp = new HashSet<String>(V1);
        Set<String> nodesToContractT2_temp = new HashSet<String>(V2);
        nodesToContractT1_temp.removeAll(commonNodesT1);
        nodesToContractT2_temp.removeAll(commonNodesT2);
        Set<Integer> nodesToContractT1 = new HashSet<Integer>();
        Set<Integer> nodesToContractT2 = new HashSet<Integer>();

        for (String node : nodesToContractT1_temp) { nodesToContractT1.add(this.T1.inv_V.get(node)); }
        for (String node : nodesToContractT2_temp) { nodesToContractT2.add(this.T2.inv_V.get(node)); }

        return new Pair<>(nodesToContractT1, nodesToContractT2);
    }

}
