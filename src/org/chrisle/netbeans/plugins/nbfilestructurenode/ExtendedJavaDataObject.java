package org.chrisle.netbeans.plugins.nbfilestructurenode;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.FilterNode.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

/**
 *
 * @author ChrisLE
 */
@Messages({
    "LBL_InstallPluginAction_LOADER=Java file"
})
@MIMEResolver.ExtensionRegistration(
        displayName = "#LBL_InstallPluginAction_LOADER",
        mimeType = "text/x-java",
        extension = {"java", "JAVA"}
)
@DataObject.Registration(
        mimeType = "text/x-java",
        displayName = "#LBL_InstallPluginAction_LOADER",
        position = 300
)
@ActionReferences({
    @ActionReference(
            path = "Loaders/text/x-java/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200
    ),
    @ActionReference(
            path = "Loaders/text/x-java/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 300
    ),
    @ActionReference(
            path = "Loaders/text/x-java/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 400,
            separatorAfter = 500
    ),
    @ActionReference(
            path = "Loaders/text/x-java/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 600
    ),
    @ActionReference(
            path = "Loaders/text/x-java/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 700,
            separatorAfter = 800
    ),
    @ActionReference(
            path = "Loaders/text/x-java/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
            position = 900,
            separatorAfter = 1000
    ),
    @ActionReference(
            path = "Loaders/text/x-java/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 1100,
            separatorAfter = 1200
    ),
    @ActionReference(
            path = "Loaders/text/x-java/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
            position = 1300
    ),
    @ActionReference(
            path = "Loaders/text/x-java/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 1400
    )
})
public class ExtendedJavaDataObject extends MultiDataObject {

    public static List _list = new ArrayList();

    public static Image getIconForElement(TypeElement te) {
        Image result = null;
        if (null != te.getKind()) {
            switch (te.getKind()) {
                case CLASS:
                    result = ImageUtilities.loadImage("org/chrisle/netbeans/plugins/nbfilestructurenode/resources/classTypeJavaClass.png");
                    break;
                case INTERFACE:
                    result = ImageUtilities.loadImage("org/chrisle/netbeans/plugins/nbfilestructurenode/resources/classTypeInterface.png");
                    break;
                case ENUM:
                    result = ImageUtilities.loadImage("org/chrisle/netbeans/plugins/nbfilestructurenode/resources/classTypeEnum.png");
                    break;
                case ANNOTATION_TYPE:
                    result = ImageUtilities.loadImage("org/chrisle/netbeans/plugins/nbfilestructurenode/resources/classTypeAnnot.png");
                    break;
//                case EXCEPTION_PARAMETER:
//                    result = ImageUtilities.loadImage("org/chrisle/netbeans/plugins/nbfilestructurenode/resources/classTypeException.png");
//                    break;
                default:
                    break;
            }
        }

        return result;
    }

    public ExtendedJavaDataObject(FileObject fo, MultiFileLoader loader) throws DataObjectExistsException {
        super(fo, loader);
        registerEditor("text/x-java", true);
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }

    @Override
    protected Node createNodeDelegate() {
        List<TypeElement> children = getElementsFromFile(getPrimaryFile());
        Image iconForElement = null;

        if (!children.isEmpty()) {
            TypeElement firstElement = children.iterator().next();
            iconForElement = getIconForElement(firstElement);
        }

        final Image icon = iconForElement;
        DataNode dataNode = new DataNode(this, Children.create(new JavaChildFactory(children), true), getLookup()) {
            @Override
            public Image getIcon(int type) {
                if (null != icon) {
                    return icon;
                } else {
                    //show default ?
                    return ImageUtilities.loadImage("org/chrisle/netbeans/plugins/nbfilestructurenode/resources/javaClassFile.gif");
                }
            }

            @Override
            public Image getOpenedIcon(int type) {
                return getIcon(type);
            }
        };

        return dataNode;
    }

    private List<TypeElement> getElementsFromFile(FileObject fObj) throws IllegalArgumentException {
        final List<TypeElement> result = new ArrayList<>();
        JavaSource js = JavaSource.forFileObject(fObj);
        if (js == null) {
            return result;
        }
        try {
            js.runUserActionTask(new Task<CompilationController>() {

                @Override
                public void run(CompilationController cc) throws Exception {
                    cc.toPhase(Phase.ELEMENTS_RESOLVED);

                    for (TypeElement te : cc.getTopLevelElements()) {
                        result.add(te);
                    }
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return result;
    }

    private class JavaChildFactory extends ChildFactory<TypeElement> {

        private final List<TypeElement> elements;

        public JavaChildFactory(final List<TypeElement> list) {
            this.elements = list;
        }

        @Override
        protected boolean createKeys(final List<TypeElement> elements) {
            elements.addAll(this.elements);

            return true;
        }

        @Override
        protected Node createNodeForKey(TypeElement te) {
            JavaClassNode childNode = new JavaClassNode();
            childNode.setDisplayName(te.getSimpleName().toString());

            Image icon = getIconForElement(te);
            if (icon != null) {
                childNode.icon = icon;
            }

            return childNode;
        }
    }

    private class JavaClassNode extends AbstractNode {

        Image icon;

        @Override
        public Image getIcon(int type) {
            return icon;
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[]{new MyAction()};
        }

        public JavaClassNode() {
            super(Children.LEAF);
        }
    }

    private final class MyAction extends AbstractAction implements LookupListener {
        private Lookup context;
        Lookup.Result<JavaClassNode> lkpInfo;

        public MyAction() {
            this(Utilities.actionsGlobalContext());
        }

        public MyAction(Lookup context) {
            this.context = context;

            //The thing we want to listen for the presence or absence of
            //on the global selection
//            lkpInfo = context.lookupResult(JavaClassNode.class);
//            lkpInfo.addLookupListener(this);
//            resultChanged(null);
            Lookup.Result res = context.lookupResult(JavaClassNode.class);
            res.addLookupListener(new LookupListener() {
                public void resultChanged(LookupEvent evt) {
                    Collection c = ((Lookup.Result) evt.getSource()).allInstances();
                    //do something with the collection of 0 or more instances - the collection has changed

                    if (!c.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Selected");
                    }
                }
            });
        }

        @Override
        public void actionPerformed(ActionEvent e) {
        }

        @Override
        public void resultChanged(LookupEvent evt) {
//            Collection c = ((Lookup.Result<JavaClassNode>) evt.getSource()).allInstances();
//            
//            if (c.isEmpty()) {
//                 JOptionPane.showMessageDialog(null, "Selected");
//            } else {
//                 JOptionPane.showMessageDialog(null, "Not Selected");
//            }

//            JavaClassNode javaClassNode = Utilities.actionsGlobalContext().lookup(JavaClassNode.class);
//            int selected = lkpInfo.allInstances().size();
//
//            if (selected == 0) {
//                JOptionPane.showMessageDialog(null, "Selected");
//            } else {
//                JOptionPane.showMessageDialog(null, "Not selected");
//            }
        }
    }
}

class Test {

}

interface Tester {

}
