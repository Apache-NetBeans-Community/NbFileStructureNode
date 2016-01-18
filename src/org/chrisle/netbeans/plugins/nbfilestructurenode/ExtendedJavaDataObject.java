package org.chrisle.netbeans.plugins.nbfilestructurenode;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePathScanner;
import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.StatusDisplayer;
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
import org.openide.util.NbBundle.Messages;

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

    public ExtendedJavaDataObject(FileObject fo, MultiFileLoader loader) throws DataObjectExistsException {
        super(fo, loader);
        registerEditor("text/x-java", true);
    }

    public static Image _javaClassIcon;

    @Override
    protected Node createNodeDelegate() {
        DataNode dataNode = new DataNode(this, Children.create(new JavaChildFactory(this), true), getLookup());
        
        dataNode.setIconBase("org/chrisle/netbeans/plugins/nbfilestructurenode/resources/javaClassFile.gif");

        return dataNode;
    }
    
    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }

    private static class JavaChildFactory extends ChildFactory<Object> {
        private final ExtendedJavaDataObject dObj;

        public JavaChildFactory(ExtendedJavaDataObject dObj) {
            this.dObj = dObj;
        }

        @Override
        protected boolean createKeys(List list) {
            FileObject fObj = dObj.getPrimaryFile();
            JavaSource js = JavaSource.forFileObject(fObj);
            
            try {
                _list = list;

                js.runUserActionTask(new Task<CompilationController>() {
                    public void run(CompilationController parameter) throws IOException {
                        parameter.toPhase(Phase.ELEMENTS_RESOLVED);
                        new MemberVisitor(parameter).scan(parameter.getCompilationUnit(), null);
                    }
                }, true);
                
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            return true;
        }

        @Override
        protected Node createNodeForKey(Object key) {
            Node childNode = new JavaClassNode();
            childNode.setDisplayName(key.toString());

            return childNode;
        }
    }
    
    private static class JavaClassNode extends AbstractNode {
        @Override
        public Image getIcon(int type) {
            return _javaClassIcon;
        }
        
        public JavaClassNode() {
            super(Children.LEAF);
        }
    }

    private static class MemberVisitor extends TreePathScanner<Void, Void> {
        private final CompilationInfo info;

        public MemberVisitor(CompilationInfo info) {
            this.info = info;
        }

        @Override
        public Void visitClass(ClassTree t, Void v) {
            Element el = info.getTrees().getElement(getCurrentPath());

            if (el == null) {
                StatusDisplayer.getDefault().setStatusText("Cannot resolve class!");
            } else {
                TypeElement te = (TypeElement) el;
                
                if(null != te.getKind()) switch (te.getKind()) {
                    case CLASS:
                        _javaClassIcon = ImageUtilities.loadImage("org/chrisle/netbeans/plugins/nbfilestructurenode/resources/classTypeJavaClass.png");
                        break;
                    case INTERFACE:
                        _javaClassIcon = ImageUtilities.loadImage("org/chrisle/netbeans/plugins/nbfilestructurenode/resources/classTypeInterface.png");
                        break;
                    case ENUM:
                        _javaClassIcon = ImageUtilities.loadImage("org/chrisle/netbeans/plugins/nbfilestructurenode/resources/classTypeEnum.png");
                        break;
                    default:
                        break;
                }
                
                _list.add(te.getSimpleName().toString());
                
//                JOptionPane.showMessageDialog(null, te.getSimpleName());
//                
//                for (Element enclosedElement : te.getEnclosedElements()) {
//                    JOptionPane.showMessageDialog(null, enclosedElement.getSimpleName());
//                }
//
////                List<ExecutableElement> methodsIn = ElementFilter.methodsIn(te.getEnclosedElements());
////                List<ExecutableElement> methodsIn = ElementFilter.methodsIn(t.getMembers());
////                List<TypeElement> typesIn = ElementFilter.typesIn(t.);
////                
////                JOptionPane.showMessageDialog(null, typesIn.size());
////                
////                for (TypeElement typeElem : typesIn) {
////                    JOptionPane.showMessageDialog(null, typeElem.getSimpleName().toString());
////                }
//                
////                for (ExecutableElement executableElement : methodsIn) {
////                    JOptionPane.showMessageDialog(null, executableElement.getSimpleName().toString());
////                }
//
////                System.err.println("Resolved class: " + te.getQualifiedName().toString());
////                //XXX: only as an example, uses toString on element, which should be used only for debugging
////                System.err.println("enclosed methods: " + ElementFilter.methodsIn(te.getEnclosedElements()));
////                System.err.println("enclosed types: " + ElementFilter.typesIn(te.getEnclosedElements()));
            }
            return null;
        }
    }
}