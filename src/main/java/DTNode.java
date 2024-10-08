import java.util.LinkedList;

public class DTNode
{
    public String name;
    public LinkedList<String> label;

    public DTNode(String name, LinkedList<String> label)
    {
        this.name = name;
        this.label = label;
    }
}