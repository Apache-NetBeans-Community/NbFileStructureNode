package org.chrisle.netbeans.plugins.nbfilestructurenode;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import static org.chrisle.netbeans.plugins.nbfilestructurenode.ExtendedJavaDataObject.getIconForElement;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

public class JavaChildFactory extends ChildFactory<Element> {
    private final List<Element> elements;
    private List<Element> _typeElementChilds = new ArrayList<>();

    public JavaChildFactory(final List<Element> list) {
        this.elements = list;
    }

    @Override
    protected boolean createKeys(final List<Element> elements) {
        if (this.elements != null) {
            elements.addAll(this.elements);
        }

        return true;
    }

    @Override
    protected Node createNodeForKey(Element te) {
        JavaClassNode childNode = new JavaClassNode();
        Image icon = getIconForElement(te);

        childNode.setDisplayName(te.getSimpleName().toString());
        _typeElementChilds = (List<Element>) te.getEnclosedElements();

        if (icon != null) {
            childNode.icon = icon;
        }
            
        return childNode;
    }
}