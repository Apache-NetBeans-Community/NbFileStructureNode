package org.chrisle.netbeans.plugins.nbfilestructurenode;
import java.awt.Image;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;


public class JavaClassNode extends AbstractNode {
    Image icon;

    @Override
    public Image getIcon(int type) {
        return icon;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public Action getPreferredAction() {
        return new JavaClassNodeAction();
    }

    public JavaClassNode() {
//            super(Children.create(new JavaChildFactory(typeElemenChilds), true));
        super(Children.LEAF);
    }
}