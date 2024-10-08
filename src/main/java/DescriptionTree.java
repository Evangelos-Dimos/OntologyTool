import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import org.javatuples.Pair;


public class DescriptionTree
{

    /**
     * Class attributes of class {@link DescriptionTree}
     */
    public char vertex_notation;
    public Map<Integer, LinkedList<Integer>> tree = new HashMap<>();
    public Map<Integer, DTNode> V = new HashMap<>();
    public Map<String, Integer> inv_V = new HashMap<>();
    public Map<Pair<Integer, Integer>, Character> E = new HashMap<>();

    /**
     * Constructor for class {@link DescriptionTree}
     * @param vertex_notation
     */
    public DescriptionTree(char vertex_notation) {
        this.vertex_notation = vertex_notation;
    }

    // ====================================================================== Tree Functions ======================================================================
    /**
     * Function for adding a new edge in a tree.
     * @param v
     * @param u
     * @param edgeLabel
     * @param label_v
     * @param label_u
     */
    public void addEdge(int v, int u, char edgeLabel, LinkedList<String> label_v, LinkedList<String> label_u)
    {
        if (!this.tree.containsKey(v))
        {
            LinkedList<Integer> x = new LinkedList<>();
            x.add(u);
            this.tree.put(v, x);
        }
        else
        {
            this.tree.get(v).add(u);
        }

        Pair<Integer, Integer> key_to_check = new Pair<>(v, u);

        if (!this.E.containsKey(key_to_check))
        {
            this.E.put(key_to_check, edgeLabel);
        }

        if (!V.containsKey(v))
        {
            String vertexName = Character.toString((char) this.vertex_notation) + v;
            DTNode n_v = new DTNode(vertexName, label_v);

            this.V.put(v, n_v);
            this.inv_V.put(n_v.name, v);

        }

        if (!V.containsKey(u))
        {
            String vertexName = Character.toString((char) this.vertex_notation) + u;
            DTNode n_u = new DTNode(vertexName, label_u);

            this.V.put(u, n_u);
            this.inv_V.put(n_u.name, u);

        }
    }


    // ====================================================================== Definitions and Formulae for the Method ======================================================================
    /**
     * For tree T, T.N(v) := {y in T.V | <x, y> in T.E}.
     * @param v
     * @return N(v)
     */
    public Set<Integer> N(int v)
    {
        if (this.tree.containsKey(v))
        {
            Set<Integer> N = new HashSet<>(this.tree.get(v));

            return N;
        }
        else
        {
            Set<Integer> N = new HashSet<>();

            return N;
        }
    }

    /**
     * For set of neighbors N(v) of node v in T, find N(v)/~ wrt the equivalence relation:
     * @param neighbors N(v)
     * @return N(v)/~
     */
    public Set<Set<Integer>> N_equivClasses(Set<Integer> neighbors, int x)
    {
        Set<Set<Integer>> N_equiv = new HashSet<>();

        // for all y in N(x)
        for (Integer y : neighbors)
        {
            // [y] in N/~(x)
            Set<Integer> equivalenceClass = new HashSet<>();
            equivalenceClass.add(y);

            // for all z in N(x)
            for (Integer z : neighbors)
            {
                // <x, y> in E1, <x, z> in E2
                Pair<Integer, Integer> e1 = new Pair<>(x, y);
                Pair<Integer, Integer> e2 = new Pair<>(x, z);

                // iff xi(<x, y>) == xi(<x, z>)   =>    z in [y]
                if (this.E.get(e1) == this.E.get(e2)) { equivalenceClass.add(z); }
            }

            // [y] in N(x)/~
            N_equiv.add(equivalenceClass);
        }
        // N(x)/~
        return N_equiv;
    }

}













